/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.itpf.sdk.security.accesscontrol.classic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityTargetGroup;

/**
 *
 */
@Singleton
public class TargetGroupRegistry {

    private final Map<String, Set<ESecurityTargetGroup>> mapTargetGroupsForTarget = new HashMap<String, Set<ESecurityTargetGroup>>();

    /**
     * Method for TBAC test
     *
     * @param neName
     */
    public void addNodeToTargetGroup(final String neName, final ESecurityTargetGroup targetGroup) {
        Set<ESecurityTargetGroup> securityTargetGroupSet = mapTargetGroupsForTarget.get(neName);
        if (securityTargetGroupSet == null) {
            securityTargetGroupSet = new HashSet<ESecurityTargetGroup>();
        }
        securityTargetGroupSet.add(targetGroup);
        mapTargetGroupsForTarget.put(neName, securityTargetGroupSet);
    }

    /**
     * Method for TBAC test
     *
     * @param neName
     * @return
     */
    public Set<ESecurityTargetGroup> getTargetGroupsForTarget(final String neName) {
        final Set<ESecurityTargetGroup> targetGroupsForTarget = mapTargetGroupsForTarget.get(neName);
        if (targetGroupsForTarget != null) {
            return targetGroupsForTarget;
        }
        return Collections.emptySet();
    }

    /**
     * @return
     */
    public Set<String> getTargetsListForGroupTarget() {
        return mapTargetGroupsForTarget.keySet();
    }
}
