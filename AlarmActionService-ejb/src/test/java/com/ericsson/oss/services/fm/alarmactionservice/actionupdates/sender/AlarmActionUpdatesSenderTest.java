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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LAST_DELIVERED;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ATRInputEvent;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionUpdatesSenderTest {

    @InjectMocks
    private AlarmActionUpdatesSender alarmActionUpdatesSender;

    @Mock
    private EventSender<ProcessedAlarmEvent> modeledEventSender;

    @Mock
    private DownwardOperationRequestSender downwardOperationRequestSender;

    @Mock
    private EventSender<ATRInputEvent> atrModeledEventSender;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Test
    public void sendAlarmActionsToQueues_test() {
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setActionUpdatedInDb(true);
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setAlarmAction("ACK");
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put(ALARMSTATE, CLEARED_ACKNOWLEDGED);
        final Date lastUpdted = new Date();
        alarmAttributes.put(LASTUPDATED, lastUpdted);
        alarmAttributes.put(LAST_DELIVERED, lastUpdted.getTime());
        alarmActionInformation.setAlarmAttributes(alarmAttributes);
        alarmActionInformations.add(alarmActionInformation);
        alarmActionUpdatesSender.sendAlarmActionsToQueues(alarmActionInformations, "operator", "ACK");
        verify(downwardOperationRequestSender).prepareAndSendMediationTaskRequest(Matchers.anyList(), Matchers.anyString(), Matchers.anyString());
    }

    @Test
    public void sendFailedAlarmActionsToQueues_test() {
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setActionUpdatedInDb(true);
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setAlarmAction("ACK");
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put(ALARMSTATE, CLEARED_ACKNOWLEDGED);
        final Date lastUpdted = new Date();
        alarmAttributes.put(LASTUPDATED, lastUpdted);
        alarmAttributes.put(LAST_DELIVERED, lastUpdted.getTime());
        alarmActionInformation.setAlarmAttributes(alarmAttributes);
        alarmActionInformations.add(alarmActionInformation);
        alarmActionUpdatesSender.sendFailedAlarmActionsToQueues(alarmActionInformations);
        verify(downwardOperationRequestSender).prepareAndSendFailedActionsMediationTaskRequest(Matchers.anyList());
    }

}
