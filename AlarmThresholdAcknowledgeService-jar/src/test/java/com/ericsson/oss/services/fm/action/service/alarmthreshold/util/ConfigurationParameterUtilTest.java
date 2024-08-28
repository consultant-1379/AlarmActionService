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


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.ConfigurationParameterUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationParameterUtilTest {

    @InjectMocks
    private ConfigurationParameterUtil configurationParameterUtil;

    @Test
    public void isSmallENMTest() {
      assertEquals(true, configurationParameterUtil.isSmallEnm());
    }

    @Test
    public void getNumberOfReplicasTest() {
      assertEquals(0, configurationParameterUtil.getNumberOfReplicas());
    }

}
