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

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.alarm.action.service.model.*;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionUpdateHandlerTest {


    @InjectMocks
    private AlarmActionUpdateHandler alarmActionUpdateHandler;

    @Mock
    private ActionAcknowledger acknowledger;

    @Mock
    private ActionUnAcknowledger unAcknowledger;

    @Mock
    private ActionCommenter actionCommenter;

    @Mock
    private ActionClearer actionClearer;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private AlarmActionsBatchManager alarmActionsBatchManager;

    @Mock
    private SystemRecorder systemRecorder;

    @Mock
    private RetryPolicy policy;

    @Mock
    private ConfigurationsChangeListener actionConfigurationListener;

    @Mock
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    AlarmActionData alarmActionData = new AlarmActionData();

    String fdn = "MeContext=1,ManageElement=1,EnodeBFunction=1";
    List<String> alarmAttributes = new ArrayList<String>();
    List<String> nodeList = new ArrayList<String>();
    String fdn2 = "MeContext=2,ManageElement=1,EnodeBFunction=1";
    List<Long> alarmIdList = new ArrayList<Long>();
    List<AlarmActionResponse> response = mock(ArrayList.class);
    List<AlarmActionInformation> alarmActionInformations = mock(ArrayList.class);

    @Test
    public void testPerformAck() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setAction(AlarmAction.ACK);
        when(acknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);

    }

    @Test
    public void testPerformUnAck() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setAction(AlarmAction.UNACK);
        when(unAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);

    }

    @Test
    public void testPerformComment() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setAction(AlarmAction.COMMENT);
        when(actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);

    }

    @Test
    public void testPerformClear() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setAction(AlarmAction.CLEAR);
        when(actionClearer.performClear(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);

    }

    @Test
    public void testWithOutAction() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAlarmIds(alarmIdList);
        when(acknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);
    }

    @Test
    public void testWithAlarmIdList() {
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setAction(AlarmAction.ACK);
        when(acknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);
    }

    @Test
    public void testWithfdn() {
        alarmActionUpdateHandler.alarmActionUpdate(alarmActionData);
    }

}
