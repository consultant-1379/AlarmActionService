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
package com.ericsson.oss.services.fm.alarmactionservice.timer;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_ROUTING_SERVICE;

import static org.mockito.Mockito.verify;

import javax.ejb.Timer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.fm.alarmactionservice.impl.AlarmActionsBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.timer.AutoAckRetryTimerHandler;

@RunWith(MockitoJUnitRunner.class)
public class AutoAckRetryTimerHandlerTest {

    @InjectMocks
    private AutoAckRetryTimerHandler autoAckRetryTimerHandler;

    @Mock
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Test
    public void testHandleTimeOut_NoFailedPoIds() {

        final Timer timer = null;
        autoAckRetryTimerHandler.handleTimeout(timer);
    }

    @Test
    public void testHandleTimeOut_WithFailedPoIds() {

        autoAckRetryTimerHandler.failedPoids.add(123L);
        final Timer timer = null;
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setOperatorName(ALARM_ROUTING_SERVICE);

        autoAckRetryTimerHandler.handleTimeout(timer);

        verify(alarmActionsBatchManager, VerificationModeFactory.times(1)).processAlarmActionsInBatches((AlarmActionData) Mockito.anyObject(),
                Mockito.anyList(), (RetryPolicy) Mockito.any(), (Integer) Mockito.anyInt(), Mockito.anyBoolean());

    }

}
