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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.fm.alarmactionservice.cluster.events.AlarmActionServiceClusterEvent;
import com.ericsson.oss.services.fm.alarmactionservice.handlers.FailedAlarmActionsHandler;

/**
 * AlarmActionServiceClusterEventListener listen to AlarmActionServiceEvent to invoke failed alarm action updates information in history.
 */
@ApplicationScoped
public class AlarmActionServiceClusterEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionServiceClusterEventListener.class);

    @Inject
    private FailedAlarmActionsHandler failedAlarmActionsHandler;

    /**
     * Observes {@link AlarmActionServiceClusterEvent} from AlarmActionSerice jar.
     * @param alarmActionServiceClusterEvent
     *            containing cluster membership event state and failed/removed Jboss instances information from cluster group.
     */
    public void receiveAlarmActionServiceEvent(@Observes final AlarmActionServiceClusterEvent alarmActionServiceClusterEvent) {
        LOGGER.info("received AlarmActionServiceClusterEvent:{}", alarmActionServiceClusterEvent);
        failedAlarmActionsHandler.processFailedAlarmActions(alarmActionServiceClusterEvent.getFailedJbossInstances());
    }
}