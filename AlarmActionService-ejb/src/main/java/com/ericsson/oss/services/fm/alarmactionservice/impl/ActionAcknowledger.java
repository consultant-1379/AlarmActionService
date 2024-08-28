/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKSTATE_CHANGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSUPPRESSEDALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSUPPRESSEDSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSUPPRESSED_SP;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_ACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAREDUNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COLON_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTALARMOPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NO_ALARMS_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.RECORDTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.REPEATED_ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SPECIFICPROBLEM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TECHNICIANPRESENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TECHNICIANPRESENTSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TECHNICIANPRESENT_SP;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNACK;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.StringMatchCondition;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.services.alarm.action.service.instrumentation.AASInstrumentedBean;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * This class performs Alarm Acknowledge Operation. It will perform ActionAcknowledge Operation for Error,Alarm.
 * It will perform ActionAcknowledge Operation with following inputs. <br>
 * 1. FDN <br>
 * 2. FDN and AlarmIDs <br>
 * 3. AlarmIDs.
 **/
@Stateless
public class ActionAcknowledger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionAcknowledger.class);

    @Inject
    private DpsUtil dpsUtil;

    @Inject
    private AASInstrumentedBean aasInstrumentedBean;

    @Inject
    private AlarmActionUtils alarmActionUtils;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    public List<AlarmActionInformation> performAckWithoutBatching(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String objectOfReference = alarmActionData.getObjectOfReference();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        if (isNotBlank(objectOfReference)) {
            if (alarmIdList != null && !alarmIdList.isEmpty()) {
                alarmActionInformations = performAckWithObjectOfReferenceAndPoIds(alarmActionData, alarmActionResponses);
            } else {
                alarmActionInformations = performAckWithObjectOfReference(alarmActionData, alarmActionResponses);
            }
        } else {
            alarmActionInformations = performAckWithPoIds(alarmActionData, alarmActionResponses);
        }
        return alarmActionInformations;
    }

    /**
     * Method performs alarm ack operation on given poIds.
     *
     * @param operatorName
     *            ack requested operator name.
     * @param poIds
     *            ack requested poIds.
     * @param alarmActionResponses
     *            response for any alarm action.
     * @return list of AlarmActionInformation.
     */
    public List<AlarmActionInformation> processAckForSingleBatch(final String operatorName, final List<Long> poIds,
            final List<AlarmActionResponse> alarmActionResponses) {
        final List<Long> requestedPoIds = new ArrayList<Long>(poIds);
        final List<Long> processedPoIds = new ArrayList<Long>();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();

        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(poIds);

        for (final PersistenceObject persistenceObject : persistenceObjects) {
            final Long poId = persistenceObject.getPoId();
            processedPoIds.add(poId);
            final String alarmAction = AlarmAction.ACK.name();
            final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);

            final Map<String, String> actionResponse = performAck(persistenceObject, alarmActionInformation, liveBucket);
            if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                alarmActionsCacheManager.put(alarmActionInformation);
                alarmActionInformations.add(alarmActionInformation);
            }
            alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
        }
        // With this code, we are verifying ack on cleared alarms available in cache.
        requestedPoIds.removeAll(processedPoIds);
        if (!requestedPoIds.isEmpty()) {
            alarmActionResponses.addAll(verifyAndReturnResponseForFailedAck(operatorName, requestedPoIds, alarmActionInformations));
            final List<AlarmActionResponse> failedActionResponses = AlarmActionUtils.buildResponseForInvalidPoIds(requestedPoIds);
            alarmActionResponses.addAll(failedActionResponses);
        }
        return alarmActionInformations;
    }

    /**
     * Method checks the alarm whether is Alarm or Error , sends to respective methods.
     *
     * @Method : processAlarm
     * @param non
     *            -null PersistenceObject, liveBucket and Strings like FDN and OperatorName
     * @return List of HashMaps contains response and responseForMediation
     **/
    public Map<String, String> acknowledgeErrorMessage(final PersistenceObject persistenceObject,
            final AlarmActionInformation alarmActionInformation, final DataBucket liveBucket) {
        final Map<String, String> actionResponse = new HashMap<String, String>(5);
        final Long poId = alarmActionInformation.getPoId();
        final String operatorName = alarmActionInformation.getOperatorName();
        alarmActionInformation.getAlarmAction();

        final Map<String, Object> alarmMap = persistenceObject.getAllAttributes();
        alarmMap.put(LASTALARMOPERATION, ACKSTATE_CHANGE);
        alarmMap.put(ACKOPERATOR, operatorName);
        final Date ackTime = new Date();
        alarmMap.put(ACKTIME, ackTime);
        alarmMap.put(ALARMSTATE, ACK);
        alarmMap.put(LASTUPDATED, ackTime);

        persistenceObject.setAttributes(alarmMap);
        alarmMap.put(EVENTPOID, poId);
        alarmActionUtils.updateLastDeliveredTime(alarmMap);

        actionResponse.put(
                persistenceObject.getAttribute(OBJECTOFREFERENCE).toString() + COLON_DELIMITER
                        + persistenceObject.getAttribute(ALARMNUMBER).toString() + COLON_DELIMITER + poId.toString(), SUCCESS);

        liveBucket.deletePo(persistenceObject);
        alarmActionInformation.setAlarmAttributes(alarmMap);
        LOGGER.debug("Error Alarm with PoId: {}  is Acknowledged and Removed from DPS ", poId);
        return actionResponse;
    }

    /**
     * Method checks for recordType of an alarm and sends to respective methods based on it.
     *
     * @param persistenceObject
     *            - enumeration of the data fetched from DB
     * @param operatorName
     *            - Name of the Operator performing this Action
     * @param liveBucket
     *            - Instance of the DB that used to perform the DB operations
     **/
    private Map<String, String> performAck(final PersistenceObject persistenceObject, final AlarmActionInformation alarmActionInformation,
            final DataBucket liveBucket) {
        Map<String, String> actionResponse = new HashMap<String, String>();
        final String recordType = persistenceObject.getAttribute(RECORDTYPE);
        if (ERROR_MESSAGE.equals(recordType) || REPEATED_ERROR_MESSAGE.equals(recordType)) {
            actionResponse = acknowledgeErrorMessage(persistenceObject, alarmActionInformation, liveBucket);
        } else {
            actionResponse = processAck(persistenceObject, alarmActionInformation, liveBucket);
        }
        return actionResponse;
    }

    /**
     * Method process the Ack operation based on the Alarm State.
     *
     * @Method : processAck
     * @param non
     *            -null PersistenceObject and Strings like FDN and OperatorName
     * @return List of HashMaps contains response and responseForMediation
     **/
    private Map<String, String> processAck(final PersistenceObject persistenceObject, final AlarmActionInformation alarmActionInformation,
            final DataBucket liveBucket) {
        final Long poId = alarmActionInformation.getPoId();
        final String operatorName = alarmActionInformation.getOperatorName();
        final Map<String, String> actionResponse = new HashMap<String, String>();
        final Map<String, Object> alarmAttibutes = new HashMap<String, Object>();
        final Long alarmNumber = persistenceObject.getAttribute(ALARMNUMBER);
        final String alarmState = persistenceObject.getAttribute(ALARMSTATE);
        final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
        LOGGER.debug("Current  AlarmState of PO:{} is: {} ", poId, alarmState);
        if (ACK.equals(alarmState)) {
            final AlarmActionInformation alarmActionDetailsInCache = alarmActionsCacheManager.get(alarmActionInformation);
            if (alarmActionDetailsInCache != null) {
                actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId, SUCCESS);
                alarmAttibutes.putAll(alarmActionDetailsInCache.getAlarmAttributes());
            } else {
                actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId, ALREADY_ACK);
                LOGGER.debug("Alarm  with Po ID: {} is Already Acknowledged", poId);
            }
        } else if (CLEAREDUNACK.equals(alarmState)) {
            alarmAttibutes.putAll(processAlarmAction(actionResponse, persistenceObject, operatorName, alarmState));
            liveBucket.deletePo(persistenceObject);
        } else if (UNACK.equals(alarmState)) {
            final Date ackTime = setAlarmAttrForAck(persistenceObject, operatorName);
            LOGGER.debug("updated UNACK and lastpUdated updated to: {} ", ackTime);
            alarmAttibutes.putAll(processAlarmAction(actionResponse, persistenceObject, operatorName, alarmState));
            LOGGER.debug("Successfully updated the alarmState of alarm to: {} with  PoId: {} ", ACK, persistenceObject.getPoId());
        }
        alarmActionInformation.setAlarmAttributes(alarmAttibutes);
        return actionResponse;
    }

    private Date setAlarmAttrForAck(final PersistenceObject persistenceObject, final String operatorName) {
        final Date ackTime = new Date();
        final Map<String, Object> attributeMap = new HashMap<String, Object>(5);
        attributeMap.put(ALARMSTATE, ACK);
        attributeMap.put(ACKOPERATOR, operatorName);
        attributeMap.put(ACKTIME, ackTime);
        attributeMap.put(LASTUPDATED, ackTime);
        attributeMap.put(LASTALARMOPERATION, ACKSTATE_CHANGE);
        persistenceObject.setAttributes(attributeMap);
        return ackTime;
    }

    private Map<String, Object> processAlarmAction(final Map<String, String> actionResponse, final PersistenceObject persistenceObject,
            final String operatorName, final String prevAlarmState) {
        final Map<String, Object> alarmMap = persistenceObject.getAllAttributes();
        final Long alarmNumber = (Long) alarmMap.get(ALARMNUMBER);
        final String objectOfReference = (String) alarmMap.get(OBJECTOFREFERENCE);
        final String neFdn = (String) alarmMap.get(FDN);
        final String recordType = (String) alarmMap.get(RECORDTYPE);
        final Long poId = persistenceObject.getPoId();
        final String specificProblem = (String) alarmMap.get(SPECIFICPROBLEM);
        final Date ackTime = new Date();

        LOGGER.debug("processAlarmActionAndSendEvent with AlarmState {}, recordType {} and SP {}", prevAlarmState, recordType, specificProblem);
        if (UNACK.equalsIgnoreCase(prevAlarmState)) {
            alarmMap.put(EVENTPOID, poId);
        } else if (CLEAREDUNACK.equalsIgnoreCase(prevAlarmState)) {
            populateAlarmMap(ackTime, poId, operatorName, alarmMap, persistenceObject);
            // TORF-166531 - Clear on FMX Updated TechnicianPresent and AlarmSupressedMode alarms should also update the FmFunction
            // So added the below || to check the SP value of that alarm.
            if (ALARMSUPPRESSEDALARM.equalsIgnoreCase(recordType) || ALARMSUPPRESSED_SP.equals(specificProblem)) {
                dpsUtil.updateFmFunction(neFdn, ALARMSUPPRESSEDSTATE);
            } else if (TECHNICIANPRESENT.equalsIgnoreCase(recordType) || TECHNICIANPRESENT_SP.equals(specificProblem)) {
                dpsUtil.updateFmFunction(neFdn, TECHNICIANPRESENTSTATE);
            }
        }
        alarmActionUtils.updateLastDeliveredTime(alarmMap);
        aasInstrumentedBean.increasAckalarmCount();
        actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId, SUCCESS);
        return alarmMap;
    }

    private void populateAlarmMap(final Date ackTime, final Long alarmId, final String operatorName, final Map<String, Object> alarmMap,
            final PersistenceObject persistenceObject) {
        alarmMap.put(LASTALARMOPERATION, ACKSTATE_CHANGE);
        alarmMap.put(ACKOPERATOR, operatorName);
        alarmMap.put(ACKTIME, ackTime);
        alarmMap.put(ALARMSTATE, CLEARED_ACKNOWLEDGED);
        alarmMap.put(LASTUPDATED, ackTime);
        persistenceObject.setAttributes(alarmMap);
        alarmMap.put(EVENTPOID, alarmId);
    }

    private List<AlarmActionInformation> performAckWithObjectOfReference(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>(100);
        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final String operatorName = alarmActionData.getOperatorName();
        final String objectOfReference = alarmActionData.getObjectOfReference();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final Restriction oorRestriction = typeQuery.getRestrictionBuilder().matchesString(OBJECTOFREFERENCE, objectOfReference,
                StringMatchCondition.CONTAINS);

        typeQuery.setRestriction(oorRestriction);
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);

        if (poListIterator == null || !poListIterator.hasNext()) {
            LOGGER.debug("No Alarms found for FDN: {}", objectOfReference);
            alarmActionResponses.add(AlarmActionUtils.setActionResponse(NO_ALARMS_UNDERFDN, objectOfReference, "", ""));
        } else {
            while (poListIterator.hasNext()) {
                final PersistenceObject persistenceObject = poListIterator.next();
                final Long poId = persistenceObject.getPoId();
                final AlarmActionInformation actionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
                final Map<String, String> actionResponse = performAck(persistenceObject, actionInformation, liveBucket);
                if (null != actionInformation.getAlarmAttributes() && !actionInformation.getAlarmAttributes().isEmpty()) {
                    alarmActionsCacheManager.put(actionInformation);
                    alarmActionInformations.add(actionInformation);
                }
                alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
            }
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> performAckWithObjectOfReferenceAndPoIds(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        final List<Long> successfulAlarmIds = new ArrayList<Long>(alarmIdList.size());
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        if (!alarmIdList.isEmpty()) {
            final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);

            for (final PersistenceObject persistenceObject : persistenceObjects) {
                final Long poId = persistenceObject.getPoId();
                successfulAlarmIds.add(poId);
                final String poType = persistenceObject.getType();
                if (!OPENALARM.equals(poType)) {
                    LOGGER.info(" Alarm with PoId: {} Not Found under the FDN: {} ", persistenceObject.getPoId(), inputObjectOfReference);
                    alarmActionResponses
                            .add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", poId.toString()));
                } else {
                    final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
                    if (objectOfReference.contains(inputObjectOfReference)) {
                        final AlarmActionInformation actionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName,
                                poId);
                        final Map<String, String> actionResponse = performAck(persistenceObject, actionInformation, liveBucket);
                        if (null != actionInformation.getAlarmAttributes() && !actionInformation.getAlarmAttributes().isEmpty()) {
                            alarmActionsCacheManager.put(actionInformation);
                            alarmActionInformations.add(actionInformation);
                        }
                        alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
                    } else {
                        LOGGER.info("Alarm with PoId: {} not found under the FDN: {} ", poId, inputObjectOfReference);
                        alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "",
                                poId.toString()));
                    }
                }
            }
        }
        if (alarmIdList.size() != successfulAlarmIds.size()) {
            alarmIdList.removeAll(successfulAlarmIds);
            LOGGER.debug("Alarms with PoIds: {} Not Found", alarmIdList.size());
            for (final Long poId : alarmIdList) {
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, "", "", poId.toString()));
                aasInstrumentedBean.increaseFailedAckAlarms();
            }
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> performAckWithPoIds(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String operatorName = alarmActionData.getOperatorName();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>(alarmIdList.size());
        final List<Long> processedPoIds = new ArrayList<Long>(alarmIdList.size());

        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        if (!alarmIdList.isEmpty()) {
            final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
            for (final PersistenceObject persistenceObject : persistenceObjects) {
                final Long alarmId = persistenceObject.getPoId();
                final String poType = persistenceObject.getType();
                processedPoIds.add(alarmId);
                if (!OPENALARM.equals(poType)) {
                    LOGGER.debug(" Alarm with PoID : {} Not Found ", alarmId);
                    alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", alarmId.toString()));
                } else {
                    final String alarmAction = alarmActionData.getAlarmAction().name();
                    final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName,
                            alarmId);
                    final Map<String, String> actionResponse = performAck(persistenceObject, alarmActionInformation, liveBucket);
                    if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                        alarmActionsCacheManager.put(alarmActionInformation);
                        alarmActionInformations.add(alarmActionInformation);
                    }
                    alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
                }
            }
        }
        if (alarmIdList.size() != processedPoIds.size()) {
            alarmIdList.removeAll(processedPoIds);
            LOGGER.debug("Alarms with PoIds: {} Not Found", alarmIdList.size());
            for (final Long poId : alarmIdList) {
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString()));
                aasInstrumentedBean.increaseFailedAckAlarms();
            }
        }
        return alarmActionInformations;
    }

    /**
     * Method takes failed poIds and verifies the cache with failed poIds and include those actions information to the responses list.
     * Here,in this case ack on clear alarms not there in DB but in cache will be included to current responses list.
     *
     * @param operatorName
     *            operator name
     * @param requestedPoIds
     *            total failed poIds in DB operation.
     * @param alarmActionInformations
     *            list of alarm actions information for the current request.
     * @return List of AlarmActionResponse.
     */
    private List<AlarmActionResponse> verifyAndReturnResponseForFailedAck(final String operatorName, final List<Long> requestedPoIds,
            final List<AlarmActionInformation> alarmActionInformations) {
        final List<AlarmActionResponse> alarmActionResponses = new ArrayList<AlarmActionResponse>();
        final List<Long> failedClearAction = new ArrayList<Long>();
        for (final Long poId : requestedPoIds) {
            final String alarmAction = AlarmAction.ACK.name();
            final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
            final AlarmActionInformation alarmActionDetailsInCache = alarmActionsCacheManager.get(alarmActionInformation);
            if (alarmActionDetailsInCache != null) {
                failedClearAction.add(poId);
                alarmActionInformations.add(alarmActionDetailsInCache);
                final Map<String, Object> alarmAttributes = alarmActionDetailsInCache.getAlarmAttributes();
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(SUCCESS, alarmAttributes.get(OBJECTOFREFERENCE).toString(),
                        alarmAttributes.get(ALARMNUMBER).toString(), poId.toString()));
            }
        }
        requestedPoIds.removeAll(failedClearAction);
        return alarmActionResponses;
    }
}
