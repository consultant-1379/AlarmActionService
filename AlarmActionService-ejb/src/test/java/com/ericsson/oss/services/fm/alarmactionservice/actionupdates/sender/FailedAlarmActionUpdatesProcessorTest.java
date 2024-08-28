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

package com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LAST_DELIVERED;

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

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.rest.client.FailedAlarmActionUpdatesRestClient;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class FailedAlarmActionUpdatesProcessorTest {

    @InjectMocks
    private FailedAlarmActionUpdatesProcessor failedAlarmActionUpdatesProcessor;

    @Mock
    private RetryManager retryManager;

    @Mock
    private FailedAlarmActionUpdatesRestClient restClient;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Test
    public void processFailedAlarmActionUpdates_test() {
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
        final List<String> failedJbossInstances = new ArrayList<String>();
        failedJbossInstances.add("svc-1-fmhistory");
        when(configurationsChangeListener.getSleeptTimeMilliSeconds()).thenReturn(1000);
        when(configurationsChangeListener.getNumberOfRetries()).thenReturn(3);
        when(configurationsChangeListener.getExponentialBackOff()).thenReturn(2.0);
        failedAlarmActionUpdatesProcessor.processFailedAlarmActionUpdates(alarmActionInformations, failedJbossInstances);
        verify(retryManager).executeCommand((RetryPolicy) Matchers.anyObject(), (RetriableCommand) Matchers.anyObject());
    }

}
