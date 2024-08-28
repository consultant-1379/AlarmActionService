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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionsBatchManagerTest {

    @InjectMocks
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Mock
    private RetryPolicy policy;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private AlarmActionsTransactionalFacade autoackBean;

    @Mock
    private AlarmActionData alarmActionData;

    @Mock
    private RetryManager retryManager;

    @Mock
    private RetriableCommand<Void> command;

    @Mock
    private AlarmActionsTransactionalFacade alarmActionsTransactionalFacade;

    final String operatorName = "AlarmRoutingService";
    final List<Long> poIds = new ArrayList<Long>();

    private void addPoIds() {
        for (int i = 0; i < 12; i++) {
            poIds.add(123L);
        }
    }

    @Test
    public void testProcessAutoAckInBatchesWithPolicySuccess() {
        addPoIds();
        when(alarmActionData.getOperatorName()).thenReturn(operatorName);

        alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIds, policy, 10, true);
    }

    @Test
    public void testProcessAutoAckInBatchesWithOutPolicy() {
        addPoIds();
        when(alarmActionData.getOperatorName()).thenReturn(operatorName);

        alarmActionsBatchManager.processAlarmActionsInBatches(alarmActionData, poIds, null, 10, true);
    }

    @Test
    public void testPerformAlarmAction_ACK() {
        addPoIds();
        alarmActionsBatchManager.performAlarmAction(poIds, operatorName, AlarmAction.ACK, null);
        verify(alarmActionsTransactionalFacade, VerificationModeFactory.times(1)).performAckForSingleBatch(operatorName, poIds);

    }

    @Test
    public void testPerformAlarmAction_UNACK() {
        addPoIds();
        alarmActionsBatchManager.performAlarmAction(poIds, operatorName, AlarmAction.UNACK, null);
        verify(alarmActionsTransactionalFacade, VerificationModeFactory.times(1)).performUnAckForSingleBatch(operatorName, poIds);

    }

    @Test
    public void testPerformAlarmAction_Clear() {
        addPoIds();
        alarmActionsBatchManager.performAlarmAction(poIds, operatorName, AlarmAction.CLEAR, null);
        verify(alarmActionsTransactionalFacade, VerificationModeFactory.times(1)).performClearForSingleBatch(operatorName, poIds);

    }

    @Test
    public void testPerformAlarmAction_Comment() {
        addPoIds();
        alarmActionsBatchManager.performAlarmAction(poIds, operatorName, AlarmAction.COMMENT, "commentText");
        verify(alarmActionsTransactionalFacade, VerificationModeFactory.times(1)).performCommentForSingleBatch(operatorName, poIds, "commentText");

    }

    @Test
    public void testProcessAlarmActionsWithoutBatching() {
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setAlarmNumber("123");
        alarmActionResponse.setEventPoId("1234");
        final List<AlarmActionResponse> alarmActionResponses = new ArrayList<AlarmActionResponse>();
        alarmActionResponses.add(alarmActionResponse);
        when(alarmActionsTransactionalFacade.performActionsWithoutBatching(alarmActionData)).thenReturn(alarmActionResponses);

        final List<AlarmActionResponse> result = alarmActionsBatchManager.processAlarmActionsWithoutBatching(alarmActionData);
        assertEquals(result.get(0).getAlarmNumber(), "123");

    }
}
