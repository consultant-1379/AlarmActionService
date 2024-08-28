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

package com.ericsson.oss.services.fm.alarmactionservice.cluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent;
import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.events.AlarmActionServiceClusterEventSender;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionServiceClusterListenerTest {

    @InjectMocks
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Mock
    private MembershipChangeEvent changeEvent;

    @Mock
    private AlarmActionServiceClusterEventSender actionServiceClusterEventSender;

    @Test
    public void testListenForMembershipChange_Master() {
        when(changeEvent.isMaster()).thenReturn(true);
        alarmActionServiceClusterListener.listenForMembershipChange(changeEvent);
        final List<String> failedJbossInstances = new ArrayList<String>();
        failedJbossInstances.add("svc-2-fmhistory");
        final List<ClusterMemberInfo> removedMembersInfo = new ArrayList<MembershipChangeEvent.ClusterMemberInfo>();
        final ClusterMemberInfo clusterMemberInfo = new ClusterMemberInfo("svc-2-fmhistory", "AlarmActionSerice", "1.0.0");
        removedMembersInfo.add(clusterMemberInfo);
        when(changeEvent.getRemovedMembers()).thenReturn(removedMembersInfo);
        alarmActionServiceClusterListener.listenForMembershipChange(changeEvent);
        assertTrue(alarmActionServiceClusterListener.getMasterState());
        verify(actionServiceClusterEventSender).sendAlarmActionSericeCulsterEvent(true, removedMembersInfo);
    }

    @Test
    public void testListenForMembershipChange_NotMaster() {
        when(changeEvent.isMaster()).thenReturn(false);
        alarmActionServiceClusterListener.listenForMembershipChange(changeEvent);
        assertFalse(alarmActionServiceClusterListener.getMasterState());
    }
}
