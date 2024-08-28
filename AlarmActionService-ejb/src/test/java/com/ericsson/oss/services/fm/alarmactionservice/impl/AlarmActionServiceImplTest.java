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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionServiceImplTest {

    @InjectMocks
    private AlarmActionServiceImpl alarmActionServiceImpl;

    @Mock
    private AlarmActionServiceImpl alarmActionServiceImplMock;

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

    @Mock
    private AuthorizationHandler authorizationHandler;

    @Mock
    private AlarmActionTbacHandler alarmActionTbacHandler;

    @Mock
    private AlarmActionUpdateHandler alarmActionUpdateHandler;

    AlarmActionData alarmActionData = new AlarmActionData();

    String fdn = "MeContext=1,ManageElement=1,EnodeBFunction=1";
    List<String> alarmAttributes = new ArrayList<String>();
    List<String> nodeList = new ArrayList<String>();
    String fdn2 = "MeContext=2,ManageElement=1,EnodeBFunction=1";
    List<Long> alarmIdList = new ArrayList<Long>();
    List<AlarmActionResponse> response = mock(ArrayList.class);
    List<AlarmActionInformation> alarmActionInformations = mock(ArrayList.class);

    @Test
    public void testAlarmActionUpdate() {
        final List<Long> alarmIds = new ArrayList<Long>();
        alarmIds.add(1234L);
        alarmActionData.setObjectOfReference("NetworkElement=1");
        alarmActionData.setAlarmIds(alarmIds);
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionServiceImpl.alarmActionUpdate(alarmActionData);
        verify(alarmActionUpdateHandler, times(1)).alarmActionUpdate(alarmActionData);
    }

    @Test
    public void testalarmActionUpdateforMultipleClear() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setAction(AlarmAction.CLEAR);
        when(actionClearer.performClear(alarmActionData, new ArrayList<AlarmActionResponse>())).thenReturn(alarmActionInformations);
        alarmActionServiceImpl.alarmActionUpdateforMultipleClear(alarmActionData);
    }

    @Test
    public void testAlarmActionUpdateforMultipleClear_Authorized() {
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmActionServiceImpl.alarmActionUpdateforMultipleClear(alarmActionData, true);
        verify(actionClearer, times(1)).performClearforMultipleFdns(alarmActionData, new ArrayList<AlarmActionInformation>());
    }

    @Test
    public void testAlarmActionUpdate_Authorized() {
        final List<Long> alarmIds = new ArrayList<Long>();
        alarmIds.add(1234L);
        alarmActionData.setObjectOfReference("NetworkElement=1");
        alarmActionData.setAlarmIds(alarmIds);
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionServiceImpl.alarmActionUpdate(alarmActionData, true);
        verify(alarmActionTbacHandler, times(1)).alarmActionUpdateWithTbac(alarmActionData);
    }

    @Test
    public void testClear_WithNullPoids() {

        alarmActionServiceImpl.clear(alarmActionData);
        verify(alarmActionsBatchManager, times(1)).processAlarmActionsWithoutBatching(alarmActionData);
    }

    @Test
    public void testClear_WithPoids_Authorized() {
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(1234L);
        alarmActionData.setPoIds(poIds);
        alarmActionServiceImpl.clear(alarmActionData, true);
        verify(alarmActionsBatchManager, times(1)).processAlarmActionsInBatches(alarmActionData, poIds, null, 0, false);
    }

}
