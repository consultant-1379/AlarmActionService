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

package com.ericsson.oss.services.alarm.action.service.model;

/**
 * ENUM for severities used for alarm actions.
 */
public enum AlarmActionServiceSeverity {

    INDETERMINATE(0), CRITICAL(1), MAJOR(2), MINOR(3), WARNING(4), CLEARED(5);

    int severity;

    AlarmActionServiceSeverity() {
    }

    AlarmActionServiceSeverity(final int alarmActionSeverity) {
        severity = alarmActionSeverity;
    }

    public int getAlarmActionServiceSeverity() {
        return severity;
    }

}
