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

package com.ericsson.oss.services.fm.alarmactionservice.util;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKSTATE_CHANGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ADDITIONAL_INFORMATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMINGOBJECT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.BACKUPOBJECTINSTANCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.BACKUPSTATUS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASEOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASETIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CHANGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAREDUNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLOSED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CORRELATEDEVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CRITICAL;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMX_GENERATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HISTORYALARMPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INDETERMINATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INSERTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTALARMOPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LESS_SEVERE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MAJOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MINOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MORE_SEVERE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NEW;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NO_CHANGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PRESENTSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PREVIOUSSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBABLECAUSE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBLEMDETAIL;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBLEMTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROCESSING_TYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROPOSEDREPAIRACTION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.RECORDTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.REPEATCOUNT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SPECIFICPROBLEM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SYNCSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TRENDINDICATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNDEFINED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.VISIBILITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.WARNING;

import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_1;
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_2;
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.ROOT;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.fm.common.addinfo.CorrelationType;
import com.ericsson.oss.services.fm.common.addinfo.TargetAdditionalInformationHandler;
import com.ericsson.oss.services.fm.models.processedevent.FMProcessedEventType;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedEventSeverity;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedEventState;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedEventTrendIndication;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedLastAlarmOperation;

/**
 * Utility class for building {@link ProcessedAlarmEvent} with alarm attributes map provided.
 */
