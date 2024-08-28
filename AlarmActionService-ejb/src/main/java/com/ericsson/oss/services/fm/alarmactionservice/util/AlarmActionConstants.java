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

package com.ericsson.oss.services.fm.alarmactionservice.util;

/**
 * This class provides the constants which can be used trough out Alarm Action Service.
 **/
public final class AlarmActionConstants {
    public static final String ALARM_NOTFOUND = "Alarm Not Found";
    public static final String ALARM_NOTFOUND_UNDERFDN = "Alarm Not Found Under the FDN";
    public static final String NO_ALARMS_UNDERFDN = "No Alarms found  Under FDN";
    public static final String ALREADY_ACK = "Alarm Is already Acknowledged";
    public static final String ALREADY_UNACK = "Alarm Is already Un-Acknowledged";
    public static final String ALREADY_CLEAR = "Alarm Is already Cleared";
    public static final String FDN_NOTFOUND = "FDN Not Found";
    public static final String ALARM_NOTMATCH = "Alarm Not Found Under the FDN";
    public static final String FDN_NOTVALID = "Invalid FDN";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String ACK = "ACTIVE_ACKNOWLEDGED";
    public static final String UNACK = "ACTIVE_UNACKNOWLEDGED";
    public static final String CLEAREDUNACK = "CLEARED_UNACKNOWLEDGED";
    public static final String CLEARED_ACKNOWLEDGED = "CLEARED_ACKNOWLEDGED";
    public static final String ALARM_ERROR = "Error Alarm Found";
    public static final String ERROR_UNACK = "Error Alarm can't be Un-Acknowledged";
    public static final String ERROR_CLEAR = "Error Alarm can't be Cleared Directly";
    public static final String FDN_MANDATORY_CLEAR = "FDN is mandatory for clear operation";
    public static final String FDN_MANDATORY_COMMENT = "FDN is mandatory for Commenting";
    public static final String FDN_IMPROPER = "Improper FDN";
    public static final String NO_ALARMS_QUERY = "No Alarms found for given Query";
    public static final String NOT_PROPER_INPUT = "Not proper Input";
    public static final String FM = "FM";
    public static final String OSS_EDT = "oss_edt";
    public static final String DOWNARD_ACK_SUPPORTED_NODETYPES = "DownwardAckSupportedNodeTypes";
    public static final String ALARM = "ALARM";
    public static final String EVENT = "EVENT";
    public static final String OPENALARM = "OpenAlarm";
    public static final String ALARMDESCRIPTION = "alarmDescription";
    public static final String ERROR = "ERROR_MESSAGE";
    public static final String REPEATED_ERROR = "REPEATED_ERROR_MESSAGE";
    public static final String SUCCESS = "SUCCESS";
    public static final String ACTION_NOT_SPECIFIED = "Action to be performed is not specified";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String NO_SYNCHABLE_ALARM = "NO_SYNCHABLE_ALARM";
    public static final String REPEATED_ALARM = "REPEATED_ALARM";
    public static final String REPEATED_ERROR_MESSAGE = "REPEATED_ERROR_MESSAGE";
    public static final String REPEATED_NON_SYNCHABLE = "REPEATED_NON_SYNCHABLE";
    public static final String LESS_SEVERE = "LESS_SEVERE";
    public static final String MORE_SEVERE = "MORE_SEVERE";
    public static final String NO_CHANGE = "NO_CHANGE";
    public static final String CLEAR_LIST = "CLEAR_LIST";
    public static final String CLEARALL = "CLEARALL";
    public static final String HB_FAILURE_NO_SYNCH = "HB_FAILURE_NO_SYNCH";
    public static final String SYNC_NETWORK = "SYNC_NETWORK";
    public static final String OSCILLATORY_HB_ALARM = "OSCILLATORY_HB_ALARM";
    public static final String CLI = "CLI";
    public static final String MTR = "MTR";
    public static final String ALARMNUMBER = "alarmNumber";
    public static final String ALARMSTATE = "alarmState";
    public static final String OBJECTOFREFERENCE = "objectOfReference";
    public static final String FDN = "fdn";
    public static final String COLON_DELIMITER = ":";
    public static final char NULL_CHARACTER_DELIMITER = '\u0000';
    public static final String HASH_DELIMITER = "#";
    public static final String UNDERSCORE_DELIMITER = "_";
    public static final String RECORDTYPE = "recordType";
    public static final String ACKOPERATOR = "ackOperator";
    public static final String ACKTIME = "ackTime";
    public static final String LASTUPDATED = "lastUpdated";
    public static final String LASTALARMOPERATION = "lastAlarmOperation";
    public static final String UTC = "UTC";
    public static final String COMMENTTEXT = "commentText";
    public static final String COMMENTTIME = "commentTime";
    public static final String COMMENTOPERATOR = "commentOperator";
    public static final String PRESENTSEVERITY = "presentSeverity";
    public static final String PREVIOUSSEVERITY = "previousSeverity";
    public static final String PSEUDO_PRESENT_SEVERITY = "pseudoPresentSeverity";
    public static final String PSEUDO_PREVIOUS_SEVERITY = "pseudoPreviousSeverity";
    public static final String EVENTTIME = "eventTime";
    public static final String CEASETIME = "ceaseTime";
    public static final String MANUALCEASE = "manualCease";
    public static final String ALARMINGOBJECT = "alarmingObject";
    public static final String SPECIFICPROBLEM = "specificProblem";
    public static final String PROBABLECAUSE = "probableCause";
    public static final String EVENTTYPE = "eventType";
    public static final String BACKUPSTATUS = "backupStatus";
    public static final String BACKUPOBJECTINSTANCE = "backupObjectInstance";
    public static final String PROPOSEDREPAIRACTION = "proposedRepairAction";
    public static final String ALARMID = "alarmId";
    public static final String EXTERNAL_EVENTID = "externalEventId";
    public static final String GENERATED_ALARMID = "generatedAlarmId";
    public static final String CEASEOPERATOR = "ceaseOperator";
    public static final String INSERTTIME = "insertTime";
    public static final String SYNCSTATE = "syncState";
    public static final String TRENDINDICATION = "trendIndication";
    public static final String HISTORYALARMPOID = "historyAlarmPOId";
    public static final String CORRELATEDEVENTPOID = "correlatedeventPOId";
    public static final String REPEATCOUNT = "repeatCount";
    public static final String SYNCALARM = "SYNCHRONIZATION_ALARM";
    public static final String HBALARM = "HEARTBEAT_ALARM";
    public static final String SYNCABORT = "SYNCHRONIZATION_ABORTED";
    public static final String SYNCIGNORED = "SYNCHRONIZATION_IGNORED";
    public static final String UPDATE = "UPDATE";
    public static final String NODESUSPENDED = "NODE_SUSPENDED";
    public static final String OUT_OF_SYNC = "OUT_OF_SYNC";
    public static final String CRITICAL = "CRITICAL";
    public static final String MAJOR = "MAJOR";
    public static final String MINOR = "MINOR";
    public static final String INDETERMINATE = "INDETERMINATE";
    public static final String WARNING = "WARNING";
    public static final String CLEARED = "CLEARED";
    public static final String UNDEFINED = "UNDEFINED";
    public static final String NEW = "NEW";
    public static final String CLEAR = "CLEAR";
    public static final String CHANGE = "CHANGE";
    public static final String ACKSTATE_CHANGE = "ACKSTATE_CHANGE";
    public static final String COMMENT = "COMMENT";
    public static final String CLOSED = "CLOSED";
    public static final String ACKNOWLEDGE = "ACKNOWLEDGE";
    public static final String UNACKNOWLEDGE = "UNACKNOWLEDGE";
    public static final String FMFUNCTION = ",FmFunction=1";
    public static final String ALARMSUPPRESSEDALARM = "ALARM_SUPPRESSED_ALARM";
    public static final String ALARMSUPPRESSED_SP = "AlarmSuppressedMode";
    public static final String TECHNICIANPRESENT = "TECHNICIAN_PRESENT";
    public static final String TECHNICIANPRESENT_SP = "FieldTechnicianPresent";
    public static final String ALARMSUPPRESSEDSTATE = "alarmSuppressedState";
    public static final String TECHNICIANPRESENTSTATE = "technicianPresentState";
    public static final String EVENTPOID = "eventPoId";
    public static final String FMA_DELAYED_ACK_OF_EVENTS_ON = "FMA_DELAYED_ACK_OF_EVENTS_ON";
    public static final String FMA_DELAYED_ACK_OF_ALARMS_ON = "FMA_DELAYED_ACK_OF_ALARMS_ON";
    public static final String FMA_TIME_TO_DELAYED_ACK_EVENTS = "FMA_TIME_TO_DELAYED_ACK_EVENTS";
    public static final String FMA_TIME_TO_DELAYED_ACK_ALARMS = "FMA_TIME_TO_DELAYED_ACK_ALARMS";
    public static final String FMA_DELAYED_ACK_CHECK_INTERVAL = "FMA_DELAYED_ACK_CHECK_INTERVAL";
    public static final String COMMENT_PURGING_FREQUENCY = "COMMENT_PURGING_FREQUENCY";
    public static final String ADDITIONAL_INFORMATION = "additionalInformation";
    public static final String PROBLEMTEXT = "problemText";
    public static final String PROBLEMDETAIL = "problemDetail";
    /** Configuration parameters for AutoAck use case. **/
    public static final String NUMBER_OF_RETRIES = "NUMBER_OF_RETRIES";
    public static final String SLEEP_TIME_MILLISECONDS = "SLEEP_TIME_MILLISECONDS";
    public static final String AUTOACK_TIMER_MINUTES = "AUTOACK_TIMER_MINUTES";
    public static final String EXPONENTIAL_BACKOFF = "EXPONENTIAL_BACKOFF";
    public static final int AUTOACK_BATCH_SIZE = 10;
    public static final String ENABLED_AUTOACK_ON_MANUAL_CLEARS = "enableAutoAckOnManualClearedAlarms";
    public static final String ALARM_ACTIONS_BATCH_SIZE = "ALARM_ACTIONS_BATCH_SIZE";
    public static final String FM_TIMER_ACTION_BATCH_SIZE = "FM_TIMER_ACTION_BATCH_SIZE";
    public static final String DELAYED_ACK_BATCHTIMER_TIMEOUT = "DELAYED_ACK_BATCHTIMER_TIMEOUT";
    public static final String DELAYED_ACK_CYCLE_TIME_UTILIZATION = "DELAYED_ACK_CYCLE_TIME_UTILIZATION";
    public static final String DELAYED_ACK_CYCLE_GAURD_TIME = "DELAYED_ACK_CYCLE_GAURD_TIME";
    public static final String THRESHOLD_EVENT_BATCH_SIZE = "thresholdEventBatchSize";
    public static final String ALARM_ROUTING_SERVICE = "AlarmRoutingService";
    public static final String ALARM_ACTION_SERVICE = "AlarmActionService";

