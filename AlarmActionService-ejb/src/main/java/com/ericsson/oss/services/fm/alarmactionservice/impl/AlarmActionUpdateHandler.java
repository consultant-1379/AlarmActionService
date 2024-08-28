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

package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;

/**
 * This class provides the implementation for performing the alarm action update.
 **/
public class AlarmActionUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionUpdateHandler.class);

    @Inject
    private SystemRecorder systemRecorder;

    @Inject
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Inject
    private ConfigurationsChangeListener actionConfigurationListener;

    /**
     * Performs the alarm actions as specified in the alarmActionData after verifying it.
     * @param alarmActionData
     *            - an encapsulation of incoming alarm action request data with PoIds, OperatorName, Comment...etc
     *  @return - The list of encapsulated response {@link AlarmActionResponse} data for the action request.
     **/
    public List<AlarmActionResponse> alarmActionUpdate(final AlarmActionData alarmActionData) {
        List<AlarmActionResponse> actionResponse = null;
        AlarmActionResponse alarmActionResponse = null;

        if (isNotBlank(alarmActionData.getObjectOfReference()) || (alarmActionData.getAlarmIds() != null && !alarmActionData.getAlarmIds().isEmpty())) {
            if (alarmActionData.getAlarmAction() != null) {
                final List<Long> poIds = alarmActionData.getAlarmIds();
                systemRecorder.recordCommand(alarmActionData.getAlarmAction().toString(), CommandPhase.STARTED, "AlarmActionService",
                        "AlarmId(s): " + alarmActionData.getAlarmIds(), "PoId(s): " + alarmActionData.getPoIds());
                if (poIds == null || poIds.isEmpty()) {
                    LOGGER.debug("Request to perform Alarm Action is received with ObjectOfReference and null PoIds {}", alarmActionData);
                    actionResponse = alarmActionsBatchManager.processAlarmActionsWithoutBatching(alarmActionData);
                } else {
                    LOGGER.debug("Request to perform Alarm Action is received with AlarmActionData {}", alarmActionData);
                    actionResponse = alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIds, null,
                            actionConfigurationListener.getAlarmActionBatchSize(), false);
                }
            } else {
                LOGGER.info("AlarmAction to be performed is not specified ");
                alarmActionResponse = new AlarmActionResponse();
                alarmActionResponse.setResponse(AlarmActionConstants.ACTION_NOT_SPECIFIED);
                actionResponse = new ArrayList<AlarmActionResponse>(1);
                actionResponse.add(alarmActionResponse);
            }
        } else {
            LOGGER.info("Received AlarmActionData: {} is not proper", alarmActionData);
            alarmActionResponse = new AlarmActionResponse();
            alarmActionResponse.setResponse(AlarmActionConstants.NOT_PROPER_INPUT);
            actionResponse = new ArrayList<AlarmActionResponse>(1);
            actionResponse.add(alarmActionResponse);
        }
        return actionResponse;
    }

}
