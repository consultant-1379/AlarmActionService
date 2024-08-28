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

package com.ericsson.oss.services.timerservice;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.AlarmThresholdInboundHandler;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.InternalAlarmSender;

@RunWith(MockitoJUnitRunner.class)
public class AlarmThresholdInboundHandlerTest {

    @InjectMocks
    private final AlarmThresholdInboundHandler alarmThresholdInboundHandler = new AlarmThresholdInboundHandler();

    @Mock
    private DpsAccessManager dpsAccessManager;

    @Mock
    private InternalAlarmSender internalAlarmSender;

    @Mock
    SystemRecorder systemRecorder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidateAndRaiseInternalAlarm() {
        final int activeAlarmCount = 81000;
        final int activeAlarmInboundThreshold = 80000;
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(false);
        alarmThresholdInboundHandler.validateAndRaiseInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
        verify(internalAlarmSender, times(1)).sendInternalAlarmNotification(Constants.SEV_CRITICAL, 80000);
    }

    @Test
    public void testValidateAndRaiseInternalAlarm2() {
        final int activeAlarmCount = 80000;
        final int activeAlarmInboundThreshold = 80001;
        alarmThresholdInboundHandler.validateAndRaiseInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(false);
        verify(internalAlarmSender, times(0)).sendInternalAlarmNotification(Constants.SEV_CRITICAL, 80000);
    }

    @Test
    public void testValidateAndClearInternalAlarm() {
        final int activeAlarmCount = 80000;
        final int activeAlarmInboundThreshold = 80001;
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(true);
        alarmThresholdInboundHandler.validateAndclearInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
        verify(internalAlarmSender, times(1)).sendInternalAlarmNotification(Constants.SEV_CLEARED, 80001);
    }

    @Test
    public void testValidateAndClearInternalAlarm2() {
        final int activeAlarmCount = 80000;
        final int activeAlarmInboundThreshold = 80001;
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(false);
        alarmThresholdInboundHandler.validateAndclearInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
        verify(internalAlarmSender, times(0)).sendInternalAlarmNotification(Constants.SEV_CLEARED, 80000);
    }

    @Test
    public void testValidateAndClearInternalAlarmIfCountsAreSame() {
        final int activeAlarmCount = 80001;
        final int activeAlarmInboundThreshold = 80001;
        when(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification()).thenReturn(true);
        alarmThresholdInboundHandler.validateAndclearInternalAlarm(activeAlarmCount, activeAlarmInboundThreshold);
        verify(internalAlarmSender, times(0)).sendInternalAlarmNotification(Constants.SEV_CLEARED, 80000);
    }

}
