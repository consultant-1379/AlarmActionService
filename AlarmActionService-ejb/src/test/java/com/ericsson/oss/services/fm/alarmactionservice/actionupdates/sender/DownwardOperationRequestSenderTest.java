/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;

@RunWith(MockitoJUnitRunner.class)
public class DownwardOperationRequestSenderTest {

    @InjectMocks
    private DownwardOperationRequestSender operationRequestSender;

    @Mock
    private EventSender<MediationTaskRequest> mediationTaskSender;

    String fdn = "MeContext=1,ManageElement=1,EnodeBFunction=1";
    String alarmIdListString = "1234";
    int alarmIdSize = 1;
    String action = "ACKNOWLEDGE";
    final String operator = "ackOperator";
    final String additionalInformation =
            ":#sourceType:#alarmId:_2#externalEventId:#fmxToken:CREATE_ALARM#fdn:NetworkElement=85B65#FMX info:Created by rule#operator:#alarmNumber:3#29:#LTE_SeverityAndSynch:SynchAlarmModification#managedObject:PacketFrequencySyncRef#translateResult:FORWARD_ALARM#eventAgentId:";
    List<String> alarmIdList = new ArrayList<String>();

    @Test
    public void testForSendingAckRequestTillNode() {
        operationRequestSender.sendAckRequest(fdn, alarmIdListString, alarmIdSize, action, operator);
        verify(mediationTaskSender, times(1)).send((MediationTaskRequest) anyObject());
    }

    @Test
    public void testForSendingAckRequest() {
        operationRequestSender.sendAckRequest(fdn, alarmIdListString, alarmIdSize, action, operator);
        verify(mediationTaskSender, times(1)).send((MediationTaskRequest) anyObject());
    }

    // @Test
    // public void testAdditionalInformation() {
    // final Map<String, String> additionalInfoMap = AlarmActionUtils.getAdditionalInformation(additionalInformation);
    // Assert.assertEquals("_2", additionalInfoMap.get(ALARMID));
    // }

    @Test
    public void testForSendingClearRequest() {
        operationRequestSender.sendClearRequest(fdn, alarmIdListString, alarmIdSize, operator);
        verify(mediationTaskSender, times(1)).send((MediationTaskRequest) anyObject());
    }

}
