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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityPrivilege;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityRole;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityTargetGroup;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityServiceInternalFailureException;

public class SecurityPrivilegeServiceMock extends PIPHandlerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPrivilegeServiceMock.class);

    public static final String FM_USER = "FM-USER";

    public static final ESecurityTargetGroup SEC_TARGET_GROUP_1 = new ESecurityTargetGroup("tg1");
    public static final ESecurityTargetGroup SEC_TARGET_GROUP_2 = new ESecurityTargetGroup("tg2");
    public static final ESecurityTargetGroup SEC_TARGET_GROUP_ALL = new ESecurityTargetGroup(PIPHandlerImpl.TARGET_GROUP_ALL);

    @Override
    public Set<ESecurityPrivilege> getSubjectPrivileges(final ESecuritySubject subject) throws SecurityServiceInternalFailureException {
        LOGGER.warn("************************************************************");
        LOGGER.warn("SecurityPrivilegeServiceMock IS NOT FOR PRODUCTION USE.     ");
        LOGGER.warn("SecurityPrivilegeServiceMock: getSubjectPrivileges(): {}", subject.getSubjectId());
        LOGGER.warn("************************************************************");
        if (FM_USER.equals(subject.getSubjectId())) {
            final Set<ESecurityPrivilege> subjectPrivileges = new HashSet<>();
            subjectPrivileges.add(new ESecurityPrivilege(SEC_TARGET_GROUP_1, new ESecurityRole("")));
            LOGGER.info("SubjectPrivileges() {} : {}", subject.getSubjectId(), subjectPrivileges.toString());
            return subjectPrivileges;
        } else {
            return null;
        }
    }
}
