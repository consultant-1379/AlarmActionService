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

package com.ericsson.oss.services.fm.actionservice.alarmthreshold.util;

import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_ACTIONS_BATCH_SIZE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_THRESHOLD_ACK_SMALL_ENM;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_THRESHOLD_FOR_FORCE_ACK;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_THRESHOLD_FOR_NOTIFICATION;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_THRESHOLD_NOTIFICATION_SMALL_ENM;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.DELAYED_ACK_BATCHTIMER_TIMEOUT;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.DELAYED_ACK_CYCLE_GAURD_TIME;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.DELAYED_ACK_CYCLE_TIME_UTILIZATION;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.FM_THRESHOLD_ACK_OF_ALARMS_ON;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.FM_TIMER_ACTION_BATCH_SIZE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.THRESHOLD_ACK_BATCH_SIZE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.THRESHOLD_ACK_CYCLE_TIME_UTILIZATION;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.THRESHOLD_EVENT_BATCH_SIZE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.TIMER_INTERVAL_TO_CHECK_ALARMS;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.modeling.annotation.constraints.NotNull;
import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.AlarmThresholdAcknowledgeTimer;

/**
 * Listens to change in configuration parameter values which are used in alarm action service.
 */
@ApplicationScoped
public class ConfigurationParameterListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationParameterListener.class);

    @Inject
    private AlarmThresholdAcknowledgeTimer activeAlarmsAcknowledgeTimer;

    @Inject
    private ConfigurationParameterUtil configurationParameterUtil;

    @Inject
    @NotNull
    @Configured(propertyName = ALARM_THRESHOLD_FOR_NOTIFICATION)
    private int alarmThresholdForNotification;

    @Inject
    @NotNull
    @Configured(propertyName = ALARM_THRESHOLD_FOR_FORCE_ACK)
    private int alarmThresholdForForceAck;

    @Inject
    @NotNull
    @Configured(propertyName = TIMER_INTERVAL_TO_CHECK_ALARMS)
    private int timerIntervalToCheckAlarms;

    @Inject
    @NotNull
    @Configured(propertyName = FM_TIMER_ACTION_BATCH_SIZE)
    private int fmTimerActionBatchSize;

    @Inject
    @Configured(propertyName = FM_THRESHOLD_ACK_OF_ALARMS_ON)
    private boolean fmThresholdAckOfAlarmsEnabled;

    @Inject
    @Configured(propertyName = ALARM_ACTIONS_BATCH_SIZE)
    private Integer alarmActionBatchSize;

    @Inject
    @Configured(propertyName = DELAYED_ACK_BATCHTIMER_TIMEOUT)
    // Default value 4
    private Integer delayedAckBatchTimerTimeOut;

    @Inject
    @Configured(propertyName = DELAYED_ACK_CYCLE_TIME_UTILIZATION)
    // Default value 80
    private Integer delayedAckCycleTimeUtilization;

    @Inject
    @Configured(propertyName = DELAYED_ACK_CYCLE_GAURD_TIME)
    // Default value 10
    private Integer delayedAckCycleGaurdTime;

    @Inject
    @Configured(propertyName = THRESHOLD_EVENT_BATCH_SIZE)
    // Default value 175
    private Integer thresholdEventBatchSize;

    @Inject
    @Configured(propertyName = THRESHOLD_ACK_BATCH_SIZE)
    // Default value 100
    private Integer thresholdAckBatchSize;

    @Inject
    @Configured(propertyName = THRESHOLD_ACK_CYCLE_TIME_UTILIZATION)
    // Default value 95
    private Integer thresholdAckCycleTimeUtilization;

    public int getTimerIntervalToCheckAlarms() {
        return timerIntervalToCheckAlarms;
    }

    public int getAlarmThresholdForForceAck() {
        return alarmThresholdForForceAck;
    }

    public int getAlarmThresholdForNotification() {
        return alarmThresholdForNotification;
    }

    public int getFmTimerActionBatchSize() {
        return fmTimerActionBatchSize;
    }

    public Integer getAlarmActionBatchSize() {
        return alarmActionBatchSize;
    }

    public boolean isFmThresholdAckOfAlarmsEnabled() {
        return fmThresholdAckOfAlarmsEnabled;
    }

    public Integer getDelayedAckBatchTimerTimeOut() {
        return delayedAckBatchTimerTimeOut;
    }

    public Integer getDelayedAckCycleTimeUtilization() {
        return delayedAckCycleTimeUtilization;
    }

    public Integer getDelayedAckCycleGaurdTime() {
        return delayedAckCycleGaurdTime;
    }

    public Integer getThresholdEventBatchSize() {
        return thresholdEventBatchSize;
    }

    public Integer getThresholdAckBatchSize() {
        return thresholdAckBatchSize;
    }

    public Integer getThresholdAckCycleTimeUtilization() {
        return thresholdAckCycleTimeUtilization;
    }

    public void setAlarmThresholdForNotification(int alarmThresholdForNotification) {
        this.alarmThresholdForNotification = alarmThresholdForNotification;
    }

    public void setAlarmThresholdForForceAck(int alarmThresholdForForceAck) {
        this.alarmThresholdForForceAck = alarmThresholdForForceAck;
    }

    public void observeForAlarmThresholdForNotification(
            @Observes @ConfigurationChangeNotification(propertyName = ALARM_THRESHOLD_FOR_NOTIFICATION) int modifiedValue) {
        if(configurationParameterUtil.getNumberOfReplicas() != 0) {
            if(configurationParameterUtil.getNumberOfReplicas() == 1 && (modifiedValue > ALARM_THRESHOLD_NOTIFICATION_SMALL_ENM)) {
               configurationParameterUtil.updateParameter(ALARM_THRESHOLD_FOR_NOTIFICATION, ALARM_THRESHOLD_NOTIFICATION_SMALL_ENM);
            }
        }else if (configurationParameterUtil.isSmallEnm() && (modifiedValue > ALARM_THRESHOLD_NOTIFICATION_SMALL_ENM)) {
            configurationParameterUtil.updateParameter(ALARM_THRESHOLD_FOR_NOTIFICATION, ALARM_THRESHOLD_NOTIFICATION_SMALL_ENM);
        }

        if (modifiedValue < alarmThresholdForForceAck) {
            LOGGER.info("alarmThresholdForNotification is changed from {} to {}", alarmThresholdForNotification, modifiedValue);
            alarmThresholdForNotification = modifiedValue;
        } else {
            LOGGER.warn("alarmThresholdForNotification should be less than alarmThresholdForForceAck ");
        }
    }

    public void observeForAlarmThresholdForForceAck(
            @Observes @ConfigurationChangeNotification(propertyName = ALARM_THRESHOLD_FOR_FORCE_ACK) int modifiedValue) {
        if(configurationParameterUtil.getNumberOfReplicas() != 0) {
            if(configurationParameterUtil.getNumberOfReplicas() == 1 && (modifiedValue > ALARM_THRESHOLD_ACK_SMALL_ENM)) {
               configurationParameterUtil.updateParameter(ALARM_THRESHOLD_FOR_FORCE_ACK, ALARM_THRESHOLD_ACK_SMALL_ENM);
            }
        }else if (configurationParameterUtil.isSmallEnm() && (modifiedValue > ALARM_THRESHOLD_ACK_SMALL_ENM)) {
            configurationParameterUtil.updateParameter(ALARM_THRESHOLD_FOR_FORCE_ACK, ALARM_THRESHOLD_ACK_SMALL_ENM);
        }

        if (modifiedValue > alarmThresholdForNotification) {
            LOGGER.info("alarmThresholdForForceAck is changed from {} to {} ", alarmThresholdForForceAck, modifiedValue);
            alarmThresholdForForceAck = modifiedValue;
        } else {
            LOGGER.warn("alarmThresholdForForceAck should be greater than alarmThresholdForNotification");
        }
    }

    void observeForTimerIntervalToCheckAlarms(
            @Observes @ConfigurationChangeNotification(propertyName = TIMER_INTERVAL_TO_CHECK_ALARMS) final int modifiedValue) {
        final int oldValue = timerIntervalToCheckAlarms;
        if (oldValue == modifiedValue) {
            LOGGER.info("Discarding the request since listenForTimerIntervalChanges have same value {} ", oldValue);
        } else {
            LOGGER.info("timerIntervalToCheckAlarms is changed from {} to {} ", oldValue, modifiedValue);
            timerIntervalToCheckAlarms = modifiedValue;
            activeAlarmsAcknowledgeTimer.processTimerIntervalChanges(oldValue, timerIntervalToCheckAlarms);
        }
    }

    void observeForFmTimerActionBatchSize(
            @Observes @ConfigurationChangeNotification(propertyName = FM_TIMER_ACTION_BATCH_SIZE) final int modifiedValue) {
        final int oldValue = fmTimerActionBatchSize;
        if (oldValue != modifiedValue) {
            LOGGER.info("timerIntervalToCheckAlarms is changed from {} to {} ", oldValue, modifiedValue);
            fmTimerActionBatchSize = modifiedValue;
        } else {
            LOGGER.info("Discarding change in FM_TIMER_ACTION_BATCH_SIZE value as old {} and new {} values are same.", oldValue, modifiedValue);
        }
    }

    void observeForFmThresholdAckOfAlarmsEnabled(
            @Observes @ConfigurationChangeNotification(propertyName = FM_THRESHOLD_ACK_OF_ALARMS_ON) final boolean modifiedValue) {
        final boolean oldValue = fmThresholdAckOfAlarmsEnabled;
        if (oldValue != modifiedValue) {
            LOGGER.info("FM_THRESHOLD_ACK_OF_ALARMS_ON is changed from {} to {} ", oldValue, modifiedValue);
            fmThresholdAckOfAlarmsEnabled = modifiedValue;
        } else {
            LOGGER.info("Discarding the request since FM_THRESHOLD_ACK_OF_ALARMS_ON is already set to {}.", fmThresholdAckOfAlarmsEnabled);
        }
    }

    void observeAlarmActionBatchSize(@Observes @ConfigurationChangeNotification(propertyName = ALARM_ACTIONS_BATCH_SIZE) final Integer changedValue) {
        LOGGER.info("Received an event to change the ALARM_ACTIONS_BATCH_SIZE value to {} ", changedValue);
        alarmActionBatchSize = changedValue;
    }

    void observeDelayedAckBatchTimerTimeOut(
            @Observes @ConfigurationChangeNotification(propertyName = DELAYED_ACK_BATCHTIMER_TIMEOUT) final Integer changedValue) {
        LOGGER.info("Received an event to change the DELAYED_ACK_BATCHTIMER_TIMEOUT value to {} ", changedValue);
        delayedAckBatchTimerTimeOut = changedValue;
    }

    void observeDelayedAckCycleTimeUtilization(
            @Observes @ConfigurationChangeNotification(propertyName = DELAYED_ACK_CYCLE_TIME_UTILIZATION) final Integer changedValue) {
        LOGGER.info("Received an event to change the DELAYED_ACK_CYCLE_TIME_UTILIZATION value to {} ", changedValue);
        delayedAckCycleTimeUtilization = changedValue;
    }

    void observeDelayedAckCycleGaurdTime(
            @Observes @ConfigurationChangeNotification(propertyName = DELAYED_ACK_CYCLE_GAURD_TIME) final Integer changedValue) {
        LOGGER.info("Received an event to change the DELAYED_ACK_CYCLE_GAURD_TIME value to {} ", changedValue);
        delayedAckCycleGaurdTime = changedValue;
    }

    void observeThresholdEventBatchSize(
            @Observes @ConfigurationChangeNotification(propertyName = THRESHOLD_EVENT_BATCH_SIZE) final Integer changedValue) {
        LOGGER.info("Received an event to change the thresholdEventBatchSize value to {} ", changedValue);
        thresholdEventBatchSize = changedValue;
    }

    void listenForThresholdAckCycleTimeUtilization(
            @Observes @ConfigurationChangeNotification(propertyName = "THRESHOLD_ACK_CYCLE_TIME_UTILIZATION") final int newValue) {
        LOGGER.info("Received an event to change the THRESHOLD_ACK_CYCLE_TIME_UTILIZATION value to {} ", newValue);
        thresholdAckCycleTimeUtilization = newValue;
    }

    void listenForThresholdAckBatchSize(@Observes @ConfigurationChangeNotification(propertyName = "THRESHOLD_ACK_BATCH_SIZE") final int newValue) {
        LOGGER.info("Received an event to change the THRESHOLD_ACK_BATCH_SIZE value to {} ", newValue);
        thresholdAckBatchSize = newValue;
    }
}
