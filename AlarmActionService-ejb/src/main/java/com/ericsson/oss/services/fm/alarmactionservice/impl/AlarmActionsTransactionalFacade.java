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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DB_EXCEPTION_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.QUEUE_EXCEPTION_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * This class is responsible for managing the Transactions for each of the batch considered for the Alarm Actions. This class has the implementation
 * for starting a new Transaction for all the incoming requests with each type of the Alarm Action (Including AutoACK).
 */
public class AlarmActionsTransactionalFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionsTransactionalFacade.class);

    @Inject
    private ActionAcknowledger acknowledger;

    @Inject
    private ActionUnAcknowledger unAcknowledger;

    @Inject
    private ActionCommenter actionCommenter;

    @Inject
    private ActionClearer actionClearer;

    @Inject
    private AlarmActionsCacheManager alarmActionsCacheManager;

    @Inject
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    /**
     * Executes in the incoming AutoACK request for single batch in a new transaction.
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     */
    public void performAutoAckForSingleBatch(final String operatorName, final List<Long> poIds) {
        LOGGER.debug("Trying for the batch AutoACK operation on the batch of size {}", poIds.size());
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = acknowledger.processAckForSingleBatch(operatorName, poIds, actionResponses);
        final String alarmAction = AlarmAction.ACK.name();
        sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    /**
     * Executes in the incoming ACKNOWLEGE request for single batch in a new transaction. And then builds the response for the single batch of alarms.
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */
    public List<AlarmActionResponse> performAckForSingleBatch(final String operatorName, final List<Long> poIds) {
        LOGGER.debug("Trying for the batch ACK operation on the batch of size {}", poIds.size());
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = acknowledger.processAckForSingleBatch(operatorName, poIds, actionResponses);
        final String alarmAction = AlarmAction.ACK.name();
        sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
        return actionResponses;
    }

    /**
     * Executes in the incoming UNACKNOWLEGE request for single batch in a new transaction. And then builds the response for the single batch of
     * alarms.
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */
    public List<AlarmActionResponse> performUnAckForSingleBatch(final String operatorName, final List<Long> poIds) {
        LOGGER.debug("Trying for the batch UNACK operation on the batch of size {}", poIds.size());
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = unAcknowledger.processUnAck(operatorName, poIds, actionResponses);
        final String alarmAction = AlarmAction.UNACK.name();
        sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
        return actionResponses;
    }

    /**
     * Executes in the incoming COMMENT request for single batch in a new transaction. And then builds the response for the single batch of alarms.
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */
    public List<AlarmActionResponse> performCommentForSingleBatch(final String operatorName, final List<Long> poIds, final String comment) {
        LOGGER.debug("Trying for the batch COMMENT operation on the batch of size {}", poIds.size());
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionCommenter.processComment(operatorName, comment, poIds, actionResponses);
        final String alarmAction = AlarmAction.COMMENT.name();
        sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
        return actionResponses;
    }

    /**
     * Executes in the incoming CLEAR request for single batch in a new transaction.
     * @param operatorName
     *            - Name of the Operator who is trying to perform the Alarm Action
     * @param poIds
     *            - List of PoIds on which the actions has to be performed
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */
    public List<AlarmActionResponse> performClearForSingleBatch(final String operatorName, final List<Long> poIds) {
        LOGGER.debug("Trying for the batch CLEAR operation on the batch of size {}", poIds.size());
        final List<AlarmActionResponse> actionResponses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionClearer.clear(operatorName, poIds, actionResponses);
        final String alarmAction = AlarmAction.CLEAR.name();
        sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
        return actionResponses;
    }

    /**
     * Executes in the incoming Alarm Action request in a new Transaction. This method is used in case of no Batching is done.
     * @param alarmActionData
     *            - an encapsulation of incoming alarm action request data with PoIds, OperatorName, Comment...etc
     * @return - The list of encapsulated response in {@link AlarmActionResponse} data for the action request
     */
    public List<AlarmActionResponse> performActionsWithoutBatching(final AlarmActionData alarmActionData) {
        LOGGER.debug("Trying for the without batch actions on data: {}", alarmActionData);
        List<AlarmActionResponse> alarmActionResponses = null;
        try {
            if (alarmActionData.getAlarmAction() != null) {
                final String operatorName = alarmActionData.getOperatorName();
                final String alarmAction = alarmActionData.getAlarmAction().name();
                alarmActionResponses = new ArrayList<AlarmActionResponse>();
                if (AlarmAction.ACK.equals(alarmActionData.getAlarmAction())) {
                    final List<AlarmActionInformation> alarmActionInformations = acknowledger.performAckWithoutBatching(alarmActionData,
                            alarmActionResponses);
                    sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
                } else if (AlarmAction.UNACK.equals(alarmActionData.getAlarmAction())) {
                    final List<AlarmActionInformation> alarmActionInformations = unAcknowledger.performUnAck(alarmActionData, alarmActionResponses);
                    sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
                } else if (AlarmAction.COMMENT.equals(alarmActionData.getAlarmAction())) {
                    final String comment = alarmActionData.getComment();
                    alarmActionData.setComment(comment);
                    final List<AlarmActionInformation> alarmActionInformations = actionCommenter
                            .performComment(alarmActionData, alarmActionResponses);
                    sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
                } else {
                    final List<AlarmActionInformation> alarmActionInformations = actionClearer.performClear(alarmActionData, alarmActionResponses);
                    sendActionsToQueues(alarmActionInformations, operatorName, alarmAction);
                }
            }
        } catch (final Exception exception) {
            LOGGER.error("Caught exception while performing action ", exception);
            String responseMessage = DB_EXCEPTION_MESSAGE;
            if (QUEUE_EXCEPTION_MESSAGE.equals(exception.getMessage())) {
                responseMessage = QUEUE_EXCEPTION_MESSAGE;
            }
            final List<Long> alarmIdList = alarmActionData.getAlarmIds();
            final String inputObjectOfReference = alarmActionData.getObjectOfReference();
            alarmActionResponses = new ArrayList<AlarmActionResponse>();
            for (final Long poId : alarmIdList) {
                final AlarmActionResponse alarmActionResponse = AlarmActionUtils.setActionResponse(responseMessage, inputObjectOfReference, "",
                        poId.toString());
                alarmActionResponses.add(alarmActionResponse);
            }
        }

        return alarmActionResponses;
    }

    /**
     * Method takes List of AlarmActionInformations and send actions to all the Queues and remove action entries in AlarmActionsCache.
     * @param alarmActionInformations
     *            list of alarm actions which we updated in DB.
     */
    private void sendActionsToQueues(final List<AlarmActionInformation> alarmActionInformations, final String operatorName, final String alarmAction) {
        updateActionCacheWithDBOperationSuccess(alarmActionInformations);
        alarmActionUpdatesSender.sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
        alarmActionsCacheManager.removeAll(alarmActionInformations);
    }

    private void updateActionCacheWithDBOperationSuccess(final List<AlarmActionInformation> alarmActionInformations) {
        for (final AlarmActionInformation actionInformation : alarmActionInformations) {
            final AlarmActionInformation actionDetailsInCache = alarmActionsCacheManager.get(actionInformation);
            if (null != actionDetailsInCache) {
                actionDetailsInCache.setActionUpdatedInDb(true);
                alarmActionsCacheManager.put(actionDetailsInCache);
            }
        }
    }

}
