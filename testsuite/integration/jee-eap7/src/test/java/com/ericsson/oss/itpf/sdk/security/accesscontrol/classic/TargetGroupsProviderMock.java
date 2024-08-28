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
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.util.StringUtils;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityTarget;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityTargetGroup;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.spi.TargetPIPSPI;

@Stateless
public class TargetGroupsProviderMock implements TargetPIPSPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetGroupsProviderMock.class);

    @Inject
    private TargetGroupRegistry targetGroupRegistry;

    @Override
    public Set<ESecurityTargetGroup> getTargetGroupsForTarget(final ESecurityTarget target) {

        LOGGER.warn("*********************************************************");
        LOGGER.warn("TargetGroupsProviderMock IS NOT FOR PRODUCTION USE.");
        LOGGER.warn("TargetGroupsProviderMock: getTargetGroupsForTarget called.");
        LOGGER.warn("**********************************************************");

        if (target == null || StringUtils.isEmpty(target.getName())) {
            return Collections.emptySet();
        }
        return targetGroupRegistry.getTargetGroupsForTarget(target.getName());
    }

}
