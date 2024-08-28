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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent;
import com.ericsson.oss.itpf.sdk.cluster.annotation.ServiceCluster;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.events.AlarmActionServiceClusterEventSender;

/**
 * Listens for membership change notifications in the AlarmActionService cluster and updates the master state.
 */
@ApplicationScoped
public class AlarmActionServiceClusterListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionServiceClusterListener.class);

    private boolean isMaster;

    @Inject
    private AlarmActionServiceClusterEventSender actionServiceClusterEventSender;

    public void listenForMembershipChange(@Observes @ServiceCluster("AlarmActionService") final MembershipChangeEvent changeEvent) {
        if (changeEvent.isMaster()) {
            LOGGER.info("Received membership change event [{}], setting current AlarmActionService instance to master", true);
            isMaster = true;
            actionServiceClusterEventSender.sendAlarmActionSericeCulsterEvent(true, changeEvent.getRemovedMembers());
        } else {
            LOGGER.info("Received membership change event [{}], setting current AlarmActionService instance to redundant", false);
            isMaster = false;
        }
    }

    /**
     * Method for getting master state of alarm action service.
     * @return boolean state of current alarm action service instance
     */
    public boolean getMasterState() {
        return isMaster;
    }
}
