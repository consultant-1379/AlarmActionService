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

import java.io.Serializable;
import java.util.List;

/**
 * AlarmActionServiceClusterEvent holds failed/currently down Jboss instances information.
 */
public class AlarmActionServiceClusterEvent implements Serializable {

    private static final long serialVersionUID = 8110860790367346394L;

    private boolean master;

    private List<String> failedJbossInstances;

    public boolean isMaster() {
        return master;
    }

    public void setMaster(final boolean master) {
        this.master = master;
    }

    public List<String> getFailedJbossInstances() {
        return failedJbossInstances;
    }

    public void setFailedJbossInstances(final List<String> failedJbossInstances) {
        this.failedJbossInstances = failedJbossInstances;
    }

    @Override
    public String toString() {
        return "AlarmActionServiceClusterEvent [master=" + master + ", failedJbossInstances=" + failedJbossInstances + "]";
    }

}
