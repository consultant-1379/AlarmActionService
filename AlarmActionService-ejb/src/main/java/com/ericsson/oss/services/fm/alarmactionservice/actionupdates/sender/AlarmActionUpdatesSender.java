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

package com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_RECEIVED_TIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM_CORE_OUT_QUEUE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM_NORTH_BOUND_QUEUE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM_SNMP_NORTH_BOUND_TOPIC;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LAST_DELIVERED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.QUEUE_EXCEPTION_MESSAGE;
import static com.ericsson.oss.services.fm.common.util.AlarmAttributeDataPopulate.populateLimitedAlarmAttributes;
import static com.ericsson.oss.services.fm.common.util.AlarmTextRouteInputEventBuilder.buildAtrInputEvent;
import static com.ericsson.oss.services.fm.models.processedevent.FMProcessedEventType.ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.models.processedevent.FMProcessedEventType.REPEATED_ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.models.processedevent.ProcessedEventState.ACTIVE_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.models.processedevent.ProcessedEventState.CLEARED_UNACKNOWLEDGED;
import static com.ericsson.oss.services.fm.models.processedevent.ProcessedLastAlarmOperation.CLEAR;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.eventbus.EventConfigurationBuilder;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.ProcessedAlarmEventBuilder;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ATRInputEvent;
import com.ericsson.oss.services.fm.models.processedevent.FMProcessedEventType;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

/**
 * This class sends alarm action updates(ack,unack,clear,comment) events to
 * subscribed/listening JMS consumers.
 **/
