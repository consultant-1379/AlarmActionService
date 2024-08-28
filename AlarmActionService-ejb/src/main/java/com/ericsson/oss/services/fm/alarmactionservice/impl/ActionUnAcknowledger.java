/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_UNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAREDUNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COLON_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTALARMOPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
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
 * This class performs Alarm UnAcknowledge Operation. It will perform ActionAcknowledge Operation for Error and Alarm with following inputs. <br>
 * 1. FDN <br>
 * 2. FDN and AlarmIDs <br>
 * 3. AlarmIDs.
 **/
@Stateless
public class ActionUnAcknowledger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionUnAcknowledger.class);

    @Inject
    private DpsUtil dpsUtil;

    @Inject
    private AASInstrumentedBean aasInstrumentedBean;

    @Inject
    private AlarmActionUtils alarmActionUtils;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    public List<AlarmActionInformation> processUnAck(final String operatorName, final List<Long> poIds,
            final List<AlarmActionResponse> alarmActionResponses) {
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(poIds);
        final List<Long> processedPoIds = new ArrayList<Long>();
        for (final PersistenceObject persistenceObject : persistenceObjects) {
            final Long poId = persistenceObject.getPoId();
            processedPoIds.add(poId);
            final String alarmAction = AlarmAction.UNACK.name();
            final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);

            final Map<String, String> actionResponse = processAlarmAction(persistenceObject, alarmActionInformation);
            if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                alarmActionsCacheManager.put(alarmActionInformation);
                alarmActionInformations.add(alarmActionInformation);
            }
            alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
        }
        final List<AlarmActionResponse> failedActionResponses = AlarmActionUtils.buildResponseForInvalidPoIds(poIds, processedPoIds);
        alarmActionResponses.addAll(failedActionResponses);
        return alarmActionInformations;
    }

    public List<AlarmActionInformation> performUnAck(final AlarmActionData alarmActionData, final List<AlarmActionResponse> alarmActionResponses) {
        List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        if (isNotBlank(alarmActionData.getObjectOfReference())) {
            if (alarmIdList != null && !alarmIdList.isEmpty()) {
                alarmActionInformations = performUnAckWithFdnAndPoIds(alarmActionData, alarmActionResponses);
            } else {
                alarmActionInformations = performUnAckWithFdn(alarmActionData, alarmActionResponses);
            }
        } else {
            alarmActionInformations = performUnAckWithPoIds(alarmActionData, alarmActionResponses);
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> performUnAckWithFdn(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final List<AlarmActionInformation> alarmsActionInformation = new ArrayList<AlarmActionInformation>();

        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final String alarmAction = alarmActionData.getAlarmAction().name();

        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final Restriction restriction1 = getContainsRestriction(typeQuery, OBJECTOFREFERENCE, inputObjectOfReference);
        typeQuery.setRestriction(restriction1);
        final QueryExecutor queryExecutor = dpsUtil.getLiveBucket().getQueryExecutor();
        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);

        if (poListIterator == null || !poListIterator.hasNext()) {
            LOGGER.debug("No Alarms found for FDN: {}", inputObjectOfReference);
            alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", ""));
        } else {
            while (poListIterator.hasNext()) {
                final PersistenceObject persistenceObject = poListIterator.next();
                final Long poId = persistenceObject.getPoId();
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
                final Map<String, String> actionResponse = processAlarmAction(persistenceObject, alarmActionInformation);
                if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                    alarmActionsCacheManager.put(alarmActionInformation);
                    alarmsActionInformation.add(alarmActionInformation);
                }
                alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
            }
        }
        return alarmsActionInformation;
    }

    private List<AlarmActionInformation> performUnAckWithFdnAndPoIds(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<Long> processedPoIds = new ArrayList<Long>();
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        if (!alarmIdList.isEmpty()) {
            final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
            for (final PersistenceObject persistenceObject : persistenceObjects) {
                final Long alarmId = persistenceObject.getPoId();
                processedPoIds.add(alarmId);
                if (!persistenceObject.getType().equals(OPENALARM)) {
                    LOGGER.debug("Alarm with ID : {} Not Found under the FDN : {} ", alarmId, inputObjectOfReference);
                    alarmActionResponses
                            .add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", alarmId.toString()));
                } else if (persistenceObject.getType().equals(OPENALARM)) {
                    final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
                    if (objectOfReference.contains(inputObjectOfReference)) {
                        final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction,
                                operatorName, alarmId);
                        final Map<String, String> actionResponse = processAlarmAction(persistenceObject, alarmActionInformation);
                        if (null != alarmActionInformation.getAlarmAttributes() && !alarmActionInformation.getAlarmAttributes().isEmpty()) {
                            alarmActionsCacheManager.put(alarmActionInformation);
                            alarmActionInformations.add(alarmActionInformation);
                        }
                        alarmActionResponses.add(AlarmActionUtils.buildAlarmActionResponse(actionResponse));
                    } else {
                        LOGGER.debug("Alarm with Id : {} not found under the FDN : {} ", alarmId, inputObjectOfReference);
                        alarmActionResponses
                                .add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", alarmId.toString()));
                    }
                }
            }
        }
        if (alarmIdList.size() != processedPoIds.size()) {
            alarmIdList.removeAll(processedPoIds);
            LOGGER.debug("Alarm with PoIds: {} Not Found under OOR : {}", alarmIdList, inputObjectOfReference);
            for (final Long poId : alarmIdList) {
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", poId.toString()));
                aasInstrumentedBean.increaseFailedUnAckAlarms();
            }
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> performUnAckWithPoIds(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String operatorName = alarmActionData.getOperatorName();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        final List<Long> processedPoIds = new ArrayList<Long>(alarmIdList.size());
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        if (!alarmIdList.isEmpty()) {
            final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
            for (final PersistenceObject persistenceObject : persistenceObjects) {
                final Long poId = persistenceObject.getPoId();
                final String poType = persistenceObject.getType();
                processedPoIds.add(poId);
                if (!OPENALARM.equals(poType)) {
                    LOGGER.debug("Alarm with ID : {} Not Found ", poId);
                    alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString()));
                } else {
                    final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName,
                            poId);
                    final Map<String, String> actionResponse = processAlarmAction(persistenceObject, alarmActionInformation);
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
            LOGGER.debug(" Alarm with PoIDs: {} Not Found ", alarmIdList);
            for (final Long poId : alarmIdList) {
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString()));
                aasInstrumentedBean.increaseFailedUnAckAlarms();
            }
        }
        return alarmActionInformations;
    }

    /**
     * This method perform the UnAcknowledge operation for the given Open Alarm.
     * @Method : processUnAck
     * @param non
     *            -null PersistenceObject and Strings like FDN and OperatorName
     * @return List of HashMaps contains response and responseForMediation
     **/
    private Map<String, String> processAlarmAction(final PersistenceObject persistenceObject,
            final AlarmActionInformation alarmActionInformation) {
        final Map<String, String> actionResponse = new HashMap<String, String>();
        final Long poId = alarmActionInformation.getPoId();
        final Long alarmNumber = persistenceObject.getAttribute(ALARMNUMBER);
        final String alarmState = persistenceObject.getAttribute(ALARMSTATE);
        final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
        final Map<String, Object> alarmMap = new HashMap<String, Object>();
        LOGGER.debug("Current  AlarmState of  Po is: {} ", alarmState);

        if (UNACK.equalsIgnoreCase(alarmState) || CLEAREDUNACK.equalsIgnoreCase(alarmState)) {
            final AlarmActionInformation alarmActionDetailsInCache = alarmActionsCacheManager.get(alarmActionInformation);
            if (alarmActionDetailsInCache != null) {
                alarmMap.putAll(alarmActionDetailsInCache.getAlarmAttributes());
                actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), SUCCESS);
                alarmActionInformation.setAlarmAttributes(alarmMap);
            } else {
                actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), ALREADY_UNACK);
                LOGGER.debug("Alarm  with PoId: {}   is Already UnAcknowledged ", poId);
            }
        } else if (ACK.equalsIgnoreCase(alarmState)) {
            final Date lastUpdated = new Date();
            final Map<String, Object> attributeMap = new HashMap<String, Object>();
            attributeMap.put(ALARMSTATE, UNACK);
            attributeMap.put(ACKOPERATOR, "");
            attributeMap.put(ACKTIME, null);
            attributeMap.put(LASTUPDATED, lastUpdated);
            attributeMap.put(LASTALARMOPERATION, ACKSTATE_CHANGE);

            persistenceObject.setAttributes(attributeMap);
            alarmMap.putAll(persistenceObject.getAllAttributes());
            alarmMap.put(EVENTPOID, poId);
            alarmActionUtils.updateLastDeliveredTime(alarmMap);
            actionResponse.put(objectOfReference + COLON_DELIMITER + alarmNumber.toString() + COLON_DELIMITER + poId.toString(), SUCCESS);
            aasInstrumentedBean.increaseUnAckalarmCount();
            LOGGER.debug("Successfully Updated the alarmState of alarm to: {} with  poId: {} ", UNACK, poId);
            alarmActionInformation.setAlarmAttributes(alarmMap);
        }

        return actionResponse;
    }

    private Restriction getContainsRestriction(final Query<TypeRestrictionBuilder> typeQuery, final String attributeName,
            final String attributeValue) {
        final Restriction restriction = typeQuery.getRestrictionBuilder().matchesString(attributeName, attributeValue, StringMatchCondition.CONTAINS);
        return restriction;
    }

}
