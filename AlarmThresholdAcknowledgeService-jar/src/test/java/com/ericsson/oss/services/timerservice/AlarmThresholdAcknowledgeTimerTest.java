/*------------------------------------------------------------------------------
 *******************************************************************************
 COPYRIGHT Ericsson 2015
 *
 The copyright to the computer program(s) herein is the property of
 Ericsson Inc. The programs may be used and/or copied only with written
 permission from Ericsson Inc. or in accordance with the terms and
 conditions stipulated in the agreement/contract under which the
 program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.timerservice;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterListener;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.AlarmThresholdAcknowledgeTimer;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.AlarmThresholdInboundHandler;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.AlarmThresholdOutboundHandler;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.InternalAlarmSender;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;

@RunWith(MockitoJUnitRunner.class)
public class AlarmThresholdAcknowledgeTimerTest {

    @InjectMocks
    private final AlarmThresholdAcknowledgeTimer alarmThresholdAcknowledgeTimer = new AlarmThresholdAcknowledgeTimer();

    @Mock
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Mock
    private ConfigurationParameterListener configurationParameterListener;

    @Mock
    private AlarmThresholdInboundHandler alarmThresholdInboundHandler;

    @Mock
    private AlarmThresholdOutboundHandler alarmThresholdOutboundHandler;

    @Mock
    private InternalAlarmSender internalAlarmSender;

    @Mock
    private Timer timer;

    @Mock
    private TimerService timerService;

    @Mock
    private DpsAccessManager dpsAccessManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTimeoutNonMaster() {
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(false);
        alarmThresholdAcknowledgeTimer.timeOut();
        when(timerService.createSingleActionTimer(anyLong(), any(TimerConfig.class))).thenReturn(timer);
        verify(internalAlarmSender, times(0)).sendInternalAlarmNotification(Constants.SEV_CRITICAL, 1000);
    }

    @Test
    public void testTimeoutMasterRaiseAlarm() {
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(true);
        when(configurationParameterListener.getAlarmThresholdForNotification()).thenReturn(80000);
        when(dpsAccessManager.getActiveAlarmCount()).thenReturn((long) 100000);
        when(alarmThresholdInboundHandler.validateAndRaiseInternalAlarm(80000, 100000)).thenReturn(false);
        alarmThresholdAcknowledgeTimer.timeOut();
        verify(internalAlarmSender, times(0)).sendInternalAlarmNotification(Constants.SEV_CRITICAL, 80000);
    }

    @Test
    public void testTimeoutMasterClearInternalAlarm() {
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(true);
        when(configurationParameterListener.getAlarmThresholdForNotification()).thenReturn(80000);
        when(dpsAccessManager.getActiveAlarmCount()).thenReturn((long) 10000);
        when(configurationParameterListener.getAlarmThresholdForForceAck()).thenReturn(11000);
        when(alarmThresholdInboundHandler.validateAndRaiseInternalAlarm(80000, 100000)).thenReturn(false);
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(true);
        alarmThresholdAcknowledgeTimer.timeOut();
        verify(internalAlarmSender, times(0)).sendInternalAlarmNotification(Constants.SEV_CLEARED, 80000);
    }

    @Test
    public void testTimeoutMasterAckActiveClearAlarms() {
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(true);
        when(configurationParameterListener.getAlarmThresholdForNotification()).thenReturn(80000);
        when(dpsAccessManager.getActiveAlarmCount()).thenReturn((long) 100000);
        when(configurationParameterListener.getAlarmThresholdForForceAck()).thenReturn(99999);
        when(alarmThresholdInboundHandler.validateAndRaiseInternalAlarm(100000, 80000)).thenReturn(true);
        alarmThresholdAcknowledgeTimer.timeOut();
    }

    @Test
    public void testProcessTimerIntervalChanges() {
        when(configurationParameterListener.getTimerIntervalToCheckAlarms()).thenReturn(2);
        alarmThresholdAcknowledgeTimer.processTimerIntervalChanges(1, 2);
        assertNotNull(timer);
    }

    @Test
    public void testProcessTimerIntervalChangesWithSameValue() {
        when(configurationParameterListener.getTimerIntervalToCheckAlarms()).thenReturn(1);
        alarmThresholdAcknowledgeTimer.processTimerIntervalChanges(1, 1);
        assertNotNull(timer);
    }
}
