/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.cluster.events;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionServiceClusterEventSenderTest {

    @InjectMocks
    private AlarmActionServiceClusterEventSender alarmActionServiceClusterEventSender;

    @Mock
    private Event<AlarmActionServiceClusterEvent> alarmActionServiceEvent;

    @Test
    public void sendAlarmActionSericeCulsterEvent_test() {
        final List<ClusterMemberInfo> removedMembersInfo = new ArrayList<ClusterMemberInfo>();
        final ClusterMemberInfo ClusterMemberInfo = new ClusterMemberInfo("svc-2-fmhistory", "svc-2-fmhistory", "1.0");
        removedMembersInfo.add(ClusterMemberInfo);
        alarmActionServiceClusterEventSender.sendAlarmActionSericeCulsterEvent(true, removedMembersInfo);
        verify(alarmActionServiceEvent).fire((AlarmActionServiceClusterEvent) Matchers.anyObject());
    }

}
