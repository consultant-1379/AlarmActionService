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

package com.ericsson.oss.services.fm.alarmactionservice.listener;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.fm.alarmactionservice.cluster.events.AlarmActionServiceClusterEvent;
import com.ericsson.oss.services.fm.alarmactionservice.handlers.FailedAlarmActionsHandler;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionServiceClusterEventListenerTest {

    @InjectMocks
    private AlarmActionServiceClusterEventListener alarmActionServiceClusterEventListener;

    @Mock
    private FailedAlarmActionsHandler failedAlarmActionsHandler;

    @Test
    public void receiveFailoverEvent_test() {
        final AlarmActionServiceClusterEvent alarmActionServiceClusterEvent = new AlarmActionServiceClusterEvent();
        alarmActionServiceClusterEvent.setMaster(true);
        final List<String> failedJbossNodeIds = new ArrayList<String>();
        alarmActionServiceClusterEvent.setFailedJbossInstances(failedJbossNodeIds);
        alarmActionServiceClusterEventListener.receiveAlarmActionServiceEvent(alarmActionServiceClusterEvent);
        verify(failedAlarmActionsHandler).processFailedAlarmActions(Matchers.anyList());
    }

}
