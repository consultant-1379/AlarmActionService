/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.EPredefinedRole;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.annotation.Authorize;

/**
 * A class responsible for checking the authorization Only authorized users such as Administrator and Operator can invoke designated methods. <br>
 * In case of un-authorized access, {@link com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException} will be thrown.
 */
public class AuthorizationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationHandler.class);

    /**
     * Authorization check on "Perform ACK/UNACK and CLEAR operation on open alarms".
     */
    @Authorize(resource = "open_alarms", action = "execute", role = { EPredefinedRole.ADMINISTRATOR, EPredefinedRole.OPERATOR })
    public void checkAuthorization() {
        LOGGER.debug("User is Authorized to excute");
    }

    /**
     * Authorization check on "Updating the Comments on the alarms".
     */
    @Authorize(resource = "open_alarms", action = "update", role = { EPredefinedRole.ADMINISTRATOR, EPredefinedRole.OPERATOR })
    public void checkAuthorizationForComment() {
        LOGGER.debug("User is Authorized to update");
    }

}
