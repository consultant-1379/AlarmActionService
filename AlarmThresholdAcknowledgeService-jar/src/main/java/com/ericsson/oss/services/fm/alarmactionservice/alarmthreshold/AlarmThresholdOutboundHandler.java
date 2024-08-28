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

package com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterListener;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.AlarmAckBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.BatchingParameters;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_ACTION_SERVICE;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;

/**
 * Responsible for performing ack on cleared alarms when the open alarm count is higher than threshold value and if threshold ack is enabled.
 */
public class AlarmThresholdOutboundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmThresholdOutboundHandler.class);

    @Inject
    private DpsAccessManager dpsAccessManager;

    @Inject
    private ThresholdAckAlarmBatchTimer thresholdAckAlarmBatchTimer;

    @Inject
    private AlarmAckBatchManager alarmAckBatchManager;

    @Inject
    private ConfigurationParameterListener configurationParameterListener;
    @Inject
    private SystemRecorder systemRecorder;

    private final List<Long> poIds = new ArrayList<Long>();

    public void validateAndAcknowledgeActiveClearedAlarms(final long activeAlarmCount, final int activeAlarmInboundThreshold,
            final int activeAlarmoutboundThreshold) {
        if (activeAlarmCount >= activeAlarmoutboundThreshold) {
            if (configurationParameterListener.isFmThresholdAckOfAlarmsEnabled()) {
                final long startTime = System.currentTimeMillis();
                if (poIds.isEmpty()) {
                    LOGGER.info("acknowledging all cleared alarms as active count {} cross the outboundThreshold Value {}", activeAlarmCount,
                            activeAlarmoutboundThreshold);
                    poIds.addAll(dpsAccessManager.getPoIds());
                }
                ackAlarms(startTime);
            }
        } else {
            final long startTime = System.currentTimeMillis();
            ackAlarms(startTime);
        }
    }

    private void ackAlarms(final long startTime) {
        if (!thresholdAckAlarmBatchTimer.getAckInProgress().get()) {
        	int value = poIds.size();
            final BatchingParameters batchingParameters = buildBatchigParameters();
            final LinkedList<List<Long>> poIdBatchesForCurrentCycle = alarmAckBatchManager.getpoIdBatches(poIds, batchingParameters);
            systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "activeAlarmoutboundThreshold", "The poIds size after removing the batch is :" + poIds.size() , "");
            thresholdAckAlarmBatchTimer.startThresholdAlarmAckTimer(poIdBatchesForCurrentCycle, startTime);
            final Iterator<List<Long>> poIdBatchesIterator = poIdBatchesForCurrentCycle.iterator();
            while (poIdBatchesIterator.hasNext()) {
                poIds.removeAll(poIdBatchesIterator.next());
            }
            systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "activeAlarmoutboundThreshold", "number of alarms threshold acked are :" + value , "");
        } else {
            systemRecorder.recordEvent(ALARM_ACTION_SERVICE, EventLevel.DETAILED, "activeAlarmoutboundThreshold", "The threshold ack batch timer is still running for previous batch, So Ignoring :", "");
        }
    }

    private BatchingParameters buildBatchigParameters() {
        final BatchingParameters batchingParameters = new BatchingParameters();
        batchingParameters.setAckCycleTimeUtilization(configurationParameterListener.getThresholdAckCycleTimeUtilization());
        batchingParameters.setBatchTimerTimeout(configurationParameterListener.getDelayedAckBatchTimerTimeOut());
        batchingParameters.setCheckFrequency(configurationParameterListener.getTimerIntervalToCheckAlarms());
        batchingParameters.setThresholdEventBatchSize(configurationParameterListener.getThresholdAckBatchSize());
        return batchingParameters;
    }

}
