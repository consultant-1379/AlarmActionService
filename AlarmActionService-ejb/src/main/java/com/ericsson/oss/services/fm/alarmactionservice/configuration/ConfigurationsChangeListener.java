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

package com.ericsson.oss.services.fm.alarmactionservice.configuration;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_ACTIONS_BATCH_SIZE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.AUTOACK_TIMER_MINUTES;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT_PURGING_FREQUENCY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DELAYED_ACK_BATCHTIMER_TIMEOUT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DELAYED_ACK_CYCLE_GAURD_TIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DELAYED_ACK_CYCLE_TIME_UTILIZATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ENABLED_AUTOACK_ON_MANUAL_CLEARS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EXPONENTIAL_BACKOFF;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMA_DELAYED_ACK_CHECK_INTERVAL;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMA_DELAYED_ACK_OF_ALARMS_ON;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMA_DELAYED_ACK_OF_EVENTS_ON;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMA_TIME_TO_DELAYED_ACK_ALARMS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMA_TIME_TO_DELAYED_ACK_EVENTS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM_TIMER_ACTION_BATCH_SIZE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NUMBER_OF_RETRIES;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SLEEP_TIME_MILLISECONDS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.THRESHOLD_EVENT_BATCH_SIZE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.AUTO_ACK_EVENTS_DELAY_ENABLED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.AUTO_ACK_EVENTS_DELAY_TO_QUEUE;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.modeling.annotation.constraints.NotNull;
import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;
import com.ericsson.oss.services.fm.alarmactionservice.timer.AutoAckRetryTimerHandler;
import com.ericsson.oss.services.fm.alarmactionservice.timer.CommentPurgingTimerService;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckAlarmTimerService;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckEventTimerService;

/**
 * This class listens to the changes of all the configuration parameters related to the Alarm Actions and Triggers respective actions.
 **/
