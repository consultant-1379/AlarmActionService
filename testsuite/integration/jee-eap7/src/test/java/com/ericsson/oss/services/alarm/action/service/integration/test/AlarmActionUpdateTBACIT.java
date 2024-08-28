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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_ACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_UNACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.context.classic.ContextServiceBean;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.SecurityPrivilegeServiceMock;
import com.ericsson.oss.services.alarm.action.service.integration.base.DummyDataCreator;
import com.ericsson.oss.services.alarm.action.service.integration.util.ProxyBeanProvider;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;

@RunWith(Arquillian.class)
public class AlarmActionUpdateTBACIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(AlarmActionUpdateTBACIT.class);
    final ContextServiceBean contextService = new ContextServiceBean();
    @Inject
    private ProxyBeanProvider proxyBeanProvider;
    @Inject
    private DummyDataCreator dummyDataCreator;

    @Before
    public void setupUser() {
        final String HTTP_HEADER_USERNAME_KEY = "X-Tor-UserID";
        contextService.setContextValue(HTTP_HEADER_USERNAME_KEY, SecurityPrivilegeServiceMock.FM_USER);
    }

    @After
    public void tearDown() {
        dummyDataCreator.removeTestAlarms();
        LOGGER.info(" Cleared the Alarms Created for Testing ");

    }

    @Test
    @InSequence(1)
    public void testPerformAckForfdnAndAlarmIDs_NoUser() {
        LOGGER.info("Test for Ack with FDN and Alarm IDs without User");
        contextService.flushContext();
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        alarmIdList = dummyDataCreator.createTestAlarms(1, 6);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData, true);
        final Integer count = getFailureCount(response);
        LOGGER.info("PerformAckForfdnAndAlarmIDs: Number of Alarms which are failed to ack are {} {}", count, response);
        assertEquals(SUCCESS, 10, getResponseStringCount(response, SUCCESS).intValue());
        assertEquals(ALREADY_ACK, 5, getResponseStringCount(response, ALREADY_ACK).intValue());
        assertNotNull(response);
    }

    @Test
    @InSequence(2)
    public void testPerformClearForOnlyAlarmIDs() {
        LOGGER.info("Test for Comment with Only Alarm Numbers ");
        List<Long> alarmIdList = new ArrayList<>();
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmIdList = dummyDataCreator.createTestAlarms(1, 5);
        alarmActionData.setAlarmIds(alarmIdList);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData, true);
        final Integer count = getFailureCount(response);
        LOGGER.info("Number of Alarms which are failed to clear are {}", count);
        assertNotNull(response);
    }

    @Test
    @InSequence(3)
    public void testPerformAckForOnlyfdn() {
        LOGGER.info("Test for Ack with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.ACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 6);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData, true);
        final Integer count = getFailureCount(response);
        LOGGER.info("PerformAckForOnlyfdn: Number of Alarms which are failed to ack are {} {}", count, response);
        assertEquals(2, getResponseStringCount(response, SUCCESS).intValue());
        assertEquals(1, getResponseStringCount(response, ALREADY_ACK).intValue());
        assertNotNull(response);
    }

    @Test
    @InSequence(4)
    public void testPerformUnAckForOnlyfdn() {
        LOGGER.info("Test for Un-Ack with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.UNACK);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 6);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData, true);
        final Integer count = getFailureCount(response);
        LOGGER.info("PerformUnAckForOnlyfdn: Number of Alarms which are failed to unack are {} {}", count, response);
        assertEquals(1, getResponseStringCount(response, SUCCESS).intValue());
        assertEquals(2, getResponseStringCount(response, ALREADY_UNACK).intValue());
        assertNotNull(response);
    }

    @Test
    @InSequence(5)
    public void testPerformCommentForOnlyfdn() {
        LOGGER.info("Test for Comment with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.COMMENT);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 6);
        alarmActionData.setComment("Hello World");
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData, true);
        final Integer count = getFailureCount(response);
        LOGGER.info("PerformCommentForOnlyfdn: number of Alarms which are failed to comment are {} {}", count, response);
        assertEquals(3, getResponseStringCount(response, SUCCESS).intValue());
        assertNotNull(response);
    }

    @Test
    @InSequence(6)
    public void testPerformClearForOnlyfdn() {
        LOGGER.info("Test for Clear with Only FDN");
        final AlarmActionData alarmActionData = new AlarmActionData();
        alarmActionData.setAction(AlarmAction.CLEAR);
        alarmActionData.setObjectOfReference("MeContext=1");
        dummyDataCreator.createTestAlarms(1, 6);
        final List<AlarmActionResponse> response = proxyBeanProvider.getAlarmActionService().alarmActionUpdate(alarmActionData, true);
        final Integer count = getFailureCount(response);
        LOGGER.info("PerformClearForOnlyfdn: Number of Alarms which are failed to clear are {} {}", count, response);
        assertEquals(2, getResponseStringCount(response, SUCCESS).intValue());
        assertEquals(1, getResponseStringCount(response, ALREADY_CLEAR).intValue());
        assertNotNull(response);
    }

    public Integer getFailureCount(final List<AlarmActionResponse> response) {
        int count = 0;
        for (final AlarmActionResponse alarmActionResponse : response) {
            if (!alarmActionResponse.getResponse().equalsIgnoreCase(SUCCESS)) {
                count++;
            }
        }
        return count;
    }

    private Integer getResponseStringCount(final List<AlarmActionResponse> responseList, final String responseString) {
        int count = 0;
        for (final AlarmActionResponse alarmActionResponse : responseList) {
            if (responseString.equalsIgnoreCase(alarmActionResponse.getResponse())) {
                count++;
            }
        }
        return count;
    }

}
