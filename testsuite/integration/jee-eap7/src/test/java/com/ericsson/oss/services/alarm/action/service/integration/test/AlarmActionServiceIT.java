/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.alarm.action.service.integration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.alarm.action.service.integration.base.DummyDataCreator;
import com.ericsson.oss.services.alarm.action.service.integration.util.AuthenticationHandler;
import com.ericsson.oss.services.alarm.action.service.integration.util.MediationTaskRequestListener;
import com.ericsson.oss.services.alarm.action.service.integration.util.ProxyBeanProvider;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionServiceSeverity;

@RunWith(Arquillian.class)
public class AlarmActionServiceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(AlarmActionServiceIT.class);
    private static final String PIB_EAR = "PIB";
    private static final String AAS_EAR = "AlarmActionService";
    private final DefaultHttpClient httpclient = new DefaultHttpClient();

    @Inject
    private ProxyBeanProvider proxyBeanProvider;

    @Inject
    private DummyDataCreator dummyDataCreator;

    @Inject
    private MediationTaskRequestListener mediationTaskRequestListener;

    public void testCleanUp() {
        dummyDataCreator.removeTestAlarms();
        LOGGER.info(" Cleared the Alarms Created for Testing ");
    }

    @Test
    @InSequence(1)
    public void testPerformAckForfdnAndAlarmIDs() {
        assertNotNull(proxyBeanProvider);
        LOGGER.info("Test for Ack with FDN and Alarm IDs");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(2)
    public void testPerformUnAckForfdnAndAlarmIDs() {
        LOGGER.info("Test for UnAck with FDN and Alarm IDs ");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to unack are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(3)
    public void testPerformCommentForfdnAndAlarmIDs() {
        LOGGER.info("Test for Comment with FDN and Alarm IDs");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setComment("Hello World");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to comment are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(4)
    public void testPerformClearForfdnAndAlarmIDs() {
        LOGGER.info("Test for Comment with Only Alarm IDs");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to clear are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(5)
    public void testPerformAckForOnlyAlarmIDs() {
        LOGGER.info("Test for Comment with Only Alarm IDs");
        List<Long> alarmIdList = new ArrayList<>();
        LOGGER.info(" Test for Clear with FDN and Alarm Numbers ");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(6)
    public void testPerformUnAckForOnlyAlarmIDs() {
        LOGGER.info("Test for Comment with Only Alarm IDs ");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to unack are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(7)
    public void testPerformCommentForOnlyAlarmIDs() {
        LOGGER.info("Test for Comment with Only Alarm IDs");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setComment("Hello World");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to comment are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(8)
    public void testPerformClearForOnlyAlarmIDs() {
        LOGGER.info("Test for Comment with Only Alarm Numbers ");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to clear are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(9)
    public void testPerformAckForOnlyfdn() {
        LOGGER.info("Test for Ack with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        dummyDataCreator.createTestAlarms(1, 5);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(10)
    public void testPerformUnAckForOnlyfdn() {
        LOGGER.info("Test for Un-Ack with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to unack are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(11)
    public void testPerformCommentForOnlyfdn() {
        LOGGER.info("Test for Comment with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setComment("Hello World");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info(" number of Alarms which are failed to comment are " + count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(12)
    public void testPerformClearForOnlyfdn() {
        LOGGER.info("Test for Clear with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to clear are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(13)
    public void testPerformCommentForWithSP() {
        LOGGER.info("Test for Comment with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setComment("Hello World");
        alarmActionData.setSpecificProblem("sntpServerDown");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to comment with sp are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(14)
    public void testPerformCommentForWithPC() {
        LOGGER.info("Test for Comment with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setComment("Hello World");
        alarmActionData.setProbableCause("LossOfSignal");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to comment with pc are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(15)
    public void testPerformCommentForWithET() {
        LOGGER.info("Test for Comment with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setComment("Hello World");
        alarmActionData.setEventType("Equipmentalarm");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to comment with et are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(16)
    public void testPerformCommentForWithSeverity() {
        LOGGER.info("Test for Comment with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setComment("Hello World");
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.CRITICAL);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to comment with severity are {}", count);
        assertNotNull(response);
        testCleanUp();
    }

    @Test
    @InSequence(17)
    @RunAsClient
    public void test_DownwardAckEnable() throws Exception {
        final HttpGet httpget = new HttpGet(
                "http://localhost:8080/pib/configurationService" + "/updateConfigParameterValue?paramName=downwardAck&paramValue=true");
        AuthenticationHandler.addUserPassword(httpget);
        final HttpResponse response = httpclient.execute(httpget);
        assertNotNull("ClientResponse should not be null", response);
        assertEquals("Expecting status not equals", Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        Thread.sleep(2000);
    }

    @Test
    @InSequence(18)
    public void testPerformAckForfdnAndAlarmIDsWhenDownwardAckEnabled() {
        LOGGER.info("testPerformAckForfdnAndAlarmIDsWhenDownwardAckEnabled");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    @Test
    @InSequence(19)
    public void testPerformUnAckForfdnAndAlarmIDsWhenDownwardAckEnabled() {
        LOGGER.info("testPerformUnAckForfdnAndAlarmIDsWhenDownwardAckEnabled");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to unack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    @Test
    @InSequence(20)
    public void testPerformAckWithAlarmIDsWhenDownwardAckEnabled() {
        // Re-setting to count 4 as we test downward ack for 4 different fdn's for which alarms are created with same alarmIds. So, We need to
        // receive 4 mtrs.
        mediationTaskRequestListener.resetLatch(4);
        LOGGER.info("testPerformAckWithAlarmIDsWhenDownwardAckEnabled");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    @Test
    @InSequence(21)
    public void testPerformUnAckWithAlarmIDsWhenDownwardAckEnabled() {
        // Re-setting to count 4 as we test downward unack for 4 different fdn's for which alarms are created with same alarmIds. So, We need to
        // receive 4 mtrs.
        mediationTaskRequestListener.resetLatch(4);
        LOGGER.info("testPerformUnAckWithAlarmIDsWhenDownwardAckEnabled");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to unack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    public Integer getFailureCount(final List<AlarmActionResponse> response) {
        int count = 0;
        for (final AlarmActionResponse alarmActionResponse : response) {
            if (!alarmActionResponse.getResponse().equalsIgnoreCase("SUCCESS")) {
                count++;
            }
        }
        return count;
    }

    @Test
    @InSequence(22)
    public void testPerformAckForfdnAndAlarmIDsWhenDownwardAckEnabledOnECM() {
        LOGGER.info("testPerformAckForfdnAndAlarmIDsWhenDownwardAckEnabledOnECM");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionData.setObjectOfReference("VirtualNetworkFunctionManager=1");
        alarmIdList = dummyDataCreator.createEcmTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    @Test
    @InSequence(23)
    public void testPerformUnAckForfdnAndAlarmIDsWhenDownwardAckEnabledOnECM() {
        LOGGER.info("testPerformUnAckForfdnAndAlarmIDsWhenDownwardAckEnabledOnECM");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmActionData.setObjectOfReference("VirtualNetworkFunctionManager=1");
        alarmIdList = dummyDataCreator.createEcmTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(1, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    @Test
    @InSequence(24)
    public void testPerformClearForfdnAndAlarmIDsWhenDownwardAckEnabledOnECM() {
        LOGGER.info("testPerformClearForfdnAndAlarmIDsWhenDownwardAckEnabledOnECM");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmActionData.setObjectOfReference("VirtualNetworkFunctionManager=1");
        alarmIdList = dummyDataCreator.createEcmTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The currrent latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }

    @Test
    @InSequence(25)
    public void testPerformClearForAlarmIDsWhenDownwardAckEnabledOnECM() {
        LOGGER.info("testPerformClearForAlarmIDsWhenDownwardAckEnabledOnECM");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmIdList = dummyDataCreator.createEcmTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(AAS_EAR);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to ack are {}", count);
        assertNotNull(response);
        testCleanUp();
        try {
            mediationTaskRequestListener.LATCH.await(3, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Exception in waiting for latch count to be decreased: {}", e);
        }
        LOGGER.info("The current latch count {}", mediationTaskRequestListener.getCountInLatch());
        assertEquals(0, mediationTaskRequestListener.getCountInLatch());
        mediationTaskRequestListener.resetLatch(1);
    }
}
