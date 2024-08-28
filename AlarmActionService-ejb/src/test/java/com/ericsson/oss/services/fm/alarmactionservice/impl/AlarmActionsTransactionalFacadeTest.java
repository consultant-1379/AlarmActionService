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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionsTransactionalFacadeTest {

    @InjectMocks
    private AlarmActionsTransactionalFacade alarmActionsTransactionalFacade;

    @Mock
    private ActionCommenter actionCommenter;

    @Mock
    private ActionAcknowledger acknowledger;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private ActionClearer actionClearer;

    @Mock
    private AlarmActionUtils alarmActionUtils;

    @Mock
    private PersistenceObject persistenceObject;

    @Mock
    private ActionUnAcknowledger unAcknowledger;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    @Mock
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    final String operatorName = "AlarmRoutingService";
    final List<Long> poIds = new ArrayList<Long>();
    final List<AlarmActionResponse> alarmActionResponses = new ArrayList<AlarmActionResponse>();
    final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();

    @Before
    public void setUp() {
        poIds.add(123456L);
        poIds.add(456789L);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findPosByIds(poIds)).thenReturn(persistenceObjects);
    }

    private AlarmActionData setUpForAlarmActionData() {
        final AlarmActionData alarmActionData = new AlarmActionData();
        final List<Long> alarmIds = new ArrayList<Long>();
        alarmIds.add(111L);
        alarmActionData.setAlarmIds(alarmIds);
        alarmActionData.setObjectOfReference("NetworkElement=1");
        alarmActionData.setPoIds(poIds);
        alarmActionData.setOperatorName(operatorName);
        return alarmActionData;
    }

    @Test
    public void testPerformAutoAckForSingleBatch() {
        when(acknowledger.processAckForSingleBatch(operatorName, poIds, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performAutoAckForSingleBatch(operatorName, poIds);
        final String alarmAction = AlarmAction.ACK.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    @Test
    public void testProcessAckForSingleBatch() {
        when(acknowledger.processAckForSingleBatch(operatorName, poIds, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performAckForSingleBatch(operatorName, poIds);
        final String alarmAction = AlarmAction.ACK.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    @Test
    public void testPerformUnAckForSingleBatch() {
        when(unAcknowledger.processUnAck(operatorName, poIds, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performUnAckForSingleBatch(operatorName, poIds);
        final String alarmAction = AlarmAction.UNACK.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    @Test
    public void testPerformCommentForSingleBatch() {
        when(actionCommenter.processComment(operatorName, "comment", poIds, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performCommentForSingleBatch(operatorName, poIds, "comment");
        final String alarmAction = AlarmAction.COMMENT.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    @Test
    public void testPerformClearForSingleBatch() {
        when(actionClearer.clear(operatorName, poIds, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performClearForSingleBatch(operatorName, poIds);
        final String alarmAction = AlarmAction.CLEAR.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);

    }

    @Test
    public void testPerformActionsWithoutBatching_Ack() {

        final AlarmActionData alarmActionData = setUpForAlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        when(acknowledger.performAckWithoutBatching(alarmActionData, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performActionsWithoutBatching(alarmActionData);
        final String alarmAction = AlarmAction.ACK.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    @Test
    public void testPerformActionsWithoutBatching_UnAck() {

        final AlarmActionData alarmActionData = setUpForAlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        when(acknowledger.performAckWithoutBatching(alarmActionData, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performActionsWithoutBatching(alarmActionData);
        final String alarmAction = AlarmAction.UNACK.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

    @Test
    public void testPerformActionsWithoutBatching_Comment() {

        final AlarmActionData alarmActionData = setUpForAlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setComment("comment");
        when(acknowledger.performAckWithoutBatching(alarmActionData, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performActionsWithoutBatching(alarmActionData);
        final String alarmAction = AlarmAction.COMMENT.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);

    }

    @Test
    public void testPerformActionsWithoutBatching_Clear() {

        final AlarmActionData alarmActionData = setUpForAlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        when(acknowledger.performAckWithoutBatching(alarmActionData, alarmActionResponses)).thenReturn(alarmActionInformations);
        alarmActionsTransactionalFacade.performActionsWithoutBatching(alarmActionData);
        final String alarmAction = AlarmAction.CLEAR.name();
        verify(alarmActionUpdatesSender, times(1)).sendAlarmActionsToQueues(alarmActionInformations, operatorName, alarmAction);
    }

}