    public static final int AUTO_ACK_BATCH_SIZE = 10;

    public static final int COMMENT_PURGER_BATCH_SIZE = 100;
    public static final String ADDTIONAL_INFORMATION = "additionalInformation";
    public static final String FMALARM_SUPERVISION_MO_SUFFIX = ",FmAlarmSupervision=1";
    public static final String SOURCETYPE = "sourceType";

    public static final String VISIBILITY = "visibility";
    public static final String PROCESSING_TYPE = "processingType";
    public static final String FMX_GENERATED = "fmxGenerated";

    // Added for comment history.
    public static final String INDEX = "index";
    public static final String COMMENTS = "comments";
    public static final String COMMENT_OPERATION = "CommentOperation";
    public static final String OPEN_ALARM_POID = "openAlarmPOId";
    public static final String VERSION = "1.0.1";

    // Exception Message while performing actions.This exception might be due to optimistic lock exception or issue with DB.
    public static final String EXCEPTION_MESSAGE = "Unable to perform Action. The Object being accessed might be already in use or issue with DB."
            + " Please try again later";
    // Exception Message while performing actions. This exception message might be due to DB access failure.
    public static final String DB_EXCEPTION_MESSAGE = "Unable to perform DB operations. Please try again later";

    public static final String QUEUE_EXCEPTION_MESSAGE = "Unable to send alarm event to queue. Please try again later";

