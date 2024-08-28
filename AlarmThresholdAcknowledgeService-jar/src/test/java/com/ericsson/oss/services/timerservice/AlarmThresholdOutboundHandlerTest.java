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
 *---------------------------------------------------------------------------- */

package com.ericsson.oss.services.timerservice;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterListener;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.AlarmThresholdOutboundHandler;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.InternalAlarmSender;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.ThresholdAckAlarmBatchTimer;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.AlarmAckBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.BatchingParameters;

@RunWith(MockitoJUnitRunner.class)
public class AlarmThresholdOutboundHandlerTest {

    @InjectMocks
    private final AlarmThresholdOutboundHandler alarmThresholdOutboundHandler = new AlarmThresholdOutboundHandler();

    @Mock
    private DpsAccessManager dpsAccessManager;

    @Mock
    private InternalAlarmSender internalAlarmSender;

    @Mock
    private SystemRecorder systemRecorder;

    @Mock
    private List<Long> poIds;

    @Mock
    private ConfigurationParameterListener configurationParameterListener;

    @Mock
    private ThresholdAckAlarmBatchTimer thresholdAckAlarmBatchTimer;

    @Mock
    private AlarmAckBatchManager alarmAckBatchManager;

    private final BatchingParameters batchingParameters = new BatchingParameters();

    private final AtomicBoolean ackInProgress = new AtomicBoolean();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidateAndAcknowledgeActiveClearedAlarms_ThresholdLimitCrossed() {
        final int activeAlarmCount = 800000;
        final int activeAlarmInboundThreshold = 80001;
        final int activeAlarmoutboundThreshold = 120000;
        final List<Long> poIdsList = new ArrayList<Long>();
        final LinkedList<List<Long>> poIdSubBatches = new LinkedList<List<Long>>();
        for (long i = 0; i < 80000; i++) {
            poIdsList.add(i);
        }
        when(configurationParameterListener.getThresholdAckCycleTimeUtilization()).thenReturn(80);
        when(configurationParameterListener.getDelayedAckBatchTimerTimeOut()).thenReturn(4);
        when(configurationParameterListener.getTimerIntervalToCheckAlarms()).thenReturn(10);
        when(configurationParameterListener.getThresholdAckBatchSize()).thenReturn(175);
        when(dpsAccessManager.getPoIds()).thenReturn(poIdsList);
        poIdSubBatches.add(poIdsList.subList(0, 500));
        when(alarmAckBatchManager.getpoIdBatches((List<Long>) Matchers.anyObject(), (BatchingParameters) Matchers.anyObject()))
                .thenReturn(poIdSubBatches);
        ackInProgress.set(false);
        when(thresholdAckAlarmBatchTimer.getAckInProgress()).thenReturn(ackInProgress);
        when(dpsAccessManager.getActiveAlarmCount()).thenReturn((long) 79999);
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(true);
        when(configurationParameterListener.isFmThresholdAckOfAlarmsEnabled()).thenReturn(true);
        alarmThresholdOutboundHandler.validateAndAcknowledgeActiveClearedAlarms(activeAlarmCount, activeAlarmInboundThreshold,
                activeAlarmoutboundThreshold);

        verify(alarmAckBatchManager, times(1)).getpoIdBatches((List<Long>) Matchers.anyObject(), (BatchingParameters) Matchers.anyObject());
    }

    @Test
    public void testValidateAndAcknowledgeActiveClearedAlarms_ThresholdLimitNotCrossed() {
        final int activeAlarmCount = 80000;
        final int activeAlarmInboundThreshold = 80001;
        final int activeAlarmoutboundThreshold = 120000;
        ackInProgress.set(false);
        when(thresholdAckAlarmBatchTimer.getAckInProgress()).thenReturn(ackInProgress);
        when(dpsAccessManager.getActiveAlarmCount()).thenReturn((long) 79999);
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(true);
        when(configurationParameterListener.isFmThresholdAckOfAlarmsEnabled()).thenReturn(false);

        when(configurationParameterListener.getThresholdAckCycleTimeUtilization()).thenReturn(80);
        when(configurationParameterListener.getDelayedAckBatchTimerTimeOut()).thenReturn(4);
        when(configurationParameterListener.getTimerIntervalToCheckAlarms()).thenReturn(10);
        when(configurationParameterListener.getThresholdAckBatchSize()).thenReturn(175);
        alarmThresholdOutboundHandler.validateAndAcknowledgeActiveClearedAlarms(activeAlarmCount, activeAlarmInboundThreshold,
                activeAlarmoutboundThreshold);
        verify(alarmAckBatchManager, times(1)).getpoIdBatches((List<Long>) Matchers.anyObject(), (BatchingParameters) Matchers.anyObject());
    }

    @Test
    public void testValidateAndAcknowledgeActiveClearedAlarms_WithPrviousBatchNotCompleted() {
        final int activeAlarmCount = 80000;
        final int activeAlarmInboundThreshold = 80001;
        final int activeAlarmoutboundThreshold = 120000;
        ackInProgress.set(true);
        when(thresholdAckAlarmBatchTimer.getAckInProgress()).thenReturn(ackInProgress);
        alarmThresholdOutboundHandler.validateAndAcknowledgeActiveClearedAlarms(activeAlarmCount, activeAlarmInboundThreshold,
                activeAlarmoutboundThreshold);

    }
}
