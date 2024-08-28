/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.action.service.alarmthreshold.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterListener;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterUtil;


@RunWith(MockitoJUnitRunner.class)
public class ConfigurationParameterListenerTest {

    @InjectMocks
    private ConfigurationParameterListener configurationParameterListener;

    @Mock
    private ConfigurationParameterUtil configurationParameterUtil;

    @Test
    public void observeForAlarmThresholdForNotificationForCENMTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(1);
      configurationParameterListener.setAlarmThresholdForForceAck(60000);
      configurationParameterListener.observeForAlarmThresholdForNotification(50000);
      configurationParameterListener.observeForAlarmThresholdForNotification(40000);
      assertEquals(40000, configurationParameterListener.getAlarmThresholdForNotification());
    }

    @Test
    public void observeForAlarmThresholdForNotificationForCENMWith2ReplicasTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(2);
      configurationParameterListener.setAlarmThresholdForForceAck(60000);
      configurationParameterListener.observeForAlarmThresholdForNotification(50000);
      assertEquals(50000, configurationParameterListener.getAlarmThresholdForNotification());
    }

    @Test
    public void observeForAlarmThresholdForNotificationForPhysicalTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(0);
      when(configurationParameterUtil.isSmallEnm()).thenReturn(false);
      configurationParameterListener.setAlarmThresholdForForceAck(120000);
      configurationParameterListener.observeForAlarmThresholdForNotification(50000);
      assertEquals(50000, configurationParameterListener.getAlarmThresholdForNotification());
    }

    @Test
    public void observeForAlarmThresholdForNotificationForCloudTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(0);
      when(configurationParameterUtil.isSmallEnm()).thenReturn(true);
      configurationParameterListener.setAlarmThresholdForForceAck(60000);
      configurationParameterListener.observeForAlarmThresholdForNotification(50000);
      configurationParameterListener.observeForAlarmThresholdForNotification(40000);
      assertEquals(40000, configurationParameterListener.getAlarmThresholdForNotification());
    }

    @Test
    public void observeForAlarmThresholdForForceAckForCENMTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(1);
      configurationParameterListener.setAlarmThresholdForNotification(40000);
      configurationParameterListener.observeForAlarmThresholdForForceAck(70000);
      configurationParameterListener.observeForAlarmThresholdForForceAck(60000);
      assertEquals(60000, configurationParameterListener.getAlarmThresholdForForceAck());
    }

    @Test
    public void observeForAlarmThresholdForForceAckForCENMWith2ReplicasTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(2);
      configurationParameterListener.setAlarmThresholdForNotification(40000);
      configurationParameterListener.observeForAlarmThresholdForForceAck(70000);
      assertEquals(70000, configurationParameterListener.getAlarmThresholdForForceAck());
    }

    @Test
    public void observeForAlarmThresholdForForceAckForPhysicalTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(0);
      when(configurationParameterUtil.isSmallEnm()).thenReturn(false);
      configurationParameterListener.setAlarmThresholdForNotification(40000);
      configurationParameterListener.observeForAlarmThresholdForForceAck(70000);
      assertEquals(70000, configurationParameterListener.getAlarmThresholdForForceAck());
    }

    @Test
    public void observeForAlarmThresholdForForceAckForCloudTest() {
      when(configurationParameterUtil.getNumberOfReplicas()).thenReturn(0);
      when(configurationParameterUtil.isSmallEnm()).thenReturn(true);
      configurationParameterListener.setAlarmThresholdForNotification(40000);
      configurationParameterListener.observeForAlarmThresholdForForceAck(70000);
      configurationParameterListener.observeForAlarmThresholdForForceAck(60000);
      assertEquals(60000, configurationParameterListener.getAlarmThresholdForForceAck());
    }

}
