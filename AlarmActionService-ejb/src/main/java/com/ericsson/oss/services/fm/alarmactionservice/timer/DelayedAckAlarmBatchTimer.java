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

package com.ericsson.oss.services.fm.alarmactionservice.timer;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_ACTION_SERVICE;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.impl.AlarmActionsBatchManager;

/**
 * Responsible for acknowledging batch of alarms based on the timer's timeout.
 **/
@Stateless
public class DelayedAckAlarmBatchTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedAckAlarmBatchTimer.class);

    private LinkedList<List<Long>> masterPoIdsList = new LinkedList<List<Long>>();

    private long startTime;

    @Resource
    private TimerService timerService;

    private Timer delayedAlarmAckTimer;

    @Inject
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;
    @Inject
    private SystemRecorder systemRecorder;

    public void startAlarmAckTimer(final LinkedList<List<Long>> poIdSubBatches, final long startTime) {
        cancelDelayedAlarmAckTimer();
        this.masterPoIdsList = poIdSubBatches;
        this.startTime = startTime;
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        final long timeInterval = configurationsChangeListener.getDelayedAckBatchTimerTimeOut() * 1000L;
        delayedAlarmAckTimer = timerService.createIntervalTimer(0, timeInterval, timerConfig);
        LOGGER.debug("The delayed ack timer for started with {} alarm batches", masterPoIdsList.size());
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout(final Timer timer) {
        try {
            final int checkFrequency = configurationsChangeListener.getCheckfrequency();
            final long currentBatchStartTime = System.currentTimeMillis();
            // we are considering the time upto 90% of the current cycle time which is delayedAckInterval *60000 *(100-10)/100 which is equal 540000,
            // this due to avoid optimistic lock which may happens due to the timers overlapping.
            final long allowableTimeForCurrentCycle = checkFrequency * 60000 * (100 - configurationsChangeListener.getDelayedAckCycleGaurdTime())
                    / 100 + startTime;
            if (!masterPoIdsList.isEmpty() && currentBatchStartTime < allowableTimeForCurrentCycle) {
                final List<Long> poIdsBatch = masterPoIdsList.get(0);
                batchAcknowledge(poIdsBatch);
                masterPoIdsList.remove();
            } else {
                LOGGER.debug(
                        "Number of PoId batches skipped in current cycle {} ,"
                                + " current Batch Start Time {} and max allowed time for the processing of current cycle is {}",
                        masterPoIdsList.size(), currentBatchStartTime, allowableTimeForCurrentCycle);
                cancelDelayedAlarmAckTimer();
            }
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred while trying to perform the DelayedACK on Alarms. DelayedACK will be performed in the next timeout.",
                    exception);
        }
    }

    private void batchAcknowledge(final List<Long> poIdsBtach) {
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setOperatorName(ALARM_ACTION_SERVICE);
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIdsBtach, null,
                configurationsChangeListener.getAlarmActionBatchSize(), false);
        LOGGER.debug("Delayed ack performed for {} Alarms. ", poIdsBtach.size());
        systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "activeAlarmoutboundThreshold", "number of delayed alarms threshold acked are :" + poIdsBtach.size() ,"");
    }

    private void cancelDelayedAlarmAckTimer() {
        if (delayedAlarmAckTimer != null) {
            try {
                delayedAlarmAckTimer.cancel();
                delayedAlarmAckTimer = null;
            } catch (final Exception exception) {
                LOGGER.error("Exception occurred while cancelling the delayedAlarmAckTimer", exception);
            }
        }
        LOGGER.debug("delayedAlarmAckTimer cancelled and {} batches skipped. ", masterPoIdsList.size());
        this.masterPoIdsList = new LinkedList<List<Long>>();
        this.startTime = 0;
    }
}
