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
package com.ericsson.oss.services.alarm.action.service.integration.util;

import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.alarm.action.service.api.AlarmActionService;

@ApplicationScoped
public class ProxyBeanProviderBeanImpl implements ProxyBeanProvider {

    @EServiceRef
    protected AlarmActionService alarmActionService;

    @Override
    public AlarmActionService getAlarmActionService() {
        return alarmActionService;
    }

}