public final class ProcessedAlarmEventBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedAlarmEventBuilder.class);

    private static final Map<String, ProcessedEventSeverity> EVENT_SEVERITIES;
    private static final Map<String, ProcessedLastAlarmOperation> LAST_ALARM_OPERATIONS;

    private ProcessedAlarmEventBuilder() {
    }

    static {
        final Map<String, ProcessedEventSeverity> severityMap = new HashMap<String, ProcessedEventSeverity>();
        final Map<String, ProcessedLastAlarmOperation> operationMap = new HashMap<String, ProcessedLastAlarmOperation>();
        severityMap.put(UNDEFINED, ProcessedEventSeverity.UNDEFINED);
        severityMap.put(INDETERMINATE, ProcessedEventSeverity.INDETERMINATE);
        severityMap.put(CLEARED, ProcessedEventSeverity.CLEARED);
        severityMap.put(MAJOR, ProcessedEventSeverity.MAJOR);
        severityMap.put(CRITICAL, ProcessedEventSeverity.CRITICAL);
        severityMap.put(MINOR, ProcessedEventSeverity.MINOR);
        severityMap.put(WARNING, ProcessedEventSeverity.WARNING);
        EVENT_SEVERITIES = Collections.unmodifiableMap(severityMap);

        operationMap.put(UNDEFINED, ProcessedLastAlarmOperation.UNDEFINED);
        operationMap.put(NEW, ProcessedLastAlarmOperation.NEW);
        operationMap.put(CHANGE, ProcessedLastAlarmOperation.CHANGE);
        operationMap.put(ACKSTATE_CHANGE, ProcessedLastAlarmOperation.ACKSTATE_CHANGE);
        operationMap.put(CLEAR, ProcessedLastAlarmOperation.CLEAR);
        operationMap.put(COMMENT, ProcessedLastAlarmOperation.COMMENT);
        LAST_ALARM_OPERATIONS = Collections.unmodifiableMap(operationMap);
    }

    public static ProcessedAlarmEvent getProcessedAlarm(final Map<String, Object> alarmAttributes) {
        final ProcessedAlarmEvent processedAlarmEvent = new ProcessedAlarmEvent();

        rebuildCorrelationInformation(alarmAttributes);

        final Set<String> alarmAttributeNames = alarmAttributes.keySet();
        for (final String alarmAttributeName : alarmAttributeNames) {
            final Object attributeValue = alarmAttributes.get(alarmAttributeName);
            if (attributeValue != null) {
                switch (alarmAttributeName) {
                    case OBJECTOFREFERENCE:
                        processedAlarmEvent.setObjectOfReference((String) attributeValue);
                        break;
                    case FDN:
                        processedAlarmEvent.setFdn((String) attributeValue);
                        break;
                    case EVENTTYPE:
                        processedAlarmEvent.setEventType((String) attributeValue);
                        break;
                    case EVENTPOID:
                        processedAlarmEvent.setEventPOId((Long) attributeValue);
                        break;
                    case EVENTTIME:
                        processedAlarmEvent.setEventTime((Date) attributeValue);
                        break;
                    case PROBABLECAUSE:
                        processedAlarmEvent.setProbableCause((String) attributeValue);
                        break;
                    case SPECIFICPROBLEM:
                        processedAlarmEvent.setSpecificProblem((String) attributeValue);
                        break;
                    case BACKUPSTATUS:
                        processedAlarmEvent.setBackupStatus((Boolean) attributeValue);
                        break;
                    case BACKUPOBJECTINSTANCE:
                        processedAlarmEvent.setBackupObjectInstance((String) attributeValue);
                        break;
                    case PROPOSEDREPAIRACTION:
                        processedAlarmEvent.setProposedRepairAction((String) attributeValue);
                        break;
                    case ALARMNUMBER:
                        processedAlarmEvent.setAlarmNumber((Long) attributeValue);
                        break;
                    case ALARMID:
                        processedAlarmEvent.setAlarmId((Long) attributeValue);
                        break;
                    case CEASETIME:
                        processedAlarmEvent.setCeaseTime((Date) attributeValue);
                        break;
                    case CEASEOPERATOR:
                        processedAlarmEvent.setCeaseOperator((String) attributeValue);
                        break;
                    case ACKTIME:
                        processedAlarmEvent.setAckTime((Date) attributeValue);
                        break;
                    case ACKOPERATOR:
                        processedAlarmEvent.setAckOperator((String) attributeValue);
                        break;
                    case INSERTTIME:
                        processedAlarmEvent.setInsertTime((Date) attributeValue);
                        break;
                    case SYNCSTATE:
                        processedAlarmEvent.setSyncState((Boolean) attributeValue);
                        break;
                    case HISTORYALARMPOID:
                        processedAlarmEvent.setHistoryPOId((Long) attributeValue);
                        break;
                    case CORRELATEDEVENTPOID:
                        processedAlarmEvent.setCorrelatedPOId((Long) attributeValue);
                        break;
                    case COMMENTTEXT:
                        processedAlarmEvent.setCommentText((String) attributeValue);
                        break;
                    case COMMENTTIME:
                        processedAlarmEvent.setCommentTime((Date) attributeValue);
                        break;
                    case LASTUPDATED:
                        processedAlarmEvent.setLastUpdatedTime((Date) attributeValue);
                        break;
                    case LASTALARMOPERATION:
                        final ProcessedLastAlarmOperation lastAlarmOperation = getLastAlarmOperation((String) attributeValue);
                        processedAlarmEvent.setLastAlarmOperation(lastAlarmOperation);
                        break;
                    case REPEATCOUNT:
                        processedAlarmEvent.setRepeatCount(Integer.parseInt(attributeValue.toString()));
                        break;
                    case ADDITIONAL_INFORMATION:
                        processedAlarmEvent.setAdditionalInformationToMap((String) attributeValue);
                        break;
                    case PROBLEMTEXT:
                        processedAlarmEvent.setProblemText((String) attributeValue);
                        break;
                    case PROBLEMDETAIL:
                        processedAlarmEvent.setProblemDetail((String) attributeValue);
                        break;
                    case PRESENTSEVERITY:
                        final ProcessedEventSeverity presentSeverity = getSeverity((String) attributeValue);
                        processedAlarmEvent.setPresentSeverity(presentSeverity);
                        break;
                    case PREVIOUSSEVERITY:
                        final ProcessedEventSeverity previousSeverity = getSeverity((String) attributeValue);
                        processedAlarmEvent.setPreviousSeverity(previousSeverity);
                        break;
                    case ALARMINGOBJECT:
                        processedAlarmEvent.setAlarmingObject((String) alarmAttributes.get(alarmAttributeName));
                        break;
                    case RECORDTYPE:
                        processedAlarmEvent.setRecordType(getRecordType(attributeValue));
                        break;
                    case FMX_GENERATED:
                        processedAlarmEvent.setFmxGenerated((String) alarmAttributes.get(alarmAttributeName));
                        break;
                    case PROCESSING_TYPE:
                        processedAlarmEvent.setProcessingType((String) alarmAttributes.get(alarmAttributeName));
                        break;
                    case VISIBILITY:
                        processedAlarmEvent.setVisibility((Boolean) alarmAttributes.get(alarmAttributeName));
                        break;
                    case ALARMSTATE:
                        switch ((String) attributeValue) {
                            case ACK:
                                processedAlarmEvent.setAlarmState(ProcessedEventState.ACTIVE_ACKNOWLEDGED);
                                break;
                            case UNACK:
                                processedAlarmEvent.setAlarmState(ProcessedEventState.ACTIVE_UNACKNOWLEDGED);
                                break;
                            case CLEARED_ACKNOWLEDGED:
                                processedAlarmEvent.setAlarmState(ProcessedEventState.CLEARED_ACKNOWLEDGED);
                                break;
                            case CLEAREDUNACK:
                                processedAlarmEvent.setAlarmState(ProcessedEventState.CLEARED_UNACKNOWLEDGED);
                                break;
                            case CLOSED:
                                processedAlarmEvent.setAlarmState(ProcessedEventState.CLOSED);
                                break;
                            default:
                                break;
                        }
                        break;
                    case TRENDINDICATION:
                        switch ((String) attributeValue) {
                            case LESS_SEVERE:
                                processedAlarmEvent.setTrendIndication(ProcessedEventTrendIndication.LESS_SEVERE);
                                break;
                            case MORE_SEVERE:
                                processedAlarmEvent.setTrendIndication(ProcessedEventTrendIndication.MORE_SEVERE);
                                break;
                            case NO_CHANGE:
                                processedAlarmEvent.setTrendIndication(ProcessedEventTrendIndication.NO_CHANGE);
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return processedAlarmEvent;
    }

    private static FMProcessedEventType getRecordType(final Object attributeValue) {
        FMProcessedEventType recordType = FMProcessedEventType.UNDEFINED;
        try {
            recordType = FMProcessedEventType.valueOf((String) attributeValue);
        } catch (final IllegalArgumentException illegalArgumentException) {
            LOGGER.error("Invalid value for FMProcessedEventType:{}, setting the recordType to default UNDEFINED", recordType);
            recordType = FMProcessedEventType.UNDEFINED;
        }
        return recordType;
    }

    private static ProcessedEventSeverity getSeverity(final String severity) {
        final ProcessedEventSeverity eventSeverity = EVENT_SEVERITIES.get(severity);
        if (eventSeverity != null) {
            return eventSeverity;
        } else {
            return ProcessedEventSeverity.UNDEFINED;
        }
    }

    private static ProcessedLastAlarmOperation getLastAlarmOperation(final String lastAlarmOperation) {
        final ProcessedLastAlarmOperation alarmOperation = LAST_ALARM_OPERATIONS.get(lastAlarmOperation);
        if (alarmOperation != null) {
            return alarmOperation;
        } else {
            return ProcessedLastAlarmOperation.UNDEFINED;
        }
    }

    /**
     * Enrich Correlation Information in targetAdditionalInfo attribute inside additionalInformation attribute.
     *
     * @param alarmAttributeMap
     *            -- all attributes of an alarm
     *
     */
    private static void rebuildCorrelationInformation(final Map<String, Object> alarmAttributeMap) {
        if ((alarmAttributeMap.get(ROOT) != null) && (alarmAttributeMap.get(ROOT) != CorrelationType.NOT_APPLICABLE.toString())) {
            LOGGER.debug("=== additional information attribute BEFORE enrichment= {}", (String) alarmAttributeMap.get(ADDITIONAL_INFORMATION));
            final TargetAdditionalInformationHandler targetAdditionalInformationHandler = new TargetAdditionalInformationHandler();
            String enrichedAdditionalInfo = null;
            try {
                enrichedAdditionalInfo = targetAdditionalInformationHandler.enrichAdditionalInfoCorrelationInformation(
                        (alarmAttributeMap.get(ADDITIONAL_INFORMATION) != null) ? alarmAttributeMap.get(ADDITIONAL_INFORMATION).toString() : null,
                        (alarmAttributeMap.get(CI_GROUP_1) != null) ? alarmAttributeMap.get(CI_GROUP_1).toString() : null,
                        (alarmAttributeMap.get(CI_GROUP_2) != null) ? alarmAttributeMap.get(CI_GROUP_2).toString() : null,
                        CorrelationType.valueOf(alarmAttributeMap.get(ROOT).toString()));
            } catch (final IllegalArgumentException illegalArgumentException) {
                LOGGER.error("Invalid value for CorrelationType : ", illegalArgumentException);
            }
            LOGGER.debug("=== additional information attribute AFTER enrichment= {}", enrichedAdditionalInfo);
            if (enrichedAdditionalInfo != null) {
                alarmAttributeMap.put(ADDITIONAL_INFORMATION, enrichedAdditionalInfo);
            }
        }
    }

}
