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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM;

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
 * This class Starts the timer, stops the timer and checks the timer value based on FMA_DELAYED_ACK_OF_ALARMS_ON and FMA_TIME_TO_DELAYED_ACK_EVENTS .
 **/
@Singleton
@Startup
public class DelayedAckAlarmTimerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedAckAlarmTimerService.class);

    @Resource
    private TimerService timerService;

    private Timer delayedAlarmAckTimer;

    @Inject
    private DpsAccessTransactionalFacade dpsAccessTransactionalFacade;

    @Inject
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    @Inject
    private DelayedAckAlarmBatchTimer delayedAckAlarmBatchTimer;

    @Inject
    private AlarmAckBatchManager alarmAckBatchManager;

    @Inject
    private AlarmActionUtils alarmActionUtils;

    @PostConstruct
    public void init() {
        if (configurationsChangeListener.isDelayedAckForAlarmsEnabled()) {
            startAlarmAckTimer(configurationsChangeListener.getCheckfrequency());
        } else {
            LOGGER.info("Delayed acknowledgement for alarms is not enabled, So not starting the timer.");
        }
    }

    public void processDelayedAckForAlarmsEnabledChanges(final boolean delayedAckForAlarmsEnabled) {
        if (delayedAckForAlarmsEnabled) {
            startAlarmAckTimer(configurationsChangeListener.getCheckfrequency());
        } else {
            cancelDelayedAlarmAckTimer();
        }
    }

    public void processCheckFrequencyChangesForAlarms(final Integer checkFrequency) {
        if (configurationsChangeListener.isDelayedAckForAlarmsEnabled()) {
            LOGGER.debug("Restarting the timer with check frequency {}", checkFrequency);
            cancelDelayedAlarmAckTimer();
            startAlarmAckTimer(checkFrequency);
        } else {
            LOGGER.debug("Delayed acknowledgement for alarms is not enabled, So not restarting the timer.");
        }
    }

    public void startAlarmAckTimer(final Integer timeInterval) {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        delayedAlarmAckTimer = timerService.createIntervalTimer((long) timeInterval * 60 * 1000, (long) timeInterval * 60 * 1000, timerConfig);
        LOGGER.info("The delayed ack timer for alarms started with time interval {} ", timeInterval);
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout(final Timer timer) {
        final long startTime = System.currentTimeMillis();
        try {
            if (alarmActionServiceClusterListener.getMasterState()) {
                LOGGER.info("Master AlarmActionService Instance, checking alarms for delayed acknowledgement");
                final List<Long> poIds = dpsAccessTransactionalFacade.getPoIds(configurationsChangeListener.getClearAge(), ALARM);
                LOGGER.debug("Number of Alarms Found for delayed ack: {}", poIds.size());
                if (poIds.isEmpty()) {
                    LOGGER.debug("No Alarms Found");
                } else {
                    final BatchingParameters batchingParameters = alarmActionUtils.createBatchigParameters();
                    final LinkedList<List<Long>> poIdBatchesForCurrentCycle = alarmAckBatchManager.getpoIdBatches(poIds, batchingParameters);
                    delayedAckAlarmBatchTimer.startAlarmAckTimer(poIdBatchesForCurrentCycle, startTime);
                }
            }
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred while trying to perform the DelayedACK on Alarms. DelayedACK will be performed in the next timeout.",
                    exception);
        }
    }

    @PreDestroy
    public void cancelDelayedAlarmAckTimer() {
        if (delayedAlarmAckTimer != null) {
            try {
                delayedAlarmAckTimer.cancel();
            } catch (final Exception exception) {
                LOGGER.error("Exception while cancelling the delayedAlarmAckTimer: ", exception);
            }
        }
    }
}
