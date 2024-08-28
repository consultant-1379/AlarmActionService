/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.alarm.action.service.api;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;

/**
 * This interface for performing alarm actions acknowledgement , un-acknowledgement and clear.
 **/
@EService
@Remote
public interface AlarmActionService {

    /**
     * Method takes Alarm Action Data as input and perform Action based on the attributes.
     * @Method : alarmActionUpdate
     * @param AlarmActionData
     **/
    List<AlarmActionResponse> alarmActionUpdate(AlarmActionData alarmActionData);

    /**
     * Method takes Alarm Action Data as input and perform Action based on the attributes based on Authorization.
     * @Method : alarmActionUpdate
     * @param AlarmActionData
     **/
    List<AlarmActionResponse> alarmActionUpdate(AlarmActionData alarmActionData, boolean authorized);

    /**
     * Method takes Alarm Action Data as input and perform clear based on the attributes.
     * @Method : alarmActionUpdate
     * @param AlarmActionData
     **/
    @Deprecated
    List<AlarmActionResponse> alarmActionUpdateforMultipleClear(AlarmActionData alarmActionData);

    /**
     * Method takes Alarm Action Data as input and perform Action based on the attributes based on Authorization.
     * @Method : alarmActionUpdate
     * @param AlarmActionData
     **/
    @Deprecated
    List<AlarmActionResponse> alarmActionUpdateforMultipleClear(AlarmActionData alarmActionData, boolean authorized);

    /**
     * Method takes Alarm Action Data as input and perform clear Action based on the attributes.
     * @Method : alarmActionUpdate
     * @param AlarmActionData
     **/

    List<AlarmActionResponse> clear(AlarmActionData alarmActionData);

    /**
     * Method takes Alarm Action Data as input and perform Action based on the attributes based on Authorization.
     * @Method : alarmActionUpdate
     * @param AlarmActionData
     **/

    List<AlarmActionResponse> clear(AlarmActionData alarmActionData, boolean authorized);

    /**
     * Method takes AlarmActionData as input and perform AutoAck in batches based on the attributes.
     * @Method : performAutoAck
     * @param AlarmActionData
     **/
    void performAutoAck(AlarmActionData alarmActionData);
}