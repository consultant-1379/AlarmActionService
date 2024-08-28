/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.util;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COLON_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HASH_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LAST_DELIVERED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.util.ServiceIdentity;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.BatchingParameters;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedEventState;

/**
 * Utility class for alarm action service.
 */
@ApplicationScoped
public class AlarmActionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionUtils.class);

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    @Inject
    private ServiceIdentity serviceIdentity;

    /**
     * Builds the response for the invalid poIds given in request.
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @param matchedPoIds
     *            - PoIds on which valid POs present in DB
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */

    public static List<AlarmActionResponse> buildResponseForInvalidPoIds(final List<Long> poIds, final List<Long> matchedPoIds) {
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        if (poIds.size() != matchedPoIds.size()) {
            poIds.removeAll(matchedPoIds);
            for (final Long poId : poIds) {
                final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString());
                actionResponses.add(alarmActionResponse);
            }
        }
        return actionResponses;
    }

    /**
     * Builds the response for the invalid poIds given in request.
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */

    public static List<AlarmActionResponse> buildResponseForInvalidPoIds(final List<Long> poIds) {
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        for (final Long poId : poIds) {
            final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString());
            actionResponses.add(alarmActionResponse);
        }
        return actionResponses;
    }

    public static AlarmActionResponse buildAlarmActionResponse(final Map<String, String> actionResponse) {
        AlarmActionResponse alarmActionResponse = null;
        for (final String key : actionResponse.keySet()) {
            final String[] alarmAttributes = key.split(COLON_DELIMITER);
            alarmActionResponse =
                    setActionResponse(actionResponse.get(key), alarmAttributes[0], alarmAttributes[1], alarmAttributes[2]);
        }

        return alarmActionResponse;
    }

    /**
     * Builds the response for the PoIds on which the specified action request failed to execute.
     * @param openAlarmRecord
     *            - alarm record persistence object on which the actions has to be performed
     * @param openAlarmRecord
     *            - alarm record persistence object on which the actions has to be performed
     * @param exceptionMessage
     *            - exception message that need to be set as alarm action response
     * @return {@link AlarmActionResponse} - The encapsulated response data for the action request
     */
    public static AlarmActionResponse buildAlarmActionResponse(final Map<String, Object> openAlarmRecord, final String exceptionMessage) {
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setObjectOfReference((String) openAlarmRecord.get(OBJECTOFREFERENCE));
        alarmActionResponse.setResponse(exceptionMessage);
        alarmActionResponse.setEventPoId(openAlarmRecord.get(EVENTPOID).toString());
        alarmActionResponse.setAlarmNumber(openAlarmRecord.get(ALARMNUMBER).toString());
        return alarmActionResponse;
    }

    public static AlarmActionResponse setActionResponse(final String response, final String objectOfReference, final String alarmNumber,
            final String poId) {
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setResponse(response);
        alarmActionResponse.setObjectOfReference(objectOfReference);
        alarmActionResponse.setEventPoId(poId);
        alarmActionResponse.setAlarmNumber(alarmNumber);
        return alarmActionResponse;
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

    public AlarmActionInformation prepareAlarmActionInformation(final String alarmAction, final String operatorName, final Long poId) {
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setAlarmAction(alarmAction);
        alarmActionInformation.setOperatorName(operatorName);
        alarmActionInformation.setJbossNodeId(serviceIdentity.getNodeId());
        alarmActionInformation.setPoId(poId);
        return alarmActionInformation;
    }

    public BatchingParameters createBatchigParameters() {
        final BatchingParameters batchingParameters = new BatchingParameters();
        batchingParameters.setAckCycleTimeUtilization(configurationsChangeListener.getDelayedAckCycleTimeUtilization());
        batchingParameters.setBatchTimerTimeout(configurationsChangeListener.getDelayedAckBatchTimerTimeOut());
        batchingParameters.setCheckFrequency(configurationsChangeListener.getCheckfrequency());
        batchingParameters.setThresholdEventBatchSize(configurationsChangeListener.getThresholdEventBatchSize());
        return batchingParameters;
    }

    public void updateLastDeliveredTime(final Map<String, Object> alarmMap) {
        final Date lastUpdatedTime = (Date) alarmMap.get(LASTUPDATED);
        if (alarmMap.get(LAST_DELIVERED) != null) {
            try {
                long lastDeliveredTime = (long) alarmMap.get(LAST_DELIVERED);
                boolean clearAlarm = false;
                final String alarmState = (String) alarmMap.get(ALARMSTATE);
                if (alarmState.equals(ProcessedEventState.CLEARED_ACKNOWLEDGED.name())
                        || alarmState.equals(ProcessedEventState.CLEARED_UNACKNOWLEDGED.name())) {
                    clearAlarm = true;
                }
                final Long diffTimeInMilliSeconds = Math.abs(lastDeliveredTime - lastUpdatedTime.getTime());
                if (diffTimeInMilliSeconds < configurationsChangeListener.getAlarmThresholdInterval()) {
                    if (clearAlarm) {
                        lastDeliveredTime = lastUpdatedTime.getTime() + configurationsChangeListener.getClearAlarmDelayToQueue();
                    } else {
                        lastDeliveredTime = lastUpdatedTime.getTime() + configurationsChangeListener.getAlarmDelayToQueue();
                    }
                } else {
                    lastDeliveredTime = lastUpdatedTime.getTime();
                }
                LOGGER.debug("lastDeliveredTime is {} lastUpdatedTime is {} diffTimeInMillSeconds is {},isClearAlarm is {} ", lastDeliveredTime,
                        lastUpdatedTime, diffTimeInMilliSeconds, clearAlarm);
                alarmMap.put(LAST_DELIVERED, lastDeliveredTime);
            } catch (final NumberFormatException numberFormatException) {
                LOGGER.error("Excpetion occured for LAST DELIVERED TIME returning timeToDeliver as NULL : ", numberFormatException);
                alarmMap.put(LAST_DELIVERED, lastUpdatedTime.getTime());
            }
        } else {
            alarmMap.put(LAST_DELIVERED, lastUpdatedTime.getTime());
        }
    }

}