    public static final String FM_CORE_OUT_QUEUE = "//global/FmCoreOutQueue";
    public static final String FM_NORTH_BOUND_QUEUE = "//global/FMNorthBoundQueue";
    public static final String FM_SNMP_NORTH_BOUND_TOPIC = "//global/FMSnmpNorthBoundTopic";
    public static final String ALARM_RECEIVED_TIME = "alarmReceivedTime";

    public static final String LAST_DELIVERED = "lastDelivered";
    public static final String ALARM_ACTIONS_CACHE = "AlarmActionsCache";
    public static final int MILLIS_TO_SECONDS = 1000;
    public static final int MAX_READ_ENTRIES_FROM_CACHE = 10000;
    public static final String ACTIONS_TO_QUEUES = "ACTIONS_TO_QUEUES";
    public static final String ACTIONS_TO_HISOTRY = "ACTIONS_TO_HISOTRY";
    public static final String FAILED_ACTIONS_REST_MESSAGE = "Failed to store alarm action updates in history and status";

    public static final String AUTO_ACK_EVENTS_DELAY_ENABLED = "autoAckEventsDelayEnabled";
    public static final String AUTO_ACK_EVENTS_DELAY_TO_QUEUE = "autoAckEventsDelayToQueue";

    public static final String HOST_PARAM = "host";
    public static final String ALARM_HISTORY_SERVICE_HOST = "alarmhistory-service";
    public static final String ALARM_HISTORY_SERVICE_URL = "http://alarmhistory-service:8080/alarm-change-listener/history/alarm/actionupdates";

    private AlarmActionConstants() {
    }
}
