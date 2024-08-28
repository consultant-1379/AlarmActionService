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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.AUTOACK_BATCH_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.EPredefinedRole;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.annotation.Authorize;
import com.ericsson.oss.services.alarm.action.service.api.AlarmActionService;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.common.tbac.FMTBAC;
import com.ericsson.oss.services.fm.common.tbac.FMTBACInputParameter;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * This class checks whether information is enough to perform any action and delegates to respective classes based on the alarm action type received..
 **/
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AlarmActionServiceImpl implements AlarmActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionService.class);

    @Inject
    private ActionClearer actionClearer;

    @Inject
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Inject
    private ConfigurationsChangeListener actionConfigurationListener;

    @Inject
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    @Inject
    private AuthorizationHandler authorizationHandler;

    @Inject
    private AlarmActionUpdateHandler alarmActionUpdateHandler;

    @Inject
    private AlarmActionTbacHandler alarmActionTbacHandler;

    @Override
    public List<AlarmActionResponse> alarmActionUpdate(final AlarmActionData alarmActionData) {
        return alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);
    }

    @Override
    public List<AlarmActionResponse> alarmActionUpdate(final AlarmActionData alarmActionData, final boolean authorized) {
        if (AlarmAction.COMMENT.equals(alarmActionData.getAlarmAction())) {
            authorizationHandler.checkAuthorizationForComment();
        } else {
            authorizationHandler.checkAuthorization();
        }
        return alarmActionTbacHandler.alarmActionUpdateWithTbac(alarmActionData);
    }

    @Override
    public List<AlarmActionResponse> alarmActionUpdateforMultipleClear(final AlarmActionData alarmActionData) {
        LOGGER.debug("Request received for clear with  multiple FDNs {}  ", alarmActionData.getClearFdnList());
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final List<AlarmActionResponse> actionResponse = actionClearer.performClearforMultipleFdns(alarmActionData, alarmActionInformations);
        final String operatorName = alarmActionData.getOperatorName();
        final String alarmAction = alarmActionData.getAlarmAction().name();
        alarmActionUpdatesSender.sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
        alarmActionsCacheManager.removeAll(alarmActionInformations);
        return actionResponse;
    }

    /*
     * Added to differentiate NBI/Auto Ack/Auto Unack and GUI calls.
     */
    @Override
    @Authorize(resource = "open_alarms", action = "update", role = { EPredefinedRole.ADMINISTRATOR, EPredefinedRole.OPERATOR })
    @FMTBAC(handlerId = "FMTBACAlarmActionHandler")
    // allowed for both roles.
    public List<AlarmActionResponse> alarmActionUpdateforMultipleClear(@FMTBACInputParameter final AlarmActionData alarmActionData,
            final boolean authorized) {
        return alarmActionUpdateforMultipleClear(alarmActionData);
    }

    @Override
    public void performAutoAck(final AlarmActionData alarmActionData) {
        final List<Long> poIds = alarmActionData.getAlarmIds();
        final RetryPolicy policy = RetryPolicy.builder().attempts(actionConfigurationListener.getNumberOfRetries())
                .waitInterval(actionConfigurationListener.getSleeptTimeMilliSeconds(), TimeUnit.MILLISECONDS)
                .exponentialBackoff(actionConfigurationListener.getExponentialBackOff()).retryOn(Exception.class).build();
        LOGGER.debug("Request for the AutoACK on the batch {} is received", poIds);
        alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIds, policy, AUTOACK_BATCH_SIZE, true);
    }

    @Override
    public List<AlarmActionResponse> clear(final AlarmActionData alarmActionData) {
        List<AlarmActionResponse> actionResponse = null;
        final List<Long> poIds = alarmActionData.getPoIds();
        if (poIds == null || poIds.isEmpty()) {
            LOGGER.debug("Request to perform CLEAR is received without having PoIds {}", alarmActionData);
            actionResponse = alarmActionsBatchManager.processAlarmActionsWithoutBatching(alarmActionData);
        } else {
            LOGGER.debug("Request to perform CLEAR is received with AlarmActionData {}", alarmActionData);
            actionResponse = alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIds, null,
                    actionConfigurationListener.getAlarmActionBatchSize(), false);
        }

        return actionResponse;
    }

    @Override
    @Authorize(resource = "open_alarms", action = "execute", role = { EPredefinedRole.ADMINISTRATOR, EPredefinedRole.OPERATOR })
    @FMTBAC(handlerId = "FMTBACAlarmActionHandler")
    public List<AlarmActionResponse> clear(@FMTBACInputParameter final AlarmActionData alarmActionData, final boolean authorized) {
        return clear(alarmActionData);
    }

}
