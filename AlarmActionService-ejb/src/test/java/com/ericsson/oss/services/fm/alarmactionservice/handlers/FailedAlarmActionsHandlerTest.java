/*------------------------------------------------------------------------------
 *******************************************************************************
 *COPYRIGHT Ericsson 2017
 *The copyright to the computer program(s)herein is the property of
 *Ericsson Inc.The programs may be used and/or copied only with written
 *permission from Ericsson Inc.or in accordance with the terms and
 *conditions stipulated in the agreement/contract under which the
 *program(s)have been supplied.
 ******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.handlers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACTIONS_TO_HISOTRY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACTIONS_TO_QUEUES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.AlarmActionUpdatesSender;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.FailedAlarmActionUpdatesProcessor;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class FailedAlarmActionsHandlerTest {

    @InjectMocks
    private FailedAlarmActionsHandler failedAlarmActionsHandler;

    @Mock
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    @Mock
    private FailedAlarmActionsValidator failedAlarmActionsValidator;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    @Mock
    private FailedAlarmActionUpdatesProcessor failedAlarmActionUpdatesProcessor;

    @Test
    public void processFailedAlarmActions_test() {
        final List<String> failedJbossInstances = new ArrayList<String>();
        failedJbossInstances.add("svc-1-fmhistory");
        final Map<String, List<AlarmActionInformation>> failedAlarms = new HashMap<String, List<AlarmActionInformation>>();
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setAlarmAction("ACK");
        alarmActionInformation.setOperatorName("Operator");
        alarmActionInformation.setPoId(12345L);
        final Map<String, AlarmActionInformation> failedAlarmActions = new HashMap<String, AlarmActionInformation>();
        failedAlarmActions.put("ACK##Operator##12345", alarmActionInformation);
        final List<AlarmActionInformation> failedActionAlarmsToSent = new ArrayList<AlarmActionInformation>();
        failedActionAlarmsToSent.add(alarmActionInformation);
        failedAlarms.put(ACTIONS_TO_QUEUES, failedActionAlarmsToSent);
        failedAlarms.put(ACTIONS_TO_HISOTRY, failedActionAlarmsToSent);
        when(alarmActionsCacheManager.readFailedAlarmActionsFromCache(failedJbossInstances)).thenReturn(failedAlarmActions);
        when(failedAlarmActionsValidator.validateFailedActionsWithDB(Matchers.anyCollection())).thenReturn(failedAlarms);
        failedAlarmActionsHandler.processFailedAlarmActions(failedJbossInstances);
        verify(alarmActionUpdatesSender).sendFailedAlarmActionsToQueues(failedActionAlarmsToSent);
        verify(failedAlarmActionUpdatesProcessor).processFailedAlarmActionUpdates(failedActionAlarmsToSent, failedJbossInstances);
    }

}
