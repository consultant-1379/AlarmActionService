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
import java.util.List;
import java.util.Map;

/**
 * This class provides the interface for the AlarmActionService by taking AlarmActionData as input.
 **/
public class AlarmActionData implements Serializable {

    private static final long serialVersionUID = 1L;
    private String operatorName;
    private String comment;
    private AlarmAction action;
    private List<Long> alarmIds;
    private List<Long> poIds;
    private String objectOfReference;
    private String specificProblem;
    private String probableCause;
    private String eventType;
    private AlarmActionServiceSeverity presentSeverity;
    private Map<String, List<Long>> clearFdnList;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(final String operatorName) {
        this.operatorName = operatorName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public AlarmAction getAlarmAction() {
        return action;
    }

    public void setAction(final AlarmAction action) {
        this.action = action;
    }

    public List<Long> getAlarmIds() {
        return alarmIds;
    }

    public void setAlarmIds(final List<Long> alarmIds) {
        this.alarmIds = alarmIds;
    }

    public String getObjectOfReference() {
        return objectOfReference;
    }

    public void setObjectOfReference(final String objectOfReference) {
        this.objectOfReference = objectOfReference;
    }

    public String getSpecificProblem() {
        return specificProblem;
    }

    public void setSpecificProblem(final String specificProblem) {
        this.specificProblem = specificProblem;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(final String probableCause) {
        this.probableCause = probableCause;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public AlarmActionServiceSeverity getPresentSeverity() {
        return presentSeverity;
    }

    public void setPresentSeverity(final AlarmActionServiceSeverity presentSeverity) {
        this.presentSeverity = presentSeverity;
    }

    public Map<String, List<Long>> getClearFdnList() {
        return clearFdnList;
    }

    public void setClearFdnList(final Map<String, List<Long>> clearFdnList) {
        this.clearFdnList = clearFdnList;
    }

    public List<Long> getPoIds() {
        return poIds;
    }

    public void setPoIds(final List<Long> poIds) {
        this.poIds = poIds;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AlarmActionData [operatorName=").append(operatorName).append(", comment=").append(comment).append(", action=").append(action)
        .append(", alarmIds=").append(alarmIds).append(", poIds=").append(poIds).append(", objectOfReference=").append(objectOfReference)
        .append(", specificProblem=").append(specificProblem).append(", probableCause=").append(probableCause).append(", eventType=")
        .append(eventType).append(", presentSeverity=").append(presentSeverity).append(", clearFdnList=").append(clearFdnList).append("]");
        return builder.toString();
    }
}