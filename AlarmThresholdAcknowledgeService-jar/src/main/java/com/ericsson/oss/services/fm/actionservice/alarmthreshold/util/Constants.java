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

package com.ericsson.oss.services.fm.actionservice.alarmthreshold.util;

/**
 * Utility for constants.
 */
public final class Constants {
    public static final String SEV_CRITICAL = "CRITICAL";
    public static final String SEV_CLEARED = "CLEARED";
    public static final String CLEAR = "CLEAR";
    public static final String NOTIF_TYPE_ALARM = "ALARM";
    public static final String THRESHOLD_LIMIT_PROBABLE_CAUSE = "thresholdCrossed";
    public static final String INTERNAL_ALARM_FDN = "ManagementSystem=ENM";
    public static final String FDN = "fdn";
    public static final String OSS_FM = "FM";
    public static final String OPEN_ALARM = "OpenAlarm";
    public static final String PRESENT_SEVERITY = "presentSeverity";
    public static final String ACTIVE_ACKNOWLEDGED = "ACTIVE_ACKNOWLEDGED";
    public static final String ACTIVE_UNACKNOWLEDGED = "ACTIVE_UNACKNOWLEDGED";
    public static final String CLEARED_ACKNOWLEDGED = "CLEARED_ACKNOWLEDGED";
    public static final String SPECIFIC_PROBLEM = "specificProblem";
    public static final String ALARM_STATE = "alarmState";
    public static final String CLASSIC_MODE = "CLASSIC";
    public static final String THRESHOLD_MODE = "THRESHOLD";
    public static final String DELAYED_ACK_MODE = "delayedAckMode";
    public static final String THRESHOLD_LIMIT_SPECIFIC_PROBLEM = "Open alarm count crossed Threshold limit";
    public static final String THRESHOLD_LIMIT_EVENT_TYPE = "qualityOfServiceAlarm";
    public static final String ADDITIONAL_TEXT = "additionalText";
    public static final String APPLICATION_NAME = "ENMFaultManagement";
    public static final String ALARM_ACTION_SERVICE = "AlarmActionService";

    public static final String FM_TIMER_ACTION_BATCH_SIZE = "FM_TIMER_ACTION_BATCH_SIZE";
    public static final String FM_THRESHOLD_ACK_OF_ALARMS_ON = "FM_THRESHOLD_ACK_OF_ALARMS_ON";
    public static final String TIMER_INTERVAL_TO_CHECK_ALARMS = "timerIntervalToCheckAlarms";
    public static final String ALARM_THRESHOLD_FOR_FORCE_ACK = "alarmThresholdForForceAck";
    public static final String ALARM_THRESHOLD_FOR_NOTIFICATION = "alarmThresholdForNotification";
    public static final String DELAYED_ACK_BATCHTIMER_TIMEOUT = "DELAYED_ACK_BATCHTIMER_TIMEOUT";
    public static final String DELAYED_ACK_CYCLE_TIME_UTILIZATION = "DELAYED_ACK_CYCLE_TIME_UTILIZATION";
    public static final String DELAYED_ACK_CYCLE_GAURD_TIME = "DELAYED_ACK_CYCLE_GAURD_TIME";
    public static final String THRESHOLD_EVENT_BATCH_SIZE = "thresholdEventBatchSize";
    public static final String ALARM_ACTIONS_BATCH_SIZE = "ALARM_ACTIONS_BATCH_SIZE";
    public static final String THRESHOLD_ACK_BATCH_SIZE = "THRESHOLD_ACK_BATCH_SIZE";
    public static final String THRESHOLD_ACK_CYCLE_TIME_UTILIZATION = "THRESHOLD_ACK_CYCLE_TIME_UTILIZATION";
    public static final int ALARM_THRESHOLD_NOTIFICATION_SMALL_ENM = 40000;
    public static final int ALARM_THRESHOLD_ACK_SMALL_ENM = 60000;

    private Constants() {}
}
