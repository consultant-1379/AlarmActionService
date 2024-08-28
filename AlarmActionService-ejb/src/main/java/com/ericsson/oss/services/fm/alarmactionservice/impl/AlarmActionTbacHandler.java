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

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.common.tbac.FMTBAC;
import com.ericsson.oss.services.fm.common.tbac.FMTBACInputParameter;

/**
 * This class checks the TBAC authorization for AlarmActionData and performs the corresponding alarm actions.
 **/
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AlarmActionTbacHandler {

    @Inject
    private AlarmActionUpdateHandler alarmActionUpdateHandler;

    /**
     * Performs the alarm actions as specified in the alarmActionData.
     * @param alarmActionData
     *            - an encapsulation of incoming alarm action request data with PoIds, OperatorName, Comment...etc
     *  @return - The list of encapsulated response {@link AlarmActionResponse} data for the action request.
     **/
    @FMTBAC(handlerId = "FMTBACAlarmActionHandler")
    public List<AlarmActionResponse> alarmActionUpdateWithTbac(@FMTBACInputParameter final AlarmActionData alarmActionData) {
        return alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);
    }

}
