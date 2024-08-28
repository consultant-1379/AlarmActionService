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

package com.ericsson.oss.services.fm.alarmactionservice.timer;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_ROUTING_SERVICE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.AUTOACK_BATCH_SIZE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
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

import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.impl.AlarmActionsBatchManager;

/**
 * Bean responsible for starting the timer for retrying the acknowledgement for the PO id's which are failed during auto acknowledgement.
 */
@Singleton
@Startup
public class AutoAckRetryTimerHandler {

    public static Set<Long> failedPoids = new HashSet<Long>();

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoAckRetryTimerHandler.class);

    @Resource
    private TimerService service;

    private Timer autoAckRetryTimer;

    @Inject
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    @PostConstruct
    public void init() {
        startAlarmTimer(configurationsChangeListener.getAutoAckTimerMinutes());
    }

    public void startAlarmTimer(final Integer checkInterval) {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        autoAckRetryTimer = service.createIntervalTimer((long) checkInterval * 60 * 1000, (long) checkInterval * 60 * 1000, timerConfig);
        LOGGER.info("Timer for AutoAckRetry is started with {} ", checkInterval * 60 * 1000);
    }

    public void processAutoAckTimerMinutesChanges(final int autoAckTimer) {
        ackAlarmsInBatches();
        autoAckRetryTimer.cancel();
        startAlarmTimer(autoAckTimer);
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout(final Timer timer) {
        LOGGER.debug("In the handleTimeout method for the AutoAckRetry Timer");
        if (failedPoids.isEmpty()) {
            LOGGER.debug("No Failed PoIds found in the List.");
        } else {
            ackAlarmsInBatches();
        }
    }

    private void ackAlarmsInBatches() {
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setOperatorName(ALARM_ROUTING_SERVICE);
        LOGGER.debug("Retrying the AutoAck for the FailedPoIds {}", failedPoids);
        try {
            alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, new ArrayList<Long>(failedPoids), null, AUTOACK_BATCH_SIZE, true);
        } catch (final Exception exception) {
            LOGGER.error("Exception {} while retrying the ACK on the poIds {}, not removed from the failedPoids", exception, failedPoids);
        }
    }
}
