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

package com.ericsson.oss.services.fm.alarmactionservice.handlers;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACTIONS_TO_HISOTRY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACTIONS_TO_QUEUES;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * This provides functionality to validate failed alarm actions information with OpenAlarms DB.Based on the validation will prepare final alarm
 * actions to send to JMS consumers.
 */
@Stateless
public class FailedAlarmActionsValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailedAlarmActionsValidator.class);

    @Inject
    private DpsUtil dpsUtil;

    /**
     * Method verifies all the alarm actions available in the AlarmActionsCache with OpenAlarm DB and prepare final failed actions to be resent to JMS
     * consumers.
     * @param failedActionsInCache
     *            failed alarm actions not sent to any Queues.
     * @param failedInstances
     *            contains failed instance information.
     * @return list of AlarmActionInformation
     */
    public Map<String, List<AlarmActionInformation>> validateFailedActionsWithDB(final Collection<AlarmActionInformation> failedActionsInCache) {
        final Map<String, List<AlarmActionInformation>> finalFailedActionsToSend = new HashMap<String, List<AlarmActionInformation>>();
        final List<AlarmActionInformation> alarmActionToBeSentToHistory = new ArrayList<AlarmActionInformation>();
        final List<AlarmActionInformation> alarmActionToBesentToAllQueues = new ArrayList<AlarmActionInformation>();
        final DataBucket dataBucket = dpsUtil.getLiveBucket();
        for (final AlarmActionInformation alarmActionInformation : failedActionsInCache) {
            final Long poId = alarmActionInformation.getPoId();
            final boolean isActionUpdatedInDb = alarmActionInformation.isActionUpdatedInDb();
            if (isActionUpdatedInDb) {
                final Map<String, Object> failedActionAttributes = alarmActionInformation.getAlarmAttributes();
                final PersistenceObject po = dataBucket.findPoById(poId);
                if (po == null) {
                    if (CLEARED_ACKNOWLEDGED.equals(failedActionAttributes.get(ALARMSTATE))) {
                        alarmActionToBesentToAllQueues.add(alarmActionInformation);
                    }
                } else {
                    final Map<String, Object> alarmAtrributes = po.getAllAttributes();
                    final Date lastUpdatedTimeInDB = (Date) alarmAtrributes.get(LASTUPDATED);
                    final Date lastUpdatedTimeInFailedAction = (Date) failedActionAttributes.get(LASTUPDATED);
                    if (lastUpdatedTimeInDB.equals(lastUpdatedTimeInFailedAction)) {
                        alarmActionToBesentToAllQueues.add(alarmActionInformation);
                    } else {
                        LOGGER.warn("Failed action and upon some action done and discarding action: {}", alarmActionInformation);
                        alarmActionToBeSentToHistory.add(alarmActionInformation);
                    }
                }
            }
        }
        finalFailedActionsToSend.put(ACTIONS_TO_QUEUES, alarmActionToBesentToAllQueues);
        finalFailedActionsToSend.put(ACTIONS_TO_HISOTRY, alarmActionToBeSentToHistory);
        LOGGER.debug("Total failed alarm action updates count:{}", finalFailedActionsToSend.size());

        return finalFailedActionsToSend;
    }

    /**
     * reads OpenAlarms from DB for the given poIds.
     * @param poIds
     *            list of poIds
     * @return {@code Map<Long, Map<String, Object>>} map containing open alarms information.
     */
    public Map<Long, Map<String, Object>> readOpenAlarms(final List<Long> poIds) {
        final Map<Long, Map<String, Object>> openAlarmAttributes = new HashMap<Long, Map<String, Object>>();
        final List<PersistenceObject> openAlarmPOs = dpsUtil.getLiveBucket().findPosByIds(poIds);
        for (final PersistenceObject po : openAlarmPOs) {
            final Long poId = po.getPoId();
            final Map<String, Object> alarmAttributes = po.getAllAttributes();
            alarmAttributes.put(EVENTPOID, poId);
            openAlarmAttributes.put(poId, alarmAttributes);
        }
        return openAlarmAttributes;
    }

}
