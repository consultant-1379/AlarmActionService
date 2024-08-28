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
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSUPPRESSEDALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSUPPRESSEDSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSUPPRESSED_SP;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASEOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASETIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAREDUNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COLON_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR_CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN_MANDATORY_CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTALARMOPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MANUALCEASE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NO_ALARMS_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PRESENTSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PREVIOUSSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PSEUDO_PRESENT_SEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PSEUDO_PREVIOUS_SEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.RECORDTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.REPEATED_ERROR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.REPEATED_ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SPECIFICPROBLEM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TECHNICIANPRESENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TECHNICIANPRESENTSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TECHNICIANPRESENT_SP;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNDERSCORE_DELIMITER;

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
import com.ericsson.oss.itpf.datalayer.dps.query.RestrictionBuilder;
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
import com.ericsson.oss.services.fm.util.PseudoSeverities;

/**
 * Utility class for performing alarm clear operation.
 */
@Stateless
public class ActionClearer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionClearer.class);

    @Inject
    private DpsUtil dpsUtil;

    @Inject
    private AASInstrumentedBean aasInstrumentedBean;

    @Inject
    private AlarmActionUtils alarmActionUtils;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    //TODO: Initializing actionResponse for now.Check caller if null and remove actionResponse initialization.
    public List<AlarmActionInformation> performClear(final AlarmActionData alarmActionData, final List<AlarmActionResponse> alarmActionResponses) {
        final String objectOfReference = alarmActionData.getObjectOfReference();
        final List<Long> alarmNumbers = alarmActionData.getAlarmIds();
        List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();

        if (isNotBlank(objectOfReference)) {
            if (alarmNumbers != null && !alarmNumbers.isEmpty()) {
                alarmActionInformations = performClearWithObjectOfReferenceAndAlarmNumbers(alarmActionData, alarmActionResponses);
            } else {
                alarmActionInformations = performClearWithObjectOfReference(alarmActionData, alarmActionResponses);
            }
        } else {
            for (final Long alarmNumber : alarmNumbers) {
                LOGGER.debug("FDN is mandatory for clear operation {} ", alarmNumber);
                final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(FDN_MANDATORY_CLEAR, "", alarmNumber.toString(),
                        "");
                alarmActionResponses.add(alarmActionResponse);
            }
        }
        return alarmActionInformations;
    }

    public List<AlarmActionResponse> performClearforMultipleFdns(final AlarmActionData alarmActionData,
            final List<AlarmActionInformation> alarmActionInformations) {
        final Map<String, List<Long>> oorAndAlarmIds = new HashMap<String, List<Long>>();
        oorAndAlarmIds.putAll(alarmActionData.getClearFdnList());
        final List<AlarmActionResponse> alarmActionResponses = new ArrayList<AlarmActionResponse>(oorAndAlarmIds.size());
        final String operatorNameforMultipleClear = alarmActionData.getOperatorName();
        LOGGER.debug("Operator: {} performed the CLEAR for multiple fdns ", operatorNameforMultipleClear);
        final String operator = alarmActionData.getOperatorName();
        for (final String oor : oorAndAlarmIds.keySet()) {
            final List<Long> alarmIds = oorAndAlarmIds.get(oor);
            final AlarmActionData newAlarmActionData = new AlarmActionData();
            newAlarmActionData.setObjectOfReference(oor);
            newAlarmActionData.setAlarmIds(alarmIds);
            newAlarmActionData.setAction(AlarmAction.CLEAR);
            newAlarmActionData.setOperatorName(operator);
            alarmActionInformations.addAll(performClearWithObjectOfReferenceAndAlarmNumbers(newAlarmActionData, alarmActionResponses));
        }
        return alarmActionResponses;
    }

    public List<AlarmActionInformation> clear(final String operatorName, final List<Long> poIds,
            final List<AlarmActionResponse> alarmActionResponses) {
        List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<Long> successfullyProcessedPoIds = new ArrayList<Long>();
        if (poIds != null && !poIds.isEmpty()) {
            final DataBucket liveBucket = dpsUtil.getLiveBucket();
            final List<PersistenceObject> poList = liveBucket.findPosByIds(poIds);
            if (poList.isEmpty()) {
                LOGGER.debug("No Records found with poIds: {}", poList);
            } else {
                alarmActionInformations = clearRecords(poList, operatorName, successfullyProcessedPoIds, alarmActionResponses, liveBucket);
            }
            final List<Long> requestedPoIds = new ArrayList<Long>(poIds);
            requestedPoIds.removeAll(successfullyProcessedPoIds);
            for (final Long poId : requestedPoIds) {
                final String alarmAction = AlarmAction.CLEAR.name();
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
                final AlarmActionInformation actionDetailsInCache = alarmActionsCacheManager.get(alarmActionInformation);
                if (actionDetailsInCache != null) {
                    alarmActionInformations.add(actionDetailsInCache);
                } else {
                    final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString());
                    alarmActionResponses.add(alarmActionResponse);
                    aasInstrumentedBean.increaseFailedClearAlarms();
                }
            }
        }
        return alarmActionInformations;
    }

    /**
     * This method perform the Clear operation for the given Open Alarms.
     *
     * @param persistenceObject
     *            The open alarm persistence object on which clear operation needs to be performed.
     * @param operatorName
     *            The operator name performing clear operation.
     * @param liveBucket
     *            The data base live bucket.
     * @return alarm action responses after performing clear operation.
     */
    private Map<String, String> processClear(final PersistenceObject persistenceObject, final AlarmActionInformation alarmActionInformation,
            final DataBucket liveBucket) {
        final Map<String, String> actionResponse = new HashMap<String, String>();
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        final String operatorName = alarmActionInformation.getOperatorName();
        final Long poId = persistenceObject.getPoId();
        final String alarmState = persistenceObject.getAttribute(ALARMSTATE);
        final Long alarmNumber = persistenceObject.getAttribute(ALARMNUMBER);
        final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
        final String recordType = persistenceObject.getAttribute(RECORDTYPE);

        LOGGER.debug("Current  AlarmState of  Po is: {} ", alarmState);

        if (CLEAREDUNACK.equals(alarmState)) {
            final AlarmActionInformation alarmActionDetailsInCache = alarmActionsCacheManager.get(alarmActionInformation);
            if (alarmActionDetailsInCache != null) {
                actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), SUCCESS);
                alarmAttributes.putAll(alarmActionDetailsInCache.getAlarmAttributes());
            } else {
                actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), ALREADY_CLEAR);
                LOGGER.debug("Alarm  having PoId: {} is Already Cleared", poId);
            }
        } else if (ACK.equals(alarmState)) {
            alarmAttributes.putAll(processClearForAckAlarm(persistenceObject, operatorName, liveBucket, poId, alarmNumber, recordType));
            liveBucket.deletePo(persistenceObject);
            actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), SUCCESS);
        } else if (UNACK.equals(alarmState)) {
            alarmAttributes.putAll(processClearForUnAckAlarm(persistenceObject, operatorName, poId, alarmNumber, recordType));
            actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), SUCCESS);
        }
        alarmActionInformation.setAlarmAttributes(alarmAttributes);
        return actionResponse;
    }

    private Map<String, Object> processClearForAckAlarm(final PersistenceObject persistenceObject, final String operatorName,
            final DataBucket liveBucket, final Long alarmId, final Long alarmNumber, final String recordType) {
        final Date ceaseTime = new Date();
        final Map<String, Object> alarmMap = persistenceObject.getAllAttributes();
        alarmMap.put(LASTALARMOPERATION, CLEAR);
        alarmMap.put(MANUALCEASE, true);
        alarmMap.put(CEASEOPERATOR, operatorName);
        alarmMap.put(CEASETIME, ceaseTime);
        alarmMap.put(ALARMSTATE, CLEARED_ACKNOWLEDGED);
        alarmMap.put(PRESENTSEVERITY, CLEARED);
        alarmMap.put(PREVIOUSSEVERITY, persistenceObject.getAttribute(PRESENTSEVERITY));
        final String psuedoPresentSeverity = persistenceObject.getAttribute(PRESENTSEVERITY) + UNDERSCORE_DELIMITER + CLEARED;
        alarmMap.put(PSEUDO_PRESENT_SEVERITY, PseudoSeverities.PSEUDO_SEVERITIES.get(psuedoPresentSeverity));
        alarmMap.put(PSEUDO_PREVIOUS_SEVERITY, PseudoSeverities.PSEUDO_SEVERITIES.get(persistenceObject.getAttribute(PRESENTSEVERITY)));
        alarmMap.put(LASTUPDATED, ceaseTime);

        persistenceObject.setAttributes(alarmMap);

        alarmMap.put(EVENTPOID, alarmId);
        alarmActionUtils.updateLastDeliveredTime(alarmMap);
        final String neFdn = persistenceObject.getAttribute(FDN);
        final String specificProblem = (String) alarmMap.get(SPECIFICPROBLEM);
        handleTechPresentOrAlarmSuppressedAlarm(neFdn, recordType, specificProblem);
        LOGGER.debug("Cleared Alarm having PoId: {} is", alarmId);
        return alarmMap;
    }

    private Map<String, Object> processClearForUnAckAlarm(final PersistenceObject persistenceObject, final String operatorName,
            final Long alarmId, final Long alarmNumber, final String recordType) {
        final Date ceaseTime = new Date();
        final Map<String, Object> attributeMap = new HashMap<String, Object>(16);
        attributeMap.put(ALARMSTATE, CLEAREDUNACK);
        attributeMap.put(CEASEOPERATOR, operatorName);
        attributeMap.put(CEASETIME, ceaseTime);
        attributeMap.put(LASTUPDATED, ceaseTime);
        attributeMap.put(PREVIOUSSEVERITY, persistenceObject.getAttribute(PRESENTSEVERITY));
        attributeMap.put(PRESENTSEVERITY, CLEARED);
        final String psuedoPresentSeverity = persistenceObject.getAttribute(PRESENTSEVERITY) + UNDERSCORE_DELIMITER + CLEARED;
        attributeMap.put(PSEUDO_PRESENT_SEVERITY, PseudoSeverities.PSEUDO_SEVERITIES.get(psuedoPresentSeverity));
        attributeMap.put(PSEUDO_PREVIOUS_SEVERITY, PseudoSeverities.PSEUDO_SEVERITIES.get(persistenceObject.getAttribute(PRESENTSEVERITY)));
        attributeMap.put(LASTALARMOPERATION, CLEAR);
        attributeMap.put(MANUALCEASE, true);

        persistenceObject.setAttributes(attributeMap);

        final Map<String, Object> alarmMap = persistenceObject.getAllAttributes();
        alarmActionUtils.updateLastDeliveredTime(alarmMap);
        alarmMap.put(EVENTPOID, alarmId);
        aasInstrumentedBean.increaseClearAlarmCount();
        final String neFdn = persistenceObject.getAttribute(FDN);
        final String specificProblem = (String) alarmMap.get(SPECIFICPROBLEM);
        handleTechPresentOrAlarmSuppressedAlarm(neFdn, recordType, specificProblem);

        LOGGER.debug("Alarm Having alarmId: {} Successfully updated the alarmState of alarm to: {} ", alarmId, CLEAREDUNACK);
        return alarmMap;
    }

    private void handleTechPresentOrAlarmSuppressedAlarm(final String neFdn, final String recordType, final String specificProblem) {
        // TORF-166531 - Clear on FMX Updated TechnicianPresent and AlarmSupressedMode alarms should also update the FmFunction
        // So added the below || to check the SP value of that alarm.
        if (ALARMSUPPRESSEDALARM.equalsIgnoreCase(recordType) || ALARMSUPPRESSED_SP.equals(specificProblem)) {
            dpsUtil.updateFmFunction(neFdn, ALARMSUPPRESSEDSTATE);
        } else if (TECHNICIANPRESENT.equalsIgnoreCase(recordType) || TECHNICIANPRESENT_SP.equals(specificProblem)) {
            dpsUtil.updateFmFunction(neFdn, TECHNICIANPRESENTSTATE);
        }
    }

    private List<AlarmActionInformation> performClearWithObjectOfReference(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>(100);

        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final String objectOfRefrence = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();

        final Restriction restrictionForfdn = getContainsRestriction(typeQuery, OBJECTOFREFERENCE, objectOfRefrence);
        typeQuery.setRestriction(restrictionForfdn);
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);

        if (poListIterator == null || !poListIterator.hasNext()) {
            LOGGER.debug("No Alarms found for FDN : {} ", objectOfRefrence);
            final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(NO_ALARMS_UNDERFDN, objectOfRefrence, "", "");
            alarmActionResponses.add(alarmActionResponse);
        } else {
            while (poListIterator.hasNext()) {
                final PersistenceObject persistenceObject = poListIterator.next();
                final Long poId = persistenceObject.getPoId();

                final String recordType = persistenceObject.getAttribute(RECORDTYPE);
                final String alarmAction = AlarmAction.CLEAR.name();
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);

                if (ERROR.equals(recordType) || REPEATED_ERROR.equals(recordType)) {
                    final Long alarmId = persistenceObject.getPoId();
                    final String alarmNumber = persistenceObject.getAttribute(ALARMNUMBER).toString();
                    final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ERROR_CLEAR, objectOfRefrence,
                            alarmId.toString(), alarmNumber);
                    alarmActionResponses.add(alarmActionResponse);
                    LOGGER.debug("Error with Id: {} can not be cleared directly", alarmId);
                } else {
                    final Map<String, String> actionResponse = processClear(persistenceObject, alarmActionInformation, liveBucket);
                    if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                        alarmActionsCacheManager.put(alarmActionInformation);
                        alarmActionInformations.add(alarmActionInformation);
                    }
                    alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
                }
            }
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> performClearWithObjectOfReferenceAndAlarmNumbers(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String objectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final List<Long> alarmNumbers = alarmActionData.getAlarmIds();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<Long> successfullyProcessedAlarmNumbers = new ArrayList<Long>();
        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final RestrictionBuilder restrictionBuilder = typeQuery.getRestrictionBuilder();

        final Restriction restrictionForfdn = getContainsRestriction(typeQuery, OBJECTOFREFERENCE, objectOfReference);
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final Object[] alarmNumberAray = alarmNumbers.toArray();
        final Restriction restrictionForAlarmNumber = typeQuery.getRestrictionBuilder().in(ALARMNUMBER, alarmNumberAray);
        final Restriction finalRestriction = restrictionBuilder.allOf(restrictionForfdn, restrictionForAlarmNumber);
        typeQuery.setRestriction(finalRestriction);
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();

        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);

        if (poListIterator == null || !poListIterator.hasNext()) {
            LOGGER.debug(" Alarm with Id: {}  not found under the FDN: {}", alarmNumbers, objectOfReference);
        } else {
            while (poListIterator.hasNext()) {
                final PersistenceObject persistenceObject = poListIterator.next();
                final Long alarmNumber = persistenceObject.getAttribute(ALARMNUMBER);
                final Long alarmId = persistenceObject.getPoId();
                successfullyProcessedAlarmNumbers.add(alarmNumber);
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName,
                        alarmId);
                final String recordType = persistenceObject.getAttribute(RECORDTYPE);
                if (ERROR_MESSAGE.equals(recordType) || REPEATED_ERROR_MESSAGE.equals(recordType)) {
                    LOGGER.debug("Error with Id: {} cannot be cleared.", alarmNumber);
                    final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ERROR_CLEAR, objectOfReference,
                            alarmId.toString(), alarmNumber.toString());
                    alarmActionResponses.add(alarmActionResponse);
                } else {
                    final Map<String, String> actionResponse = processClear(persistenceObject, alarmActionInformation, liveBucket);
                    if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                        alarmActionsCacheManager.put(alarmActionInformation);
                        alarmActionInformations.add(alarmActionInformation);
                    }
                    alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
                }
            }
        }
        if (alarmNumbers.size() != successfullyProcessedAlarmNumbers.size()) {
            alarmNumbers.removeAll(successfullyProcessedAlarmNumbers);
            LOGGER.debug(" Alarm with Id : {} not found for FDN : {}", alarmNumbers, objectOfReference);
            for (final Long poId : alarmNumbers) {
                final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, objectOfReference, "",
                        poId.toString());
                alarmActionResponses.add(alarmActionResponse);
                aasInstrumentedBean.increaseFailedClearAlarms();
            }
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> clearRecords(final List<PersistenceObject> poList, final String operatorName,
            final List<Long> successfullyProcessedPoIds, final List<AlarmActionResponse> alarmActionResponses, final DataBucket liveBucket) {
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        for (final PersistenceObject persistenceObject : poList) {
            final Long poId = persistenceObject.getPoId();
            successfullyProcessedPoIds.add(poId);
            final String recordType = persistenceObject.getAttribute(RECORDTYPE);

            if (ERROR_MESSAGE.equals(recordType) || REPEATED_ERROR_MESSAGE.equals(recordType)) {
                LOGGER.debug("Error with Id : {} can not be cleared directly.", poId);
                final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(ERROR_CLEAR, "", "", poId.toString());
                alarmActionResponses.add(alarmActionResponse);
            } else {
                final String alarmAction = AlarmAction.CLEAR.name();
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
                final Map<String, String> actionResponse = processClear(persistenceObject, alarmActionInformation, liveBucket);
                if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                    alarmActionsCacheManager.put(alarmActionInformation);
                    alarmActionInformations.add(alarmActionInformation);
                }
                alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
            }
        }
        return alarmActionInformations;
    }

    private Restriction getContainsRestriction(final Query<TypeRestrictionBuilder> typeQuery, final String attributeName,
            final String attributeValue) {
        final Restriction restriction = typeQuery.getRestrictionBuilder().matchesString(attributeName, attributeValue, StringMatchCondition.CONTAINS);
        return restriction;
    }
}
