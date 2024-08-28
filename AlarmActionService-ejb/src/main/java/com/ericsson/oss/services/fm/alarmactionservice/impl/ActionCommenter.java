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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CRITICAL;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INDETERMINATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTALARMOPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MAJOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MINOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NO_ALARMS_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PRESENTSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBABLECAUSE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SPECIFICPROBLEM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.WARNING;

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
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionServiceSeverity;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * This class performs Alarm Comment Operation.
 * It will perform ActionAcknowledge Operation for Error and Alarm.It will perform ActionAcknowledge Operation with
 * following inputs. <br>
 * 1. FDN <br>
 * 2. FDN and AlarmIDs <br>
 * 3. AlarmIDs <br>
 * 4. FDN and SpecificProblem <br>
 * 5. FDN and EventType <br>
 * 6. FDN and ProbableCause <br>
 * 7. FDN and PresentSeverity.
 **/
@Stateless
public class ActionCommenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionCommenter.class);

    @Inject
    private DpsUtil dpsUtil;

    @Inject
    private AlarmActionUtils alarmActionUtils;

    @Inject
    private CommentHistoryRecorder commentHistoryRecorder;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    public List<AlarmActionInformation> performComment(final AlarmActionData alarmActionData, final List<AlarmActionResponse> alarmActionResponses) {
        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        if (isNotBlank(inputObjectOfReference)) {
            if (alarmIdList != null && !alarmIdList.isEmpty()) {
                alarmActionInformations = performCommentWithObjectOfReferenceAndAlarmIds(alarmActionData, alarmActionResponses);
            } else if (isNotBlank(alarmActionData.getSpecificProblem()) || isNotBlank(alarmActionData.getProbableCause())
                    || isNotBlank(alarmActionData.getEventType()) || alarmActionData.getPresentSeverity() != null) {
                alarmActionInformations = performCommentWithFiltersAndFdn(alarmActionData, alarmActionResponses);
            } else {
                alarmActionInformations = performCommentWithFdn(alarmActionData, alarmActionResponses);
            }
        } else {
            alarmActionInformations = performCommentWithPoIds(alarmActionData, alarmActionResponses);
        }
        return alarmActionInformations;
    }

    public List<AlarmActionInformation> processComment(final String operatorName, final String comment, final List<Long> poIds,
            final List<AlarmActionResponse> alarmActionResponses) {
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(poIds);
        final List<Long> processedPoIds = new ArrayList<Long>();
        final List<Long> requestedPoIds = new ArrayList<Long>();
        requestedPoIds.addAll(poIds);
        for (final PersistenceObject persistenceObject : persistenceObjects) {
            final Long poId = persistenceObject.getPoId();
            processedPoIds.add(poId);
            final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(COMMENT, operatorName, poId);
            alarmActionInformation.setComment(comment);
            alarmActionResponses.add(processComment(persistenceObject, alarmActionInformation));
            alarmActionInformations.add(alarmActionInformation);
        }
        alarmActionResponses.addAll(AlarmActionUtils.buildResponseForInvalidPoIds(requestedPoIds, processedPoIds));
        return alarmActionInformations;
    }

    /**
     * This method perform the Clear operation for the given Open Alarms.
     * @param persistenceObject
     *            The open alarm persistent object.
     * @param operatorName
     *            The operator name performing comment.
     * @param comment
     *            The comment text for alarm.
     * @return The alarm action response after performing comment.
     */
    private AlarmActionResponse processComment(final PersistenceObject persistenceObject,
            final AlarmActionInformation alarmActionInformation) {
        final Long poId = alarmActionInformation.getPoId();
        final Long alarmNumber = persistenceObject.getAttribute(ALARMNUMBER);
        final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
        final String operatorName = alarmActionInformation.getOperatorName();
        final String comment = alarmActionInformation.getComment();
        final Date lastUpdated = new Date();
        final Map<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(COMMENTTEXT, comment);
        attributeMap.put(COMMENTOPERATOR, operatorName);
        attributeMap.put(COMMENTTIME, lastUpdated);
        attributeMap.put(LASTALARMOPERATION, COMMENT);
        attributeMap.put(LASTUPDATED, lastUpdated);
        persistenceObject.setAttributes(attributeMap);
        LOGGER.debug("Successfully updated the comment to: {} by the operator: {} with lastUpdated time: {} for alarm having Id:{}", comment,
                operatorName, lastUpdated, persistenceObject.getPoId());

        commentHistoryRecorder.storeCommentHistory(comment, operatorName, lastUpdated, persistenceObject);
        final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(SUCCESS, objectOfReference,
                alarmNumber.toString(), poId.toString());

        final Map<String, Object> alarmMap = persistenceObject.getAllAttributes();
        alarmMap.put(EVENTPOID, poId);
        alarmActionUtils.updateLastDeliveredTime(alarmMap);
        alarmActionInformation.setAlarmAttributes(alarmMap);
        alarmActionsCacheManager.put(alarmActionInformation);
        return alarmActionResponse;
    }

    private List<AlarmActionInformation> performCommentWithFdn(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final List<AlarmActionInformation> alarmActonsInformation = new ArrayList<AlarmActionInformation>();
        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final String comment = alarmActionData.getComment();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final Restriction restrictionForfdn = typeQuery.getRestrictionBuilder().matchesString(OBJECTOFREFERENCE, inputObjectOfReference,
                StringMatchCondition.CONTAINS);
        typeQuery.setRestriction(restrictionForfdn);
        final QueryExecutor queryExecutor = dpsUtil.getLiveBucket().getQueryExecutor();

        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);
        if (poListIterator == null || !poListIterator.hasNext()) {
            LOGGER.debug("No Alarms found with OOR: {} ", inputObjectOfReference);
            alarmActionResponses.add(AlarmActionUtils.setActionResponse(NO_ALARMS_UNDERFDN, inputObjectOfReference, "", ""));
        } else {
            while (poListIterator.hasNext()) {
                final PersistenceObject persistenceObject = poListIterator.next();
                final Long poId = persistenceObject.getPoId();
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
                alarmActionInformation.setComment(comment);
                alarmActionResponses.add(processComment(persistenceObject, alarmActionInformation));
                alarmActonsInformation.add(alarmActionInformation);
            }
        }
        return alarmActonsInformation;
    }

    private List<AlarmActionInformation> performCommentWithObjectOfReferenceAndAlarmIds(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        final String comment = alarmActionData.getComment();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final List<Long> processedPoIds = new ArrayList<Long>(alarmIdList.size());
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();

        final DataBucket liveBucket = dpsUtil.getLiveBucket();

        if (!alarmIdList.isEmpty()) {
            final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
            for (final PersistenceObject persistenceObject : persistenceObjects) {
                final Long poId = persistenceObject.getPoId();
                final String poType = persistenceObject.getType();
                if (!OPENALARM.equals(poType)) {
                    alarmActionResponses
                            .add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", poId.toString()));
                    LOGGER.debug("Alarm with ID : {} Not Found under FDN {}", poId, inputObjectOfReference);
                } else {
                    final String objectOfReference = persistenceObject.getAttribute(OBJECTOFREFERENCE);
                    if (objectOfReference.contains(inputObjectOfReference)) {
                        processedPoIds.add(poId);
                        final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction,
                                operatorName, poId);
                        alarmActionInformation.setComment(comment);
                        alarmActionResponses.add(processComment(persistenceObject, alarmActionInformation));
                        alarmActionInformations.add(alarmActionInformation);
                    } else {
                        alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "",
                                poId.toString()));
                        LOGGER.debug("Alarm having Id : {} not found under the FDN : {} ", poId, inputObjectOfReference);
                    }
                }
            }
        }
        if (alarmIdList.size() != processedPoIds.size()) {
            alarmIdList.removeAll(processedPoIds);
            LOGGER.debug("Alarm with Ids: {} Not Found under FDN {}", alarmIdList, inputObjectOfReference);
            for (final Long poId : alarmIdList) {
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND_UNDERFDN, inputObjectOfReference, "", poId.toString()));
            }
        }
        return alarmActionInformations;
    }

    private List<AlarmActionInformation> performCommentWithPoIds(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final String operatorName = alarmActionData.getOperatorName();
        final List<Long> poIds = alarmActionData.getAlarmIds();
        final String comment = alarmActionData.getComment();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<Long> successfullyProcessedPoIds = new ArrayList<Long>(poIds.size());
        if (!poIds.isEmpty()) {
            final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(poIds);
            for (final PersistenceObject persistenceObject : persistenceObjects) {
                final Long alarmId = persistenceObject.getPoId();
                successfullyProcessedPoIds.add(alarmId);
                final String poType = persistenceObject.getType();
                if (!OPENALARM.equals(poType)) {
                    alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", alarmId.toString()));
                } else {
                    final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
                    alarmActionInformation.setPoId(alarmId);
                    alarmActionInformation.setAlarmAction(alarmAction);
                    alarmActionInformation.setOperatorName(operatorName);
                    alarmActionInformation.setComment(comment);
                    alarmActionResponses.add(processComment(persistenceObject, alarmActionInformation));
                    alarmActionInformations.add(alarmActionInformation);
                }
            }
        }
        if (poIds.size() != successfullyProcessedPoIds.size()) {
            poIds.removeAll(successfullyProcessedPoIds);
            LOGGER.debug("Alarm with PoIds: {} Not Found ", poIds);
            for (final Long poId : poIds) {
                alarmActionResponses.add(AlarmActionUtils.setActionResponse(ALARM_NOTFOUND, "", "", poId.toString()));
            }
        }
        return alarmActionInformations;
    }

    private Restriction buildEqualRestrictions(final AlarmActionData alarmActionData, final Query<TypeRestrictionBuilder> typeQuery) {
        Restriction restrictionForFilter = null;
        if (isNotBlank(alarmActionData.getSpecificProblem())) {
            restrictionForFilter = getEqualRestriction(typeQuery, SPECIFICPROBLEM, alarmActionData.getSpecificProblem());
        } else if (isNotBlank(alarmActionData.getProbableCause())) {
            restrictionForFilter = getEqualRestriction(typeQuery, PROBABLECAUSE, alarmActionData.getProbableCause());
        } else if (isNotBlank(alarmActionData.getEventType())) {
            restrictionForFilter = getEqualRestriction(typeQuery, EVENTTYPE, alarmActionData.getEventType());
        } else if (alarmActionData.getPresentSeverity() != null) {
            final AlarmActionServiceSeverity presentSeverity = alarmActionData.getPresentSeverity();
            switch (presentSeverity) {
                case INDETERMINATE:
                    restrictionForFilter = getEqualRestriction(typeQuery, PRESENTSEVERITY, INDETERMINATE);
                    break;
                case CLEARED:
                    restrictionForFilter = getEqualRestriction(typeQuery, PRESENTSEVERITY, CLEARED);
                    break;
                case MAJOR:
                    restrictionForFilter = getEqualRestriction(typeQuery, PRESENTSEVERITY, MAJOR);
                    break;
                case CRITICAL:
                    restrictionForFilter = getEqualRestriction(typeQuery, PRESENTSEVERITY, CRITICAL);
                    break;
                case MINOR:
                    restrictionForFilter = getEqualRestriction(typeQuery, PRESENTSEVERITY, MINOR);
                    break;
                case WARNING:
                    restrictionForFilter = getEqualRestriction(typeQuery, PRESENTSEVERITY, WARNING);
                    break;
                default:
                    break;
            }
        }
        return restrictionForFilter;
    }

    private List<AlarmActionInformation> performCommentWithFiltersAndFdn(final AlarmActionData alarmActionData,
            final List<AlarmActionResponse> alarmActionResponses) {
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();

        final String inputObjectOfReference = alarmActionData.getObjectOfReference();
        final String operatorName = alarmActionData.getOperatorName();
        final String comment = alarmActionData.getComment();
        final String alarmAction = alarmActionData.getAlarmAction().name();

        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);

        final Restriction restrictionForfdn = getContainsRestriction(typeQuery, OBJECTOFREFERENCE, alarmActionData.getObjectOfReference());

        final Restriction restrictionForFilter = buildEqualRestrictions(alarmActionData, typeQuery);

        final RestrictionBuilder restrictionBuilder = typeQuery.getRestrictionBuilder();
        final Restriction finalRestriction = restrictionBuilder.allOf(restrictionForfdn, restrictionForFilter);
        typeQuery.setRestriction(finalRestriction);

        final QueryExecutor queryExecutor = dpsUtil.getLiveBucket().getQueryExecutor();
        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);

        if (poListIterator == null || !poListIterator.hasNext()) {
            LOGGER.debug("No Alarms found Under FDN: {}", inputObjectOfReference);
            alarmActionResponses.add(AlarmActionUtils.setActionResponse(NO_ALARMS_UNDERFDN, inputObjectOfReference, "", ""));
        } else {
            while (poListIterator.hasNext()) {
                final PersistenceObject persistenceObject = poListIterator.next();
                final Long poId = persistenceObject.getPoId();
                final AlarmActionInformation alarmActionInformation = alarmActionUtils.prepareAlarmActionInformation(alarmAction, operatorName, poId);
                alarmActionInformation.setComment(comment);
                alarmActionResponses.add(processComment(persistenceObject, alarmActionInformation));
                alarmActionInformations.add(alarmActionInformation);
            }
        }
        return alarmActionInformations;
    }

    //TODO:Duplicated across all classes.Needs to be moved to some utility class.
    private Restriction getContainsRestriction(final Query<TypeRestrictionBuilder> typeQuery, final String attributeName,
            final String attributeValue) {
        final Restriction restriction = typeQuery.getRestrictionBuilder().matchesString(attributeName, attributeValue, StringMatchCondition.CONTAINS);
        return restriction;
    }

    private Restriction getEqualRestriction(final Query<TypeRestrictionBuilder> typeQuery, final String attributeName, final Object attributeValue) {
        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(attributeName, attributeValue);
        return restriction;
    }
}
