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

import java.io.Serializable;

/**
 * This class is POJO which is used as output response for any alarm action operation invoked using alarm action service.
 */
public class AlarmActionResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    String response;
    String objectOfReference;
    String alarmNumber;
    String eventPoId;

    public String getResponse() {
        return response;
    }

    public void setResponse(final String response) {
        this.response = response;
    }

    public String getObjectOfReference() {
        return objectOfReference;
    }

    public void setObjectOfReference(final String objectOfReference) {
        this.objectOfReference = objectOfReference;
    }

    public String getAlarmNumber() {
        return alarmNumber;
    }

    public void setAlarmNumber(final String alarmNumber) {
        this.alarmNumber = alarmNumber;
    }

    public String getEventPoId() {
        return eventPoId;
    }

    public void setEventPoId(final String eventPoId) {
        this.eventPoId = eventPoId;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AlarmActionResponse [response=").append(response).append(", objectOfReference=").append(objectOfReference)
        .append(", alarmNumber=").append(alarmNumber).append(", eventPoId=").append(eventPoId).append("]");
        return builder.toString();
    }
}