@ApplicationScoped
public class ConfigurationsChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationsChangeListener.class);

    @Inject
    private DelayedAckAlarmTimerService delayedAckAlarmTimerService;

    @Inject
    private DelayedAckEventTimerService delayedAckEventTimerService;

    @Inject
    @Configured(propertyName = FMA_DELAYED_ACK_OF_EVENTS_ON)
    private boolean delayedAckForEventsEnabled;

    @Inject
    @Configured(propertyName = FMA_DELAYED_ACK_OF_ALARMS_ON)
    private boolean delayedAckForAlarmsEnabled;

    @Inject
    @Configured(propertyName = FMA_TIME_TO_DELAYED_ACK_EVENTS)
    private Integer generationAge;

    @Inject
    @Configured(propertyName = FMA_TIME_TO_DELAYED_ACK_ALARMS)
    private Integer clearAge;

    @Inject
    @Configured(propertyName = FMA_DELAYED_ACK_CHECK_INTERVAL)
    private Integer checkfrequency;

    @Inject
    @Configured(propertyName = "downwardAck")
    private Boolean downwardAck;

    @Inject
    private AutoAckRetryTimerHandler autoAckTimerHandler;

    @Inject
    private CommentPurgingTimerService commentPurgingTimerService;

    @Inject
    @Configured(propertyName = NUMBER_OF_RETRIES)
    private Integer numberOfRetries;

    @Inject
    @Configured(propertyName = SLEEP_TIME_MILLISECONDS)
    private Integer sleeptTimeMilliSeconds;

    @Inject
    @Configured(propertyName = AUTOACK_TIMER_MINUTES)
    private Integer autoAckTimerMinutes;

    @Inject
    @Configured(propertyName = ENABLED_AUTOACK_ON_MANUAL_CLEARS)
    private Boolean enableAutoAckOnManualClearedAlarms;

    @Inject
    @Configured(propertyName = EXPONENTIAL_BACKOFF)
    private Double exponentialBackOff;

    @Inject
    @Configured(propertyName = COMMENT_PURGING_FREQUENCY)
    private Integer commentPurgingTimerInterval;

    @Inject
    @Configured(propertyName = ALARM_ACTIONS_BATCH_SIZE)
    private Integer alarmActionBatchSize;

    @Inject
    @Configured(propertyName = FM_TIMER_ACTION_BATCH_SIZE)
    private int fmTimerActionBatchSize;

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
    @NotNull
    @Configured(propertyName = "alarmThresholdInterval")
    private long alarmThresholdInterval;

    @Inject
    @NotNull
    @Configured(propertyName = "alarmDelayToQueue")
    private long alarmDelayToQueue;

    @Inject
    @NotNull
    @Configured(propertyName = "alarmDelayToQueue")
    private long clearAlarmDelayToQueue;

    @Inject
    @Configured(propertyName = AUTO_ACK_EVENTS_DELAY_ENABLED)
    private boolean autoAckEventsDelayEnabled;

    @Inject
    @NotNull
    @Configured(propertyName = AUTO_ACK_EVENTS_DELAY_TO_QUEUE)
    private int autoAckEventsDelayToQueue;

    void observeDelayedAckForEventsEnabled(
            @Observes @ConfigurationChangeNotification(propertyName = FMA_DELAYED_ACK_OF_EVENTS_ON) final Boolean changedValue) {
        LOGGER.info(" FMA_DELAYED_ACK_OF_EVENTS_ON state changed to  {} ", changedValue);
        final boolean oldDelayedAckForEventsEnabled = isDelayedAckForEventsEnabled();
        if (oldDelayedAckForEventsEnabled != changedValue) {
            delayedAckForEventsEnabled = changedValue;
            delayedAckEventTimerService.processDelayedAckForEventsEnabledChanges(delayedAckForEventsEnabled);
        } else {
            LOGGER.info("Discarding the request since delayedAckForEvents is already {} ", delayedAckForEventsEnabled);
        }
    }

    void observeDelayedAckForAlarmsEnabled(
            @Observes @ConfigurationChangeNotification(propertyName = FMA_DELAYED_ACK_OF_ALARMS_ON) final Boolean changedValue) {
        LOGGER.info(" FMA_DELAYED_ACK_OF_ALARMS_ON state changed to  {} ", changedValue);
        final boolean oldDelayedAckForAlarmsEnabled = isDelayedAckForAlarmsEnabled();
        if (oldDelayedAckForAlarmsEnabled != changedValue) {
            delayedAckForAlarmsEnabled = changedValue;
            delayedAckAlarmTimerService.processDelayedAckForAlarmsEnabledChanges(delayedAckForAlarmsEnabled);
        } else {
            LOGGER.info("Discarding the request since FmDelayedAckOfAlarmsOn is already set to {}", delayedAckForAlarmsEnabled);
        }
    }

    void observeForGeneratedAgeChanges(
            @Observes @ConfigurationChangeNotification(propertyName = FMA_TIME_TO_DELAYED_ACK_EVENTS) final Integer changedValue) {
        LOGGER.info(" FMA_TIME_TO_DELAYED_ACK_EVENTS  changed to  {} ", changedValue);
        setGenerationAge(changedValue);
    }

    void observeForClearAgeChanges(
            @Observes @ConfigurationChangeNotification(propertyName = FMA_TIME_TO_DELAYED_ACK_ALARMS) final Integer changedValue) {
        LOGGER.info("FMA_TIME_TO_DELAYED_ACK_ALARMS state changed to  {} ", changedValue);
        clearAge = changedValue;
    }

    void observeForCheckIntervalChanges(
            @Observes @ConfigurationChangeNotification(propertyName = FMA_DELAYED_ACK_CHECK_INTERVAL) final Integer changedValue) {
        LOGGER.info("FMA_DELAYED_ACK_CHECK_INTERVAL state changed to {} ", changedValue);
        final Integer oldCheckFrequency = getCheckfrequency();

        if (changedValue == oldCheckFrequency) {
            LOGGER.info("Discarding the request since FMA_DELAYED_ACK_CHECK_INTERVAL have same value {} ", changedValue);
        } else {
            checkfrequency = changedValue;
            delayedAckEventTimerService.processCheckFrequencyChangesForEvents(checkfrequency);
            delayedAckAlarmTimerService.processCheckFrequencyChangesForAlarms(checkfrequency);
        }
    }

    void listenForDownwardAckChanges(@Observes @ConfigurationChangeNotification(propertyName = "downwardAck") final Boolean changedValue) {
        // do something with changed configuration
        // most probably reconfigure and set some local property to the new value
        LOGGER.info("downwardAck state changed to {} ", changedValue);
        downwardAck = changedValue;
    }

    void observeForNumberOfRetries(@Observes @ConfigurationChangeNotification(propertyName = NUMBER_OF_RETRIES) final Integer changedValue) {
        LOGGER.info("Received an event to change the NUMBER_OF_RETRIES value to {} ", changedValue);
        numberOfRetries = changedValue;
    }

    void observeForSleeptTimeMilliSeconds(
            @Observes @ConfigurationChangeNotification(propertyName = SLEEP_TIME_MILLISECONDS) final Integer changedValue) {
        LOGGER.info("Received an event to change the SLEEP_TIME_MILLISECONDS value to {} ", changedValue);
        sleeptTimeMilliSeconds = changedValue;
    }

    void observeForExponentialBackOffs(@Observes @ConfigurationChangeNotification(propertyName = EXPONENTIAL_BACKOFF) final Double changedValue) {
        LOGGER.info("Received an event to change the EXPONENTIAL_BACKOFF value to {} ", changedValue);
        exponentialBackOff = changedValue;
    }

    void observeForAutoAckTimerMinutes(@Observes @ConfigurationChangeNotification(propertyName = AUTOACK_TIMER_MINUTES) final Integer changedValue) {
        LOGGER.info("Received an event to change the AUTOACK_TIMER_MINUTES value to {} ", changedValue);
        final int oldValue = getAutoAckTimerMinutes().intValue();
        if (oldValue != changedValue) {
            autoAckTimerMinutes = changedValue;
            autoAckTimerHandler.processAutoAckTimerMinutesChanges(autoAckTimerMinutes);
        } else {
            LOGGER.info("Discarding the request to update the AutoAckRetryTimer since the timer has the same value");
        }
    }

    void observeForEnableAutoAckOnManualClearedAlarms(@Observes @ConfigurationChangeNotification(propertyName = ENABLED_AUTOACK_ON_MANUAL_CLEARS) final Boolean changedValue) {
        LOGGER.info("Received an event to change the enableAutoAckOnManualClearedAlarms value to {} ", changedValue);
        final boolean oldValue = getEnableAutoAckOnManualClearedAlarms();
        if (oldValue != changedValue) {
            enableAutoAckOnManualClearedAlarms = changedValue;
        } else {
            LOGGER.info("Discarding the request to update the enableAutoAckOnManualClearedAlarms since the parameter has the same value");
        }
    }

    void observeForCommentPurgingTimerInterval(
            @Observes @ConfigurationChangeNotification(propertyName = COMMENT_PURGING_FREQUENCY) final Integer changedValue) {
        LOGGER.info(" COMMENT_PURGING_FREQUENCY changed to  {} ", changedValue);
        final int oldValue = getCommentPurgingTimerInterval().intValue();
        if (oldValue != changedValue) {
            commentPurgingTimerInterval = changedValue;
            commentPurgingTimerService.processCommentPurgingTimerChangeEvent(commentPurgingTimerInterval);
        } else {
            LOGGER.info("Discarding the request to update the CommentPurgingTimer since the timer has the same value");
        }
    }

    void observeAlarmActionBatchSize(@Observes @ConfigurationChangeNotification(propertyName = ALARM_ACTIONS_BATCH_SIZE) final Integer changedValue) {
        LOGGER.info("Received an event to change the ALARM_ACTIONS_BATCH_SIZE value to {} ", changedValue);
        alarmActionBatchSize = changedValue;
    }

    void observeFmTimerActionBatchSize(
            @Observes @ConfigurationChangeNotification(propertyName = FM_TIMER_ACTION_BATCH_SIZE) final Integer changedValue) {
        LOGGER.info("Received an event to change the FM_TIMER_ACTION_BATCH_SIZE value to {} ", changedValue);
        fmTimerActionBatchSize = changedValue.intValue();
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

    /**
     * Method listens for @ConfigurationChangeNotification on alarmThresholdInterval and to assess delay to NBI JMS Queue.
     * @param long newValue
     */
    void listenForAalarmThresholdInterval(
            @Observes @ConfigurationChangeNotification(propertyName = "alarmThresholdInterval") final long newValue) {
        LOGGER.info("alarmThresholdInterval attribute is changed and new value for alarmThresholdInterval is: {}", newValue);
        alarmThresholdInterval = newValue;
    }

    /**
     * Method listens for @ConfigurationChangeNotification on alarmDelayToQueue and forward delay to NBI JMS Queue.
     * @param long newValue
     */
    void listenForAlarmDelayToQueue(
            @Observes @ConfigurationChangeNotification(propertyName = "alarmDelayToQueue") final long newValue) {
        LOGGER.info("alarmDelayToQueue attribute is changed and new value for alarmDelayToQueue is: {}", newValue);
        alarmDelayToQueue = newValue;
    }

    /**
     * Method listens for @ConfigurationChangeNotification on clearAlarmDelayToQueue and forward delay to NBI JMS Queue. changes.
     * @param long newValue
     */
    void listenForClearAlarmDelayToQueue(
            @Observes @ConfigurationChangeNotification(propertyName = "clearAlarmDelayToQueue") final long newValue) {
        LOGGER.info("clearAlarmDelayToQueue attribute is changed and new value for clearAlarmDelayToQueue is: {}", newValue);
        clearAlarmDelayToQueue = newValue;
    }

    /**
     * Method listens for @ConfigurationChangeNotification on autoAckEventsDelayEnabled to FM CoreOut Queue or not.
     * @param Boolean newValue
     */
    void observeForAutoAckDelayEventsEnabled(
            @Observes @ConfigurationChangeNotification(propertyName = AUTO_ACK_EVENTS_DELAY_ENABLED) final Boolean changedValue) {
        LOGGER.info(" AUTO_ACK_EVENTS_DELAY_ENABLED state changed to  {} ", changedValue);
        final boolean oldDelayedAutoAckForEventsEnabled = isAutoAckEventsDelayEnabled();
        if (oldDelayedAutoAckForEventsEnabled != changedValue) {
      autoAckEventsDelayEnabled = changedValue;
        } else {
            LOGGER.info("Discarding the request since AutoAckEventsDelay to FM CoreOut Queue is already {} ", autoAckEventsDelayEnabled);
        }
    }

    /**
     * Method listens for @ConfigurationChangeNotification on autoAckEventsDelayToQueue and forward delay to FM CoreOut Queue.
     * @param int newValue
     */
    void listenForAutoAckEventsDelayToQueue(
            @Observes @ConfigurationChangeNotification(propertyName = AUTO_ACK_EVENTS_DELAY_TO_QUEUE) final int newValue) {
        LOGGER.info("autoAckEventsDelayToQueue attribute is changed and new value for autoAckEventsDelayToQueue is: {}", newValue);
        autoAckEventsDelayToQueue = newValue;
    }

    public Boolean getDownwardAck() {
        return downwardAck;
    }

    public boolean isDelayedAckForEventsEnabled() {
        return delayedAckForEventsEnabled;
    }

    public boolean isDelayedAckForAlarmsEnabled() {
        return delayedAckForAlarmsEnabled;
    }

    public Integer getGenerationAge() {
        return generationAge;
    }

    public void setGenerationAge(final Integer generationAge) {
        this.generationAge = generationAge;
    }

    public Integer getClearAge() {
        return clearAge;
    }

    public Integer getCheckfrequency() {
        return checkfrequency;
    }

    public Integer getNumberOfRetries() {
        return numberOfRetries;
    }

    public Integer getSleeptTimeMilliSeconds() {
        return sleeptTimeMilliSeconds;
    }

    public Integer getAutoAckTimerMinutes() {
        return autoAckTimerMinutes;
    }

    public Boolean getEnableAutoAckOnManualClearedAlarms() {
        return enableAutoAckOnManualClearedAlarms;
    }

    public Double getExponentialBackOff() {
        return exponentialBackOff;
    }

    public Integer getCommentPurgingTimerInterval() {
        return commentPurgingTimerInterval;
    }

    public Integer getAlarmActionBatchSize() {
        return alarmActionBatchSize;
    }

    public int getFmTimerActionBatchSize() {
        return fmTimerActionBatchSize;
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

    public long getAlarmThresholdInterval() {
        return alarmThresholdInterval;
    }

    public long getAlarmDelayToQueue() {
        return alarmDelayToQueue;
    }

    public long getClearAlarmDelayToQueue() {
        return clearAlarmDelayToQueue;
    }

    public boolean isAutoAckEventsDelayEnabled() {
        return autoAckEventsDelayEnabled;
    }

    public int getAutoAckEventsDelayToQueue() {
        return autoAckEventsDelayToQueue;
    }
}