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
 * ENUM for different alarm action operations.
 */
public enum AlarmAction {

    ACK(1), UNACK(2), CLEAR(3), COMMENT(4), SHOWHIDE(5);

    int action;

    AlarmAction() {
    }

    AlarmAction(final int alarmAction) {
        action = alarmAction;
    }

    public int getAlarmAction() {
        return action;
    }
}
