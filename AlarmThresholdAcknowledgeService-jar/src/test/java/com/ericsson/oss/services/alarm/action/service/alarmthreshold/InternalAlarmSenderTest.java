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
package com.ericsson.oss.services.alarm.action.service.alarmthreshold;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.mediation.translator.model.EventNotification;
import com.ericsson.oss.mediation.translator.model.EventNotificationBatch;
import com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold.InternalAlarmSender;

@RunWith(MockitoJUnitRunner.class)
public class InternalAlarmSenderTest {

    @InjectMocks
    private InternalAlarmSender internalAlarmSender;

    @Mock
    private EventSender<EventNotificationBatch> eventSender;

    final List<EventNotification> notifList = new ArrayList<EventNotification>(1);

    @Test
    public void testSendInternalAlarmNotification() {

        final String severity = "CRITICAL";
        final long activeAlarmCount = 100;
        internalAlarmSender.sendInternalAlarmNotification(severity, activeAlarmCount);
        Mockito.verify(eventSender, VerificationModeFactory.times(1)).send((EventNotificationBatch) Mockito.anyObject());
    }

}
