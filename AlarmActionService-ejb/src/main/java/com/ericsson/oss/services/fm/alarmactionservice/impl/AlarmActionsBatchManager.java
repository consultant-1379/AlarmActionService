/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EXCEPTION_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.QUEUE_EXCEPTION_MESSAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.handlers.FailedAlarmActionsValidator;
import com.ericsson.oss.services.fm.alarmactionservice.timer.AutoAckRetryTimerHandler;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;

/**
 * This class is responsible for Batching the alarms from the incoming request and send each batch for further processing with the respective
 * configured retryMechanism ({@link RetryManager}) or simply send without any retryMechanism. This class has the Batching implementation for both
 * AutoAck request (with retryMechanism) and normal AlarmActions from the clients (without retryMechanism).
 */
public class AlarmActionsBatchManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionsBatchManager.class);

    @Inject
    private RetryManager retryManager;

    @Inject
    private AlarmActionsTransactionalFacade alarmActionsTransactionalFacade;

    @Inject
    private FailedAlarmActionsValidator failedAlarmActionsValidator;

    /**
     * Batches the incoming Action request based on the parameters 'poIds' and 'batchSize' and forwards for the further processing.
     * @param alarmActionData
     *            - an encapsulation of incoming alarm action request data with PoIds, OperatorName, Comment...etc
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @param policy
     *            - an encapsulation of the RetryPolicy to be applied on the retries to be performed in each of the failed requests
     * @param batchSize
     *            - Size of the each batch to be sent for further processing
     * @param isAutoAckRequest
     *            - a Boolean that tells whether the incoming request is for AutoAck or for normal Alarm Action
     * @return - The list of encapsulated response {@link AlarmActionResponse} data for the action request.
     */
    public List<AlarmActionResponse> processAlarmActionsInBatches(final AlarmActionData alarmActionData, final List<Long> poIds,
            final RetryPolicy policy, final int batchSize, final boolean isAutoAckRequest) {
        final List<AlarmActionResponse> actionResponse = new ArrayList<AlarmActionResponse>();
        final List<List<Long>> poIdBatches = new ArrayList<>();
        for (int counter = 0; counter < poIds.size(); counter += batchSize) {
            poIdBatches.add(poIds.subList(counter, counter + Math.min(batchSize, poIds.size() - counter)));
        }
        for (final List<Long> poIdsBatch : poIdBatches) {
            final List<AlarmActionResponse> actionResponseForSingleBatch = processForEachBatch(alarmActionData, policy, poIdsBatch, isAutoAckRequest);
            actionResponse.addAll(actionResponseForSingleBatch);
        }
        return actionResponse;
    }

    /**
     * Each batch is processed with the differentiation of the AutoACK request or normal Action request and proceed further with some retryMechanism
     * or with no retryMechanism.
     * @param alarmActionData
     *            - an encapsulation of incoming alarm action request data with PoIds, OperatorName, Comment...etc
     * @param policy
     *            - an encapsulation of the RetryPolicy to be applied on the retries to be performed in each of the failed requests
     * @param subList
     *            - SubList of PoIds on which the actions has to be performed
     * @param isAutoAckRequest
     *            - a Boolean that tells whether the incoming request is for AutoAck or for normal Alarm Action
     * @return - The list of encapsulated response {@link AlarmActionResponse} data for the action request.
     */
    private List<AlarmActionResponse> processForEachBatch(final AlarmActionData alarmActionData, final RetryPolicy policy, final List<Long> subList,
            final boolean isAutoAckRequest) {
        List<AlarmActionResponse> actionResponse = new ArrayList<AlarmActionResponse>();
        final String operatorName = alarmActionData.getOperatorName();
        if (isAutoAckRequest) {
            if (policy != null) {
                try {
                    retryMechanism(policy, operatorName, subList);
                } catch (final Exception exception) {
                    LOGGER.error(
                            "Max retries to autoAck alarms reached for the batch of size {}. AutoAck Operation is Discarded with the error : {} ",
                            subList.size(), exception.getMessage());
                    LOGGER.info(
                            "Max retries to autoAck alarms reached for the batch of size {}. AutoAck Operation is Discarded with the error :",
                            subList.size(), exception);
                    AutoAckRetryTimerHandler.failedPoids.addAll(subList);
                }
            } else {
                try {
                    alarmActionsTransactionalFacade.performAutoAckForSingleBatch(operatorName, subList);
                    AutoAckRetryTimerHandler.failedPoids.removeAll(subList);
                } catch (final Exception exception) {
                    LOGGER.error("Exception {} caught while retrying the ACK on the failedPoIds list of size {}, not removed from the failedPoids",
                            exception, AutoAckRetryTimerHandler.failedPoids.size());
                    AutoAckRetryTimerHandler.failedPoids.addAll(subList);
                }
            }
        } else {
            actionResponse = performAlarmAction(subList, operatorName, alarmActionData.getAlarmAction(), alarmActionData.getComment() == null ? null
                    : alarmActionData.getComment());
        }

        return actionResponse;
    }

    /**
     * Each batch is sent to the next level of processing in a fresh transaction separately for each type of Alarm Action.
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param alarmAction
     *            - An Enum representation of the type of alarm action {@link AlarmAction} to be performed on the alarm
     * @param comment
     *            - The Comment Text to be added to an Alarm
     * @return - The list of encapsulated response {@link AlarmActionResponse} data for the action request.
     */
    protected List<AlarmActionResponse> performAlarmAction(final List<Long> poIds, final String operatorName, final AlarmAction alarmAction,
            final String comment) {
        List<AlarmActionResponse> actionResponse = null;
        try {
            if (AlarmAction.ACK.equals(alarmAction)) {
                actionResponse = alarmActionsTransactionalFacade.performAckForSingleBatch(operatorName, poIds);
            } else if (AlarmAction.UNACK.equals(alarmAction)) {
                actionResponse = alarmActionsTransactionalFacade.performUnAckForSingleBatch(operatorName, poIds);
            } else if (AlarmAction.COMMENT.equals(alarmAction)) {
                actionResponse = alarmActionsTransactionalFacade.performCommentForSingleBatch(operatorName, poIds, comment);
            } else {
                actionResponse = alarmActionsTransactionalFacade.performClearForSingleBatch(operatorName, poIds);
            }
        } catch (final Exception exception) {
            LOGGER.error("Caught exception while performing action : ", exception);
            String exceptionMessage = EXCEPTION_MESSAGE;
            if (QUEUE_EXCEPTION_MESSAGE.equals(exception.getMessage())) {
                exceptionMessage = QUEUE_EXCEPTION_MESSAGE;
            }
            final Map<Long, Map<String, Object>> alarmRecords = failedAlarmActionsValidator.readOpenAlarms(poIds);
            actionResponse = new ArrayList<AlarmActionResponse>(poIds.size());
            for (final Map.Entry<Long, Map<String, Object>> entry : alarmRecords.entrySet()) {
                actionResponse.add(AlarmActionUtils.buildAlarmActionResponse(entry.getValue(), exceptionMessage));
            }
        }

        return actionResponse;
    }

    /**
     * This is method is responsible for processing the batches of requests with the retryMechanism which is defined in the retry policy
     * {@link RetryPolicy }.
     * @param policy
     *            - an encapsulation of the RetryPolicy to be applied on the retries to be performed in each of the failed requests
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     */
    private void retryMechanism(final RetryPolicy policy, final String operatorName, final List<Long> poIds) {
        retryManager.executeCommand(policy, new RetriableCommand<Void>() {
            @Override
            public Void execute(final RetryContext retryContext) throws Exception {
                final int currentAttempt = retryContext.getCurrentAttempt();
                if (currentAttempt > 1) {
                    LOGGER.debug("In the execute method for the batch of size {} with the retry mechanism. CurrentAttempt is {}", poIds.size(),
                            currentAttempt);
                }
                alarmActionsTransactionalFacade.performAutoAckForSingleBatch(operatorName, poIds);
                return null;
            }
        });
    }

    /**
     * This is method is responsible for processing the incoming Alarm Action requests without any batches when the operation is to be performed on
     * the basis of the nodes rather PoIds.
     * @param alarmActionData
     *            - an encapsulation of incoming alarm action request data with PoIds, OperatorName, Comment...etc
     * @return - The list of encapsulated response {@link AlarmActionResponse} data for the action request.
     */
    public List<AlarmActionResponse> processAlarmActionsWithoutBatching(final AlarmActionData alarmActionData) {
        return alarmActionsTransactionalFacade.performActionsWithoutBatching(alarmActionData);
    }
}
