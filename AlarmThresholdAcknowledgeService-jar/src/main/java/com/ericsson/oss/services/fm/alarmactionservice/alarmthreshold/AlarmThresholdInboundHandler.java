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

import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_ACTION_SERVICE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.SEV_CLEARED;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.SEV_CRITICAL;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;

/**
 * Raises internal alarm when the open alarm count in database of higher than the threshold value.
 */
public class AlarmThresholdInboundHandler {

    @Inject
    private DpsAccessManager dpsAccessManager;

    @Inject
    private InternalAlarmSender internalAlarmSender;

    @Inject
    private SystemRecorder systemRecorder;

    public boolean validateAndRaiseInternalAlarm(final long activeAlarmCount, final int activeAlarmInboundThreshold) {
        boolean returnFlag = false;
        if (activeAlarmCount >= activeAlarmInboundThreshold && !dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()) {
            internalAlarmSender.sendInternalAlarmNotification(SEV_CRITICAL, activeAlarmInboundThreshold);

            systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "activeAlarmInboundThreshold", "activeAlarmInboundThreshold",
                    "Active alarm count is reached inbound value");
            returnFlag = true;
        }
        return returnFlag;
    }

    public void validateAndclearInternalAlarm(final long activeAlarmCount, final int activeAlarmInboundThreshold) {
        if (activeAlarmCount < activeAlarmInboundThreshold && dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()) {
            internalAlarmSender.sendInternalAlarmNotification(SEV_CLEARED, activeAlarmInboundThreshold);
            systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "activeAlarmInboundThreshold ", "activeAlarmInboundThreshold ",
                    "Clearing internal alarm as the active alarm count is decresed below the inbound value");
        }
    }
}
