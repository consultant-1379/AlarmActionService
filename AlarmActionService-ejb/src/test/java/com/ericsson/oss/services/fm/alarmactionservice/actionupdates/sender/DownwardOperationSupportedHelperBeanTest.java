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

import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DOWNARD_ACK_SUPPORTED_NODETYPES;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.VERSION;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.fm.capability.util.ModelCapabilities;
import com.ericsson.oss.services.fm.alarmactionservice.util.ModelServiceHelper;
import com.ericsson.oss.services.fm.edt.configuration.DownwardAckSupportedNodeTypes;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class DownwardOperationSupportedHelperBeanTest {

    @InjectMocks
    private final DownwardOperationSupportedHelperBean downwardOperationSupportedHelperBean = new DownwardOperationSupportedHelperBean();

    @Mock
    private ModelServiceHelper modelServiceHelper;

    @Mock
    private ModelCapabilities modelCapabilities;

    final List<String> downAckSupportedNodeTypes = new ArrayList<String>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        downAckSupportedNodeTypes.add(DownwardAckSupportedNodeTypes.ERBS.name());
        downAckSupportedNodeTypes.add(DownwardAckSupportedNodeTypes.MGW.name());
        when(modelServiceHelper.readEdtModelInformation(DOWNARD_ACK_SUPPORTED_NODETYPES, VERSION)).thenReturn(downAckSupportedNodeTypes);
    }

    @Test
    public void testAckCapabilitiesFalse() {
        downwardOperationSupportedHelperBean.readDownwardSupportedNodeTypes();
        when(modelCapabilities.isAlarmOperationSupportedByTarget(Matchers.anyString(), Matchers.anyString())).thenReturn(false);
        Assert.assertTrue(downwardOperationSupportedHelperBean.isDownwardOperationSupported(DownwardAckSupportedNodeTypes.MGW.name(), "supportsAck"));
        Assert.assertFalse(
                downwardOperationSupportedHelperBean.isDownwardOperationSupported(DownwardAckSupportedNodeTypes.RNC.name(), "supportsAck"));
    }

    @Test
    public void testAckCapabilitiesTrue() {
        downwardOperationSupportedHelperBean.readDownwardSupportedNodeTypes();
        when(modelCapabilities.isAlarmOperationSupportedByTarget(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
        Assert.assertTrue(downwardOperationSupportedHelperBean.isDownwardOperationSupported(DownwardAckSupportedNodeTypes.MGW.name(), "supportsAck"));
        Assert.assertTrue(downwardOperationSupportedHelperBean.isDownwardOperationSupported(DownwardAckSupportedNodeTypes.RNC.name(), "supportsAck"));
    }

    @Test
    public void testAckECMCapabilitiesTrue() {
        downwardOperationSupportedHelperBean.readDownwardSupportedNodeTypes();
        when(modelCapabilities.isAlarmOperationSupportedByTarget("ECM", "supportsAck")).thenReturn(true);
        Assert.assertTrue(downwardOperationSupportedHelperBean.isDownwardOperationSupported("ECM", "supportsAck"));
    }

    @Test
    public void testClear() {
        downwardOperationSupportedHelperBean.readDownwardSupportedNodeTypes();
        when(modelCapabilities.isAlarmOperationSupportedByTarget(Matchers.anyString(), Matchers.anyString())).thenReturn(false);
        Assert.assertFalse(
                downwardOperationSupportedHelperBean.isDownwardOperationSupported(DownwardAckSupportedNodeTypes.MGW.name(), "supportsClear"));
        Assert.assertFalse(
                downwardOperationSupportedHelperBean.isDownwardOperationSupported(DownwardAckSupportedNodeTypes.RNC.name(), "supportsClear"));
    }

    @Test
    public void testClearECMCapabilitiesTrue() {
        downwardOperationSupportedHelperBean.readDownwardSupportedNodeTypes();
        when(modelCapabilities.isAlarmOperationSupportedByTarget("ECM", "supportsClear")).thenReturn(true);
        Assert.assertTrue(downwardOperationSupportedHelperBean.isDownwardOperationSupported("ECM", "supportsClear"));
    }

    @Test
    public void testClearECMCapabilitiesFalse() {
        downwardOperationSupportedHelperBean.readDownwardSupportedNodeTypes();
        when(modelCapabilities.isAlarmOperationSupportedByTarget("ECM", "supportsClear")).thenReturn(false);
        Assert.assertFalse(downwardOperationSupportedHelperBean.isDownwardOperationSupported("ECM", "supportsClear"));
    }
}
