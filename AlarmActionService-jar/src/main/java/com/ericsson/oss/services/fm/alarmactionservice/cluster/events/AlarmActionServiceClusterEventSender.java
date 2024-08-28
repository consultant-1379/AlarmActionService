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

package com.ericsson.oss.services.fm.alarmactionservice.cluster.events;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo;

/**
 * AlarmActionServiceClusterEventSender provides functionality to receive membership change event information and prepare
 * AlarmActionServiceClusterEvent and send it to internal listeners.
 */
public class AlarmActionServiceClusterEventSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionServiceClusterEventSender.class);

    @Inject
    private Event<AlarmActionServiceClusterEvent> alarmActionServiceEvent;

    public void sendAlarmActionSericeCulsterEvent(final boolean master, final List<ClusterMemberInfo> removedMembersInfo) {
        final AlarmActionServiceClusterEvent actionServiceClusterEvent = new AlarmActionServiceClusterEvent();
        actionServiceClusterEvent.setFailedJbossInstances(getRemovedNodeIds(removedMembersInfo));
        actionServiceClusterEvent.setMaster(master);
        LOGGER.warn("Failed/removed instances information:{}", actionServiceClusterEvent);
        alarmActionServiceEvent.fire(actionServiceClusterEvent);
    }

    private List<String> getRemovedNodeIds(final List<ClusterMemberInfo> removedMembersInfo) {
        final List<String> failedJbossInstances = new ArrayList<String>();
        if (!removedMembersInfo.isEmpty()) {
            for (final ClusterMemberInfo failedInsanceInfo : removedMembersInfo) {
                failedJbossInstances.add(failedInsanceInfo.getNodeId());
            }
        }
        return failedJbossInstances;
    }
}
