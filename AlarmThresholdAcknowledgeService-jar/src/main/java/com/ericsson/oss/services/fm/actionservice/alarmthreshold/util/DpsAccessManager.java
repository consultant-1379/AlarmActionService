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

package com.ericsson.oss.services.fm.actionservice.alarmthreshold.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.Projection;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.ProjectionBuilder;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.alarm.action.service.api.AlarmActionService;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;

/**
 * Class responsible to manage the DPS connections to get unique ids of alarms.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class DpsAccessManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DpsAccessManager.class);

    @Inject
    private DataPersistenceServiceProxy dataPersistenceServiceProxy;

    @EServiceRef
    private AlarmActionService alarmActionService;

    public long getActiveAlarmCount() {
        long activeAlarmCount = 0;
        dataPersistenceServiceProxy.getService().setWriteAccess(false);
        final DataBucket liveBucket = dataPersistenceServiceProxy.getLiveBucket();
        final QueryBuilder queryBuilder = dataPersistenceServiceProxy.getQueryBuilder();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM);
        activeAlarmCount = queryExecutor.executeCount(typeQuery);
        LOGGER.info("{} Active Alarms are present in DB", activeAlarmCount);
        return activeAlarmCount;
    }

    public boolean isInternalAlarmRaisedForAlarmThresholdNotification() {
        boolean result = false;
        dataPersistenceServiceProxy.getService().setWriteAccess(false);
        final DataBucket liveBucket = dataPersistenceServiceProxy.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        final QueryBuilder queryBuilder = dataPersistenceServiceProxy.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM);
        final Restriction specificProblemRestriction = typeQuery.getRestrictionBuilder().equalTo(Constants.SPECIFIC_PROBLEM,
                Constants.THRESHOLD_LIMIT_SPECIFIC_PROBLEM);
        final Restriction ackPresentSeverityRestriction = typeQuery.getRestrictionBuilder().equalTo(Constants.ALARM_STATE,
                Constants.ACTIVE_ACKNOWLEDGED);

        final Restriction unackPresentSeverityRestriction = typeQuery.getRestrictionBuilder().equalTo(Constants.ALARM_STATE,
                Constants.ACTIVE_UNACKNOWLEDGED);

        final Restriction severityRestriction = typeQuery.getRestrictionBuilder().anyOf(ackPresentSeverityRestriction,
                unackPresentSeverityRestriction);

        final Restriction finalRestriction = typeQuery.getRestrictionBuilder().allOf(specificProblemRestriction, severityRestriction);

        typeQuery.setRestriction(finalRestriction);
        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);

        while (poListIterator.hasNext()) {
            poListIterator.next();
            result = true;
            break;
        }
        LOGGER.debug("isInternalAlarmRaisedForAlarmThresholdNotification returned {} ", result);
        return result;
    }

    /**
     * Gets the list of alarm poIds with severity CLEARED from DPS.
     *
     * @return the list of open alarm poIds.
     */
    public List<Long> getPoIds() {
        LOGGER.debug("Getting the alarm poIds with CLEARED severity for performing acknowledgement");

        dataPersistenceServiceProxy.getService().setWriteAccess(false);
        final DataBucket liveBucket = dataPersistenceServiceProxy.getLiveBucket();
        final QueryBuilder queryBuilder = dataPersistenceServiceProxy.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM);
        final Restriction seveirtyRestriction = typeQuery.getRestrictionBuilder().equalTo(Constants.PRESENT_SEVERITY, Constants.SEV_CLEARED);

        typeQuery.setRestriction(seveirtyRestriction);
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();

        final Projection poidProjection = ProjectionBuilder.field(ObjectField.PO_ID);

        final List<Long> poIds = queryExecutor.executeProjection(typeQuery, poidProjection);
        return new ArrayList<Long>(poIds);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void acknowledgeClearedAlarms(final List<Long> poIds) {
        if (poIds.isEmpty()) {
            LOGGER.info("No cleared alarms found for acknowledgement");
        } else {
            LOGGER.info("The number of  cleared alarms found for acknowledgement : {}", poIds.size());
            final AlarmActionData alarmActionData = new AlarmActionData();
            alarmActionData.setAlarmIds(new ArrayList<Long>(poIds));
            alarmActionData.setOperatorName(Constants.ALARM_ACTION_SERVICE);
            alarmActionData.setAction(AlarmAction.ACK);
            alarmActionService.alarmActionUpdate(alarmActionData);
        }
    }
}
