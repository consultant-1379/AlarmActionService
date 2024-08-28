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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENT;

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

import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.AlarmAckBatchManager;
import com.ericsson.oss.services.fm.alarmactionservice.batchmanager.BatchingParameters;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckEventBatchTimer;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DelayedAckEventTimerService;
import com.ericsson.oss.services.fm.alarmactionservice.timer.DpsAccessTransactionalFacade;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;

@RunWith(MockitoJUnitRunner.class)
public class DelayedAckEventTimerServiceTest {

    @InjectMocks
    private DelayedAckEventTimerService delayedAckEventTimerService;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Mock
    private DpsAccessTransactionalFacade dpsAccessTransactionalFacade;

    @Mock
    private DelayedAckEventBatchTimer delayedAckEventBatchTimer;

    @Mock
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Mock
    private AlarmAckBatchManager alarmAckBatchManager;

    @Mock
    private BatchingParameters batchingParameters;

    @Mock
    private AlarmActionUtils alarmActionUtils;

    @Mock
    private TimerService timerService;

    private Timer delayedAckEventTimer;

    @Test
    public void testHandleTimeOut() {
        final List<Long> poIdsList = new ArrayList<Long>();
        poIdsList.add(1234L);
        final Timer timer = null;
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(true);
        when(configurationsChangeListener.getCheckfrequency()).thenReturn(0);
        when(configurationsChangeListener.getGenerationAge()).thenReturn(1);
        when(configurationsChangeListener.getFmTimerActionBatchSize()).thenReturn(10);
        when(alarmActionUtils.createBatchigParameters()).thenReturn(batchingParameters);
        when(dpsAccessTransactionalFacade.getPoIds(1, EVENT)).thenReturn(poIdsList);
        delayedAckEventTimerService.handleTimeout(timer);
        verify(alarmAckBatchManager, times(1)).getpoIdBatches(poIdsList, batchingParameters);
        verify(delayedAckEventBatchTimer, times(1)).startAlarmAckTimer((LinkedList<List<Long>>) Matchers.anyObject(), Matchers.anyLong());
    }

    @Test
    public void testInIt() {
        when(configurationsChangeListener.isDelayedAckForAlarmsEnabled()).thenReturn(true);
        when(timerService.createIntervalTimer(0, 600000L, new TimerConfig())).thenReturn(delayedAckEventTimer);
        delayedAckEventTimerService.init();
        delayedAckEventTimerService.processDelayedAckForEventsEnabledChanges(true);
        delayedAckEventTimerService.processCheckFrequencyChangesForEvents(10);
    }
}
