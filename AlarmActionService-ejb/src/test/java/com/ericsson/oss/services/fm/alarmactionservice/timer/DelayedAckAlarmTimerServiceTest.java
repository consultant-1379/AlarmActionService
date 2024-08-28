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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.AlarmAckBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.BatchingParameters;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckAlarmBatchTimer;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckAlarmTimerService;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DpsAccessTransactionalFacade;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;

@RunWith(MockitoJUnitRunner.class)
public class DelayedAckAlarmTimerServiceTest {

    @InjectMocks
    private DelayedAckAlarmTimerService delayedAckAlarmTimerService;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Mock
    private DpsAccessTransactionalFacade dpsAccessTransactionalFacade;

    @Mock
    private DelayedAckAlarmBatchTimer delayedAckBatchTimer;

    @Mock
    private AlarmActionData alarmActionData;

    @Mock
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Mock
    private AlarmAckBatchManager alarmAckBatchManager;

    @Mock
    private AlarmActionUtils alarmActionUtils;

    @Mock
    private BatchingParameters batchingParameters;

    @Mock
    private TimerService timerService;

    private Timer delayedAckAlarmTimer;

    @Test
    public void testHandleTimeOut() {
        final List<Long> poIdsList = new ArrayList<Long>();
        final List<List<Long>> poIdSubBatches = new ArrayList<List<Long>>();
        poIdsList.add(1234L);
        poIdSubBatches.add(poIdsList);
        final Timer timer = null;
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setResponse(SUCCESS);
        final List<AlarmActionResponse> response = new ArrayList<AlarmActionResponse>();
        response.add(alarmActionResponse);
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(true);
        when(configurationsChangeListener.getCheckfrequency()).thenReturn(10);
        when(configurationsChangeListener.getClearAge()).thenReturn(1);
        when(configurationsChangeListener.getFmTimerActionBatchSize()).thenReturn(10);
        when(alarmActionUtils.createBatchigParameters()).thenReturn(batchingParameters);
        when(dpsAccessTransactionalFacade.getPoIds(1, ALARM)).thenReturn(poIdsList);
        delayedAckAlarmTimerService.handleTimeout(timer);
        verify(alarmAckBatchManager, times(1)).getpoIdBatches(poIdsList, batchingParameters);
        verify(delayedAckBatchTimer, times(1)).startAlarmAckTimer((LinkedList<List<Long>>) Matchers.anyObject(), Matchers.anyLong());
    }

    @Test
    public void testInIt() {
        when(configurationsChangeListener.isDelayedAckForAlarmsEnabled()).thenReturn(true);
        when(timerService.createIntervalTimer(0, 600000L, new TimerConfig())).thenReturn(delayedAckAlarmTimer);
        delayedAckAlarmTimerService.init();
        delayedAckAlarmTimerService.processDelayedAckForAlarmsEnabledChanges(true);
        delayedAckAlarmTimerService.processCheckFrequencyChangesForAlarms(10);
    }
}
