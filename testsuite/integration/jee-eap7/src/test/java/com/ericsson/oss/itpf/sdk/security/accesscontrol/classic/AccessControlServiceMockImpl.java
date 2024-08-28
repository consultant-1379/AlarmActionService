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

import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityAction;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityResource;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject;
import com.ericsson.oss.services.security.accesscontrol.AccessControlService;

@Stateless
public class AccessControlServiceMockImpl implements AccessControlService {

    @Override
    public boolean isTBACRestrictedForSubject(final String subject) {
        if (subject != null) {
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getUserRoles(final String s) {
        return null;
    }

    @Override
    public Map<ESecurityResource, Set<ESecurityAction>> getCapabilitiesForSubject(final ESecuritySubject eSecuritySubject,
                                                                                  final Set<ESecurityResource> set) {
        return null;
    }

    @Override
    public Map<ESecurityResource, Set<ESecurityAction>> getCapabilitiesForSubjectWithAuthorize(final ESecuritySubject eSecuritySubject,
                                                                                               final Set<ESecurityResource> set) {
        return null;
    }
}
