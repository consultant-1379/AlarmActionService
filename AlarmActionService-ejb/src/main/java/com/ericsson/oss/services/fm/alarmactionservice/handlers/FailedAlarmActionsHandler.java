/*------------------------------------------------------------------------------
 * ********************************************************************************
 * COPYRIGHT Ericsson 2017
 * The copyright to the computer program(s)herein is the property of
 * Ericsson Inc.The programs may be used and/or copied only with written
 * permission from Ericsson Inc.or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s)have been supplied.
 *********************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.handlers;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACTIONS_TO_HISOTRY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACTIONS_TO_QUEUES;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.FailedAlarmActionUpdatesProcessor;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * This class provides functionality to sent alarm action information to alarm action consumers(JMS Queues). When the current alarm actions processing
 * instance goes down and alarm actions updated in DB but not sent to Queues.Those alarm action will be sent by the next master instance.
 */
@Stateless
public class FailedAlarmActionsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedAlarmActionsHandler.class);

    @Inject
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    @Inject
    private FailedAlarmActionsValidator failedAlarmActionsValidator;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    @Inject
    private FailedAlarmActionUpdatesProcessor failedAlarmActionUpdatesProcessor;

    /**
     * Sends alarm action information to alarm action consumers(JMS Queues). When the current alarm actions processing instance goes down and alarm
     * actions updated in DB but not sent to Queues.Those alarm action will be sent by the next master instance.
     */

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Asynchronous
    public void processFailedAlarmActions(final List<String> failedJbossInstances) {
        if (!failedJbossInstances.isEmpty()) {
            final Map<String, AlarmActionInformation> failedAlarmActions = alarmActionsCacheManager
                    .readFailedAlarmActionsFromCache(failedJbossInstances);
            LOGGER.trace("Failed alarm action read from cache is :{} and count :{}", failedAlarmActions, failedAlarmActions.size());
            if (!failedAlarmActions.isEmpty()) {
                final Collection<AlarmActionInformation> failedActionsFromCache = failedAlarmActions.values();
                try {
                    final Map<String, List<AlarmActionInformation>> failedActionsAlarms = failedAlarmActionsValidator
                            .validateFailedActionsWithDB(failedActionsFromCache);
                    final List<AlarmActionInformation> actionsToQueues = failedActionsAlarms.get(ACTIONS_TO_QUEUES);
                    final List<AlarmActionInformation> actionsToHistory = failedActionsAlarms.get(ACTIONS_TO_HISOTRY);
                    LOGGER.trace("Final failed alarm actions send to queues is :{}", actionsToQueues.size());
                    alarmActionUpdatesSender.sendFailedAlarmActionsToQueues(actionsToQueues);
                    alarmActionsCacheManager.removeAll(actionsToQueues);
                    if (!actionsToHistory.isEmpty()) {
                        failedAlarmActionUpdatesProcessor.processFailedAlarmActionUpdates(actionsToHistory, failedJbossInstances);
                    }
                    alarmActionsCacheManager.removeAll(actionsToHistory);
                } catch (final Exception exception) {
                    LOGGER.error("Exception occured while sending actions to queue or issue with cache operations: ", exception);
                }
            }
        }
    }
}