@Stateless
public class AlarmActionUpdatesSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionUpdatesSender.class);

    @Inject
    @Modeled
    private EventSender<ProcessedAlarmEvent> modeledEventSender;

    @Inject
    @Modeled
    private EventSender<ATRInputEvent> atrModeledEventSender;

    @Inject
    private DownwardOperationRequestSender downwardOperationRequestSender;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    public void sendAlarmActionsToQueues(final List<AlarmActionInformation> alarmActionInformations, final String operatorName,
            final String alarmAction) {
        try {
            for (final AlarmActionInformation actionInformation : alarmActionInformations) {
                final Map<String, Object> alarmAttributes = actionInformation.getAlarmAttributes();
                sendProcessedAlarmEvent(alarmAttributes);
            }
            downwardOperationRequestSender.prepareAndSendMediationTaskRequest(alarmActionInformations, operatorName, alarmAction);
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred in sending alarm to Queue: ", exception);
            throw new RuntimeException(exception);
        }
    }

    public void sendFailedAlarmActionsToQueues(final List<AlarmActionInformation> alarmActionInformations) {
        try {
            for (final AlarmActionInformation actionInformation : alarmActionInformations) {
                final Map<String, Object> alarmAttributes = actionInformation.getAlarmAttributes();
                sendProcessedAlarmEvent(alarmAttributes);
            }
            downwardOperationRequestSender.prepareAndSendFailedActionsMediationTaskRequest(alarmActionInformations);
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred in sending alarm to Queue: ", exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * Builds ProcessedAlarmEvent from given alarm attributes and sends to
     * FMNorthBoundQueue,FmCoreOutQueue.
     *
     * @param alarmMap
     *            The map containing alarm attributes.
     */
    private void sendProcessedAlarmEvent(final Map<String, Object> alarmMap) {
        try {
            final ProcessedAlarmEvent processedAlarmEvent = ProcessedAlarmEventBuilder.getProcessedAlarm(alarmMap);
            final Long alarmReceivedTime = new Date().getTime();
            final EventConfigurationBuilder eventConfigurationBuilder = new EventConfigurationBuilder();
            eventConfigurationBuilder.addEventProperty(ALARM_RECEIVED_TIME, alarmReceivedTime.toString());
            if (processedAlarmEvent.getVisibility()) {
                final long lastDeliveredTime = (long) alarmMap.get(LAST_DELIVERED);
                processedAlarmEvent.getAdditionalInformation().put(LAST_DELIVERED, String.valueOf(lastDeliveredTime));
                sendAlarmToNorthBound(processedAlarmEvent, eventConfigurationBuilder);
            }
            checkDelayToDeliver(processedAlarmEvent, eventConfigurationBuilder);
            sendEventsToSnmpNorthBound(processedAlarmEvent, eventConfigurationBuilder);
            sendEventsToCoreOutQueue(processedAlarmEvent, eventConfigurationBuilder);
            LOGGER.debug("Sent ProcessedAlarmEvent to NorthBoundQueue,FMSnmpNorthBoundTopic,FmCoreOutQueue{}", processedAlarmEvent);
            checkAndSendClearsToAtrEventChannel(processedAlarmEvent);
        } catch (final Exception exception) {
            LOGGER.error("Failed to send the alarm event to queue. Exception details are: ", exception);
            throw new RuntimeException(QUEUE_EXCEPTION_MESSAGE);
        }
    }

    private void checkAndSendClearsToAtrEventChannel(final ProcessedAlarmEvent processedAlarmEvent) {
        if (configurationsChangeListener.getEnableAutoAckOnManualClearedAlarms() && CLEAR.equals(processedAlarmEvent.getLastAlarmOperation())
                && CLEARED_UNACKNOWLEDGED.equals(processedAlarmEvent.getAlarmState())) {
            final ATRInputEvent atrInputEvent = buildAtrInputEvent(processedAlarmEvent);
            atrModeledEventSender.send(atrInputEvent);
            LOGGER.debug("Sent ATRInputEvent to registered ATRProcessedEventChannel for ATR processing. Event: {}", atrInputEvent);
        } else {
            LOGGER.debug("enableAutoAckOnManualClearAlarms is not ENABLED. So not sending the CLEAR to ATRProcessedEventChannel.");
        }
    }

    private void sendAlarmToNorthBound(final ProcessedAlarmEvent processedAlarmEvent, final EventConfigurationBuilder eventConfigurationBuilder) {
        final Integer timeToDeliver = getAlarmDelayToDeliver(processedAlarmEvent);
        if (timeToDeliver != null) {
            try {
               eventConfigurationBuilder.addEventProperty(LAST_DELIVERED, processedAlarmEvent.getAdditionalInformation().get(LAST_DELIVERED));
               eventConfigurationBuilder.delayToDeliver(timeToDeliver, TimeUnit.MILLISECONDS);
            } catch (final Exception exception) {
                LOGGER.error("Exception occured while adding timeToDeliver:{} to alarm:{}", timeToDeliver, populateLimitedAlarmAttributes(processedAlarmEvent));
                LOGGER.debug("Exception occured while adding timeToDeliver:{} to alarm:{}", timeToDeliver, processedAlarmEvent);
            }
        }
        processedAlarmEvent.getAdditionalInformation().remove(LAST_DELIVERED);
        modeledEventSender.send(processedAlarmEvent, FM_NORTH_BOUND_QUEUE, eventConfigurationBuilder.build());
    }

    private Integer getAlarmDelayToDeliver(final ProcessedAlarmEvent processedAlarmEvent) {
        Integer timeToDeliver = null;
        if (processedAlarmEvent.getLastUpdatedTime() != null && processedAlarmEvent.getAdditionalInformation().get(LAST_DELIVERED) != null) {
            try {
                final Long diffTimeInMilliSeconds = Math.abs(Long.parseLong(processedAlarmEvent.getAdditionalInformation().get(LAST_DELIVERED))
                        - processedAlarmEvent.getLastUpdatedTime().getTime());
                if (diffTimeInMilliSeconds != 0) {
                    timeToDeliver = diffTimeInMilliSeconds.intValue();
                }
            } catch (final NumberFormatException numberFormatException) {
                LOGGER.error("Exception occured for LAST DELIVERED TIME returning timeToDeliver as NULL : ", numberFormatException);
            }
        }
        processedAlarmEvent.getAdditionalInformation().remove(LAST_DELIVERED);
        LOGGER.debug("Delay time to Deliver is {},  with Last Delivered (millisec) is {}", timeToDeliver,
                processedAlarmEvent.getAdditionalInformation().get(LAST_DELIVERED));
        return timeToDeliver;
    }

    private void checkDelayToDeliver(final ProcessedAlarmEvent processedAlarmEvent, final EventConfigurationBuilder eventConfigurationBuilder) {
        final FMProcessedEventType recordType = processedAlarmEvent.getRecordType();
        if (configurationsChangeListener.isAutoAckEventsDelayEnabled()
                && (ERROR_MESSAGE.equals(recordType) || REPEATED_ERROR_MESSAGE.equals(recordType))
                && ACTIVE_ACKNOWLEDGED.equals(processedAlarmEvent.getAlarmState())) {
            eventConfigurationBuilder.delayToDeliver(configurationsChangeListener.getAutoAckEventsDelayToQueue(), TimeUnit.MILLISECONDS);
            LOGGER.debug("Sending {} {} to FmCoreOutQueue and FMSnmpNorthBoundTopic with {} delay to avoid re-ordering of AutoAck ERROR messages.",
                    processedAlarmEvent.getAlarmState(), processedAlarmEvent.getRecordType(),
                    configurationsChangeListener.getAutoAckEventsDelayToQueue());
        }
    }

    private void sendEventsToCoreOutQueue(final ProcessedAlarmEvent processedAlarmEvent, final EventConfigurationBuilder eventConfigurationBuilder) {
        modeledEventSender.send(processedAlarmEvent, FM_CORE_OUT_QUEUE, eventConfigurationBuilder.build());
    }

    private void sendEventsToSnmpNorthBound(final ProcessedAlarmEvent processedAlarmEvent, final EventConfigurationBuilder eventConfigurationBuilder) {
        modeledEventSender.send(processedAlarmEvent, FM_SNMP_NORTH_BOUND_TOPIC, eventConfigurationBuilder.build());
    }

}
