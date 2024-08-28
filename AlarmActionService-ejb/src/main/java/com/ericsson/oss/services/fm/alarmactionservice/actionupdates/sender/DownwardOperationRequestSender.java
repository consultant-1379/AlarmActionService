/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKNOWLEDGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ADDITIONAL_INFORMATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COLON_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EXTERNAL_EVENTID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMALARM_SUPERVISION_MO_SUFFIX;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.GENERATED_ALARMID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HASH_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NULL_CHARACTER_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SOURCETYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNACKNOWLEDGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.fm.capability.util.constants.ModelCapabilitiesConstants;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.mediation.core.events.MediationClientType;
import com.ericsson.oss.mediation.core.events.OperationType;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.service.model.FmMediationAckRequest;
import com.ericsson.oss.services.fm.service.model.FmMediationClearRequest;

/**
 * This class sends the Mediation Task Request to MediationServiceConsumer Queue for acknowledge, unacknowledge and clear operation.
 **/
public class DownwardOperationRequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownwardOperationRequestSender.class);

    @Inject
    @Modeled
    private EventSender<MediationTaskRequest> mediationTaskSender;

    @Inject
    private DownwardOperationSupportedHelperBean downwardOperationSupportedHelperBean;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    /**
     * Method takes all the alarm actions information and if downward ack enabled, prepare mediation task request for eligible actions and send to
     * mediation services.
     * @param alarmActionInformations
     *            list of alarm actions information.
     * @param operatorName
     *            operator name.
     * @param alarmAction
     *            alarm action.
     */
    public void prepareAndSendMediationTaskRequest(final List<AlarmActionInformation> alarmActionInformations, final String operatorName,
            final String alarmAction) {
        final boolean isDownwordAckEnabled = configurationsChangeListener.getDownwardAck();
        LOGGER.debug("Received mediation task requests count:{} and action: {} and operator :{} and is downwardAck enabled:{}",
                alarmActionInformations.size(), alarmAction, operatorName, isDownwordAckEnabled);
        if (isDownwordAckEnabled) {
            final Map<String, String> mtrAttributes = prepareMediationTaskRequest(alarmActionInformations);
            LOGGER.debug("MTR attributes data to sent to mediation:{}", mtrAttributes);
            if (!mtrAttributes.isEmpty()) {
                prepareAndSendMediationRequestForAlarmAction(mtrAttributes, operatorName, alarmAction);
            }
        }
    }

    /**
     * Method takes all the failed alarm actions information and if downward ack enabled, prepare mediation task request for eligible actions and send
     * to mediation services.
     * @param alarmActionInformations
     *            list of alarm actions information.
     */
    public void prepareAndSendFailedActionsMediationTaskRequest(final List<AlarmActionInformation> alarmActionInformations) {
        final boolean isDownwordAckEnabled = configurationsChangeListener.getDownwardAck();
        LOGGER.debug("Received mediation task request data for failed Actions:{} and is downwardAck enabled:{}", alarmActionInformations,
                isDownwordAckEnabled);
        if (isDownwordAckEnabled) {
            final Map<String, Map<String, String>> mtrAttributes = new HashMap<String, Map<String, String>>();
            if (!alarmActionInformations.isEmpty()) {
                mtrAttributes.putAll(prepareFailedActionsMediationTaskRequest(alarmActionInformations));
            }
            LOGGER.debug("Failed Actions MTR attributes data to sent to mediation:{}", mtrAttributes);
            if (!mtrAttributes.isEmpty()) {
                for (final Entry<String, Map<String, String>> failedActionMtrAttributes : mtrAttributes.entrySet()) {
                    final String key = failedActionMtrAttributes.getKey();
                    final String[] actionAndOperatorName = key.split(HASH_DELIMITER);
                    final String alarmAction = actionAndOperatorName[0];
                    final String operatorName = actionAndOperatorName[1];
                    prepareAndSendMediationRequestForAlarmAction(failedActionMtrAttributes.getValue(), operatorName, alarmAction);
                }
            }
        }
    }

    /**
     * Converts additional information to a map of additional attribute name and value as key and value respectively.
     * @param additionalInformationString
     *            The additional information string of the alarm.
     * @return The map containing additional attributes name and value.
     */
    public static Map<String, String> getAdditionalInformation(final String additionalInformationString) {
        final Map<String, String> additionalInformation = new HashMap<String, String>();
        if (additionalInformationString != null && !additionalInformationString.isEmpty()) {
            final String[] attributes = additionalInformationString.split(HASH_DELIMITER);
            for (final String attribute : attributes) {
                // Splits string into key and value .This holds good even in case of value containing ":"
                final String[] keyValue = attribute.split(COLON_DELIMITER, 2);
                if (keyValue.length == 1) {
                    additionalInformation.put(keyValue[0], null);
                } else {
                    additionalInformation.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return additionalInformation;
    }

    /**
     * Sends mediation task request to event based mediation client queue.
     * @param fdn
     *            fdn of the node for which the ack request needs to be sent.
     * @param alarmIdListString
     *            The alarm id's for which ack needs to be performed.
     * @param alarmIdSize
     *            The alarm id's size for which ack needs to be performed.
     * @param action
     *            The alarm action either ack or unack.
     * @param operatorName
     *            The operator name performing alarm ack/unack operation.
     */
    public void sendAckRequest(final String fdn, final String alarmIdListString, final int alarmIdSize, final String action,
            final String operatorName) {
        final Random randomGenerator = new Random();
        final FmMediationAckRequest fmMediationAckRequest = new FmMediationAckRequest();
        if (AlarmAction.ACK.name().equals(action)) {
            fmMediationAckRequest.setStrAckStatus(ACKNOWLEDGE);
        } else {
            fmMediationAckRequest.setStrAckStatus(UNACKNOWLEDGE);
        }
        fmMediationAckRequest.setOperatorName(operatorName);
        fmMediationAckRequest.setAlarmIds(alarmIdListString);
        fmMediationAckRequest.setAlarmIdSize(alarmIdSize);
        final Integer randomInt = randomGenerator.nextInt(1000);
        fmMediationAckRequest.setJobId("" + randomInt.toString());
        fmMediationAckRequest.setProtocolInfo(OperationType.FM.toString());
        fmMediationAckRequest.setNodeAddress(fdn.concat(FMALARM_SUPERVISION_MO_SUFFIX));
        fmMediationAckRequest.setClientType(MediationClientType.EVENT_BASED.name());

        try {
            mediationTaskSender.send(fmMediationAckRequest);
            LOGGER.debug("{} request sent to mediation successfully {} ", action, fmMediationAckRequest);
        } catch (final Exception exception) {
            LOGGER.error("Exception while sending ack request for fdn {} is  ", fdn, exception);
        }
    }

    /**
     * Sends mediation task request to event based mediation client queue.
     * @param fdn
     *            fdn of the node for which the clear request needs to be sent.
     * @param alarmIdListString
     *            The alarm id's for which clear needs to be performed.
     * @param alarmIdSize
     *            The alarm id's size for which clear needs to be performed.
     * @param operatorName
     *            The operator name performing alarm clear operation.
     */
    public void sendClearRequest(final String fdn, final String alarmIdListString, final int alarmIdSize, final String operatorName) {
        final Random randomGenerator = new Random();
        final FmMediationClearRequest fmMediationClearRequest = new FmMediationClearRequest();
        fmMediationClearRequest.setOperatorName(operatorName);
        fmMediationClearRequest.setAlarmIds(alarmIdListString);
        fmMediationClearRequest.setAlarmIdSize(alarmIdSize);
        final Integer randomInt = randomGenerator.nextInt(1000);
        fmMediationClearRequest.setJobId("" + randomInt.toString());
        fmMediationClearRequest.setProtocolInfo(OperationType.FM.toString());
        fmMediationClearRequest.setNodeAddress(fdn.concat(FMALARM_SUPERVISION_MO_SUFFIX));
        fmMediationClearRequest.setClientType(MediationClientType.EVENT_BASED.name());

        try {
            mediationTaskSender.send(fmMediationClearRequest);
            LOGGER.debug("Clear request sent to mediation successfully {} ", fmMediationClearRequest);
        } catch (final Exception exception) {
            LOGGER.error("Exception while sending clear request for fdn {} is  ", fdn, exception);
        }
    }

    /**
     * Method converts list of alarm id's to a String with delimiter as '\u0000'.
     * @param alarmIdList
     *            The list of alarm ids.
     * @return String with containing delimiter as '\u0000'.
     */
    public String setAlarmIdsAsString(final List<String> alarmIdList) {
        String alarmIdString = "";
        int firstpart = 1;
        for (final String alarmId : alarmIdList) {
            if (firstpart == 1) {
                alarmIdString += alarmId;
                firstpart = 0;
            } else {
                alarmIdString += NULL_CHARACTER_DELIMITER + alarmId;
            }
        }
        LOGGER.debug("received alarmIdList:{} and final alarmIds string:{}", alarmIdList, alarmIdString);
        return alarmIdString;
    }

    /**
     * Method Segregates unique FDN with their respective Alarm Numbers and sends Mediation Task Request.
     * @param mtrAttributeMap
     *            The map containing details of alarm IDs and corresponding FDN for sending ACK request to mediation.
     * @param operatorName
     *            The operator name performed alarm acknowledgment operation.
     */
    public void prepareAndSendMediationRequestForAlarmAction(final Map<String, String> mtrAttributeMap, final String operatorName,
            final String alarmAction) {
        LOGGER.debug("Received mediationtask request details:{} and operatorName:{} and alarmAction:{}", mtrAttributeMap, operatorName, alarmAction);

        if (mtrAttributeMap != null && !mtrAttributeMap.isEmpty()) {
            final List<String> alarmIds = new ArrayList<String>(mtrAttributeMap.size());
            final Set<String> fdnSet = new TreeSet<String>(mtrAttributeMap.values());
            for (final String fdnKey : fdnSet) {
                for (final Entry<String, String> entry : mtrAttributeMap.entrySet()) {
                    if (fdnKey.equals(entry.getValue())) {
                        LOGGER.debug("The current fdn key is: {}", entry.getKey());
                        final StringTokenizer tokenizer = new StringTokenizer(entry.getKey(), "\u0000");
                        // Taking first element always as we always add fdn as first element.
                        alarmIds.add((String) tokenizer.nextElement());
                    }
                }
                LOGGER.debug("For the FDN : {} Associated Ids are : {} idlist Size : {} and with operatorName: {} as", fdnKey, alarmIds,
                        alarmIds.size(), operatorName);
                if (alarmAction.equals(CLEAR)) {
                    sendClearRequest(fdnKey, setAlarmIdsAsString(alarmIds), alarmIds.size(),
                            operatorName);
                } else {
                    sendAckRequest(fdnKey, setAlarmIdsAsString(alarmIds), alarmIds.size(),
                            alarmAction, operatorName);
                }
                alarmIds.clear();
            }
        }
    }

    private String getNodeAlarmId(final Map<String, String> additionalInformation) {
        return additionalInformation.containsKey(ALARMID) ? additionalInformation.get(ALARMID)
                : (additionalInformation.containsKey(EXTERNAL_EVENTID) ? additionalInformation.get(EXTERNAL_EVENTID) : additionalInformation
                        .get(GENERATED_ALARMID));
    }

    private Map<String, String> prepareMtrAttributes(final AlarmActionInformation alarmActionInformation) {
        final Map<String, String> mtrAttributes = new HashMap<String, String>();
        final Map<String, Object> alarmAttributes = alarmActionInformation.getAlarmAttributes();
        final String alarmAction = alarmActionInformation.getAlarmAction();
        final String info = (String) alarmAttributes.get(ADDITIONAL_INFORMATION);
        final Map<String, String> additionalInformation = getAdditionalInformation(info);
        final String alarmId = getNodeAlarmId(additionalInformation);
        final String sourceType = additionalInformation.get(SOURCETYPE);
        final String neFdn = alarmAttributes.get(FDN).toString();
        if (alarmId != null && !alarmId.isEmpty() && sourceType != null) {
            if (AlarmAction.ACK.name().equals(alarmAction)) {
                if (downwardOperationSupportedHelperBean.isDownwardOperationSupported(sourceType,
                        ModelCapabilitiesConstants.ALARM_ACK_CAPABILITY)) {
                    mtrAttributes.put(alarmId + '\u0000' + neFdn, neFdn);
                }
            } else if (AlarmAction.UNACK.name().equals(alarmAction)) {
                if (downwardOperationSupportedHelperBean.isDownwardOperationSupported(sourceType,
                        ModelCapabilitiesConstants.ALARM_UNACK_CAPABILITY)) {
                    mtrAttributes.put(alarmId + '\u0000' + neFdn, neFdn);
                }
            } else if (AlarmAction.CLEAR.name().equals(alarmAction)) {
                if (downwardOperationSupportedHelperBean.isDownwardOperationSupported(sourceType,
                        ModelCapabilitiesConstants.ALARM_CLEAR_CAPABILITY)) {
                    mtrAttributes.put(alarmId + '\u0000' + neFdn, neFdn);
                }
            }
        }
        return mtrAttributes;
    }

    private Map<String, Map<String, String>> prepareFailedActionsMediationTaskRequest(final List<AlarmActionInformation> alarmActionInformations) {
        final Map<String, Map<String, String>> mtrAttributes = new HashMap<String, Map<String, String>>();
        for (final AlarmActionInformation alarmActionInformation : alarmActionInformations) {
            final String alarmAction = alarmActionInformation.getAlarmAction();
            final String operatorName = alarmActionInformation.getOperatorName();
            final String key = new StringBuilder("").append(alarmAction).append(HASH_DELIMITER).append(operatorName).toString();
            if (!COMMENT.equals(alarmAction)) {
                final Map<String, String> oldMtrAttribureInMap = mtrAttributes.get(key);
                final Map<String, String> newMtrAttributes = prepareMtrAttributes(alarmActionInformation);
                if (!newMtrAttributes.isEmpty()) {
                    if (null != oldMtrAttribureInMap) {
                        oldMtrAttribureInMap.putAll(newMtrAttributes);
                        mtrAttributes.put(key, oldMtrAttribureInMap);
                    } else {
                        mtrAttributes.put(key, newMtrAttributes);
                    }
                }
            }
        }
        return mtrAttributes;
    }

    private Map<String, String> prepareMediationTaskRequest(final List<AlarmActionInformation> alarmActionInformations) {
        final Map<String, String> mtrAttributes = new HashMap<String, String>();
        for (final AlarmActionInformation alarmActionInformation : alarmActionInformations) {
            mtrAttributes.putAll(prepareMtrAttributes(alarmActionInformation));
        }
        return mtrAttributes;
    }
}
