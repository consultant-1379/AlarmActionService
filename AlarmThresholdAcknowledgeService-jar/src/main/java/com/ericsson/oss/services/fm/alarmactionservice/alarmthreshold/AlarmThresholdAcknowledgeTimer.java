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

package com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold;

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

import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterListener;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;

/**
 * Bean responsible for initiating a timer for checking the open alarm count in database. Raises an internal alarm if the alarm count has crossed in
 * threshold limit and also raises clear when count is reduced again.
 */
@Singleton
@Startup
public class AlarmThresholdAcknowledgeTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmThresholdAcknowledgeTimer.class);

    private Timer alarmThresholdTimer;

    @Resource
    private TimerService timerService;

    @Inject
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Inject
    private ConfigurationParameterListener configurationParameterListener;

    @Inject
    private AlarmThresholdInboundHandler activeAlarmInboundHandler;

    @Inject
    private AlarmThresholdOutboundHandler activeAlarmOutboundHandler;

    @Inject
    private DpsAccessManager dpsAccessManager;

    /**
     * Method starts the timer for threshold functionality. Threshold functionality of alarms is enabled by default.
     */
    public void startAlarmThresholdTimer() {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        final int timeInterval = configurationParameterListener.getTimerIntervalToCheckAlarms() * 60 * 1000;
        alarmThresholdTimer = timerService.createIntervalTimer(timeInterval, timeInterval, timerConfig);
        LOGGER.info("The AlarmThresholdTimer ack timer started with time interval {} ", timeInterval);
    }

    @PostConstruct
    public void init() {
        startAlarmThresholdTimer();
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void timeOut() {
        if (alarmActionServiceClusterListener.getMasterState()) {
            LOGGER.debug("Master AlarmActionService Instance, checking active alarm count to raise internal Alarm");
            final int activeAlarmInboundThreshold = configurationParameterListener.getAlarmThresholdForNotification();
            final int activeAlarmoutboundThreshold = configurationParameterListener.getAlarmThresholdForForceAck();
            try {
                final long activeAlarmCount = dpsAccessManager.getActiveAlarmCount();
                if (!activeAlarmInboundHandler.validateAndRaiseInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold)) {
                    activeAlarmInboundHandler.validateAndclearInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
                }
                activeAlarmOutboundHandler.validateAndAcknowledgeActiveClearedAlarms(activeAlarmCount, activeAlarmInboundThreshold,
                        activeAlarmoutboundThreshold);
            } catch (final Exception exception) {
                LOGGER.error("Exception {} occurred while trying to perform the DelayedACK with the threshold mode."
                        + " DelayedACK will be performed in the next timeout.",
                        exception);
            }
        } else {
            LOGGER.debug("Non master FM instance, Not performing the any task");
        }
    }

    public void processTimerIntervalChanges(final int oldvalue, final int modifiedValue) {
        cancelAlarmThresholdTimer();
        startAlarmThresholdTimer();
    }

    @PreDestroy
    public void cleanUp() {
        cancelAlarmThresholdTimer();
    }

    private void cancelAlarmThresholdTimer() {
        if (alarmThresholdTimer != null) {
            alarmThresholdTimer.cancel();
            LOGGER.debug("AlarmThresholdTimer is cancelled");
        } else {
            LOGGER.debug("No Timer for AlarmThreshold is found");
        }
    }
}
