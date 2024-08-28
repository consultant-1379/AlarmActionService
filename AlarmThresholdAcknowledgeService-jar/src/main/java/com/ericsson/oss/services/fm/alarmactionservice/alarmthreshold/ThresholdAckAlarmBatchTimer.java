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

package com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterListener;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;

/**
 * Responsible for acknowledging batch of alarms based on the timer's timeout.
 **/
@Stateless
public class ThresholdAckAlarmBatchTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThresholdAckAlarmBatchTimer.class);

    private LinkedList<List<Long>> masterPoIdsList = new LinkedList<List<Long>>();

    private Timer thresholdAlarmAckTimer;

    private final AtomicBoolean ackInProgress = new AtomicBoolean();

    @Resource
    private TimerService timerService;

    @Inject
    private DpsAccessManager dpsAccessManager;

    @Inject
    private ConfigurationParameterListener configurationParameterListener;

    @Inject
    private AlarmThresholdInboundHandler alarmThresholdInboundHandler;

    public void startThresholdAlarmAckTimer(final LinkedList<List<Long>> poIdSubBatches, final long startTime) {
        ackInProgress.set(true);
        masterPoIdsList.addAll(poIdSubBatches);
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        thresholdAlarmAckTimer = timerService.createIntervalTimer(0, (long) configurationParameterListener.getDelayedAckBatchTimerTimeOut() * 1000,
                timerConfig);
        LOGGER.debug("The alarm ack timer for started with {} alarm batches and startTime", masterPoIdsList.size(), startTime);
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout() {
        try {
            final int activeAlarmInboundThreshold = configurationParameterListener.getAlarmThresholdForNotification();
            if (!masterPoIdsList.isEmpty()) {
                final List<Long> poIdsBatch = masterPoIdsList.get(0);
                dpsAccessManager.acknowledgeClearedAlarms(poIdsBatch);
                masterPoIdsList.remove();

                if (masterPoIdsList.isEmpty()) {
                    final long activeAlarmCount = dpsAccessManager.getActiveAlarmCount();
                    alarmThresholdInboundHandler.validateAndclearInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
                    LOGGER.info("Master poIds list is empty: {}", masterPoIdsList.size());
                    ackInProgress.set(false);
                    cancelThresholdAlarmAckTimer();
                }
            } else {
                LOGGER.info("Master poIds list is empty: {}", masterPoIdsList.size());
                ackInProgress.set(false);
                cancelThresholdAlarmAckTimer();
            }
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred while trying to perform the threshold on Alarms. Threshold will be performed in the next timeout.",
                    exception);
        }
    }

    public AtomicBoolean getAckInProgress() {
        return ackInProgress;
    }

    private void cancelThresholdAlarmAckTimer() {
        if (thresholdAlarmAckTimer != null) {
            try {
                thresholdAlarmAckTimer.cancel();
                thresholdAlarmAckTimer = null;
            } catch (final Exception exception) {
                LOGGER.error("Exception occurred while cancelling the thresholdAlarmAckTimer", exception);
            }
        }
        LOGGER.debug("thresholdAlarmAckTimer cancelled and {} batches skipped. ", masterPoIdsList.size());
        masterPoIdsList = new LinkedList<List<Long>>();
    }
}
