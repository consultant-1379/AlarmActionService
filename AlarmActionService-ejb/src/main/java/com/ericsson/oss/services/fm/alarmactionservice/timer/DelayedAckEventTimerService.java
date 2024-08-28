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

package com.ericsson.oss.services.fm.alarmactionservice.timer;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENT;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.AlarmAckBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.BatchingParameters;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;

/**
 * This class Starts the timer, stops the timer and checks the timer value based on FMA_DELAYED_ACK_OF_EVENTS_ON and FMA_TIME_TO_DELAYED_ACK_EVENTS .
 **/
@Singleton
@Startup
public class DelayedAckEventTimerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedAckEventTimerService.class);

    @Resource
    private TimerService service;

    private Timer eventTimer;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    @Inject
    private DpsAccessTransactionalFacade dpsAccessTransactionalFacade;

    @Inject
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Inject
    private DelayedAckEventBatchTimer delayedAckEventBatchTimer;

    @Inject
    private AlarmAckBatchManager alarmAckBatchManager;

    @Inject
    private AlarmActionUtils alarmActionUtils;

    @PostConstruct
    public void init() {
        if (configurationsChangeListener.isDelayedAckForEventsEnabled()) {
            startEventAckTimer(configurationsChangeListener.getCheckfrequency());
        } else {
            LOGGER.info("Delayed acknowledgement for events is not enabled, So not starting the timer.");
        }
    }

    public void processDelayedAckForEventsEnabledChanges(final boolean delayedAckForEventsEnabled) {
        if (delayedAckForEventsEnabled) {
            startEventAckTimer(configurationsChangeListener.getCheckfrequency());
        } else {
            cancelEventTimer();
        }
    }

    public void processCheckFrequencyChangesForEvents(final Integer checkFrequency) {
        if (configurationsChangeListener.isDelayedAckForEventsEnabled()) {
            cancelEventTimer();
            startEventAckTimer(checkFrequency);
        } else {
            LOGGER.info("Delayed acknowledgement for alarms is not enabled, So not restarting the timer.");
        }
    }

    public void startEventAckTimer(final Integer checkInterval) {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        eventTimer = service.createIntervalTimer((long) checkInterval * 60 * 1000, (long) checkInterval * 60 * 1000, timerConfig);
        LOGGER.info("The delayed ack timer for events started with time interval {} ", checkInterval);
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout(final Timer timer) {
        final long startTime = System.currentTimeMillis();
        if (alarmActionServiceClusterListener.getMasterState()) {
            LOGGER.debug("Current AlarmActionService Instance is master");
            final List<Long> poIds = dpsAccessTransactionalFacade.getPoIds(configurationsChangeListener.getGenerationAge(), EVENT);
            if (poIds.isEmpty()) {
                LOGGER.debug("No Events Found ");
            } else {
                LOGGER.debug("Number of Events Found for delayed ack: {}", poIds.size());
                try {
                    final BatchingParameters batchingParameters = alarmActionUtils.createBatchigParameters();
                    final LinkedList<List<Long>> poIdBatchesForCurrentCycle = alarmAckBatchManager.getpoIdBatches(poIds, batchingParameters);
                    delayedAckEventBatchTimer.startAlarmAckTimer(poIdBatchesForCurrentCycle, startTime);
                } catch (final Exception exception) {
                    LOGGER.error("Exception {} occurred while trying to perform the DelayedACK on Events. "
                            + "DelayedACK will be performed in the next timeout.", exception);
                }
            }
        } else {
            LOGGER.debug("Current AlarmActionService instance is not a master");
        }
    }

    @PreDestroy
    public void cancelEventTimer() {
        if (eventTimer != null) {
            eventTimer.cancel();
        }
    }
}
