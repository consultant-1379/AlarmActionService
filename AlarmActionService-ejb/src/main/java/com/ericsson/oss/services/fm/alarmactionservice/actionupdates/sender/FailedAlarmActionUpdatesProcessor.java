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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_ACTION_SERVICE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.rest.client.FailedAlarmActionUpdatesRestClient;
import com.ericsson.oss.services.fm.alarmactionservice.util.ProcessedAlarmEventBuilder;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

/**
 * FailedAlarmActionUpdatesProcessor provides functionality to process FailedAlarmActionupdates alarms and delegate to
 * {@link FailedAlarmActionUpdatesRestClient} to store in History DB.
 */
public class FailedAlarmActionUpdatesProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedAlarmActionUpdatesProcessor.class);
    public final int batchSize = 100;

    @Inject
    private RetryManager retryManager;

    @Inject
    private FailedAlarmActionUpdatesRestClient restClient;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    @Inject
    private SystemRecorder systemRecorder;

    public void processFailedAlarmActionUpdates(final List<AlarmActionInformation> alarmActionInformations, final List<String> failedJbossInstances) {
        LOGGER.debug("Received failed alarm actions udpates count:{}", alarmActionInformations);
        final RetryPolicy policy = RetryPolicy.builder().attempts(configurationsChangeListener.getNumberOfRetries())
                .waitInterval(configurationsChangeListener.getSleeptTimeMilliSeconds(), TimeUnit.MILLISECONDS)
                .exponentialBackoff(configurationsChangeListener.getExponentialBackOff()).retryOn(Exception.class).build();
        final List<ProcessedAlarmEvent> processedAlarmEvents = buildProcessedAlarmEvents(alarmActionInformations);
        if (!processedAlarmEvents.isEmpty()) {
            final List<List<ProcessedAlarmEvent>> processedAlarmEventBatches = batchProcessedAlarmEvents(processedAlarmEvents);
            int failedAlarmActionsupdatesCount = 0;
            for (final List<ProcessedAlarmEvent> processedAlarmEventBatch : processedAlarmEventBatches) {
                failedAlarmActionsupdatesCount += processEachBatch(processedAlarmEventBatch, policy);
            }
            if (failedAlarmActionsupdatesCount > 0) {
                systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "TotalInstancesDown:" + failedJbossInstances.toString(),
                        "TotalFailedAlarmActionUpdates:" + failedAlarmActionsupdatesCount,
                        "Total Alarm action updates failed to Store in history,during instances down:" + failedAlarmActionsupdatesCount);
            }
        }
    }

    private List<ProcessedAlarmEvent> buildProcessedAlarmEvents(final List<AlarmActionInformation> alarmActionInformations) {
        final List<ProcessedAlarmEvent> processedAlarmEvents = new ArrayList<ProcessedAlarmEvent>();
        if (alarmActionInformations != null) {
            for (final AlarmActionInformation alarmActionInformation : alarmActionInformations) {
                final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
                alarmAttributes.putAll(alarmActionInformation.getAlarmAttributes());
                final ProcessedAlarmEvent processedAlarmEvent = ProcessedAlarmEventBuilder.getProcessedAlarm(alarmAttributes);
                processedAlarmEvents.add(processedAlarmEvent);
            }
        }
        return processedAlarmEvents;
    }

    private List<List<ProcessedAlarmEvent>> batchProcessedAlarmEvents(final List<ProcessedAlarmEvent> processedAlarmEvents) {
        final List<List<ProcessedAlarmEvent>> processedAlarmEventBatches = new ArrayList<List<ProcessedAlarmEvent>>();
        for (int counter = 0; counter < processedAlarmEvents.size(); counter += batchSize) {
            processedAlarmEventBatches
                    .add(processedAlarmEvents.subList(counter, counter + Math.min(batchSize, processedAlarmEvents.size() - counter)));
        }
        return processedAlarmEventBatches;
    }

    private int processEachBatch(final List<ProcessedAlarmEvent> processedAlarmEvents, final RetryPolicy policy) {
        int failedAlarmActionsupdatesCount = 0;
        try {
            processAlarms(policy, processedAlarmEvents);
        } catch (final Exception exception) {
            LOGGER.error(
                    "Max retries attempts reached in sending failed alarm actions updates to history and count is:{}."
                            + "Exception details are: {}",
                    processedAlarmEvents.size(), exception);
            failedAlarmActionsupdatesCount = processedAlarmEvents.size();
        }
        return failedAlarmActionsupdatesCount;
    }

    /**
     * This is method is responsible for processing the batches of requests with the retryMechanism which is defined in the retry policy
     * {@link RetryPolicy }.
     * @param policy
     *            - an encapsulation of the RetryPolicy to be applied on the retries to be performed in each of the failed requests
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     */
    private void processAlarms(final RetryPolicy policy, final List<ProcessedAlarmEvent> processedAlarmEvents) {
        retryManager.executeCommand(policy, new RetriableCommand<Void>() {
            @Override
            public Void execute(final RetryContext retryContext) throws Exception {
                final int currentAttempt = retryContext.getCurrentAttempt();
                if (currentAttempt > 1) {
                    LOGGER.info("In the execute method for the batch of size {} with the retry mechanism. CurrentAttempt is {}",
                            processedAlarmEvents.size(),
                            currentAttempt);
                }
                restClient.sendFailedAlarmsToHistory(processedAlarmEvents);
                return null;
            }
        });
    }
}
