/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.timer;

import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.impl.AlarmActionsBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckAlarmBatchTimer;

@RunWith(MockitoJUnitRunner.class)
public class DelayedAckAlarmBatchTimerTest {

    @InjectMocks
    private DelayedAckAlarmBatchTimer delayedAckBatchTimer;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Mock
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Mock
    private AlarmActionData alarmActionData;

    @Mock
    private TimerService timerService;
    private Timer delayedAlarmAckTimer;

    @Test
    public void testHandleTimeOut() {
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(1234L);
        when(configurationsChangeListener.getCheckfrequency()).thenReturn(10);
        final Timer timer = null;
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setResponse(SUCCESS);

        final List<AlarmActionResponse> response = new ArrayList<AlarmActionResponse>();
        response.add(alarmActionResponse);

        delayedAckBatchTimer.handleTimeout(timer);
        when(
                alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIds, null,
                        configurationsChangeListener.getAlarmActionBatchSize(), false)).thenReturn(response);
    }

    @Test
    public void testStartTimer() {
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(1234L);
        final LinkedList<List<Long>> poIdSubBatches = new LinkedList<List<Long>>();
        poIdSubBatches.add(poIds);
        when(configurationsChangeListener.getCheckfrequency()).thenReturn(10);
        final Timer timer = null;
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setResponse(SUCCESS);

        final List<AlarmActionResponse> response = new ArrayList<AlarmActionResponse>();
        response.add(alarmActionResponse);

        delayedAckBatchTimer.startAlarmAckTimer(poIdSubBatches, 123L);
        when(timerService.createIntervalTimer(0, 4000L, new TimerConfig())).thenReturn(delayedAlarmAckTimer);
    }
}
