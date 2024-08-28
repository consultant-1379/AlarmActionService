/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.fm.alarmactionservice.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DOWNARD_ACK_SUPPORTED_NODETYPES;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OSS_EDT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.VERSION;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.direct.DirectModelAccess;
import com.ericsson.oss.itpf.modeling.schema.gen.oss_edt.EnumDataTypeDefinition;
import com.ericsson.oss.itpf.modeling.schema.gen.oss_edt.EnumDataTypeMember;
import com.ericsson.oss.services.fm.alarmactionservice.util.ModelServiceHelper;
import com.ericsson.oss.services.fm.edt.configuration.DownwardAckSupportedNodeTypes;

@RunWith(MockitoJUnitRunner.class)
public class ModelServiceHelperTest {

    @InjectMocks
    private ModelServiceHelper modelServiceHelper;

    @Mock
    private ModelService modelService;

    @Mock
    private DirectModelAccess directModelAccess;

    @Mock
    private EnumDataTypeDefinition enumDataTypeDefinition;

    @Mock
    private List<EnumDataTypeMember> enumDataTypeMemberList;

    @Mock
    private EnumDataTypeMember enumDataTypeMember;

    @Test
    public void testreadFmMediationServiceTypeModel(){
        when(modelService.getDirectAccess()).thenReturn(directModelAccess);
        when(directModelAccess.getAsJavaTree(new ModelInfo(OSS_EDT, FM, DOWNARD_ACK_SUPPORTED_NODETYPES, VERSION), EnumDataTypeDefinition.class)).thenReturn(enumDataTypeDefinition);
        final Iterator iterator = Mockito.mock(Iterator.class);
        when(enumDataTypeDefinition.getMember()).thenReturn(enumDataTypeMemberList);
        when(enumDataTypeMemberList.iterator()).thenReturn(iterator);
        Mockito.when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(iterator.next()).thenReturn(enumDataTypeMember);
        Mockito.when(enumDataTypeMember.getName()).thenReturn(DownwardAckSupportedNodeTypes.ERBS.name());
        final List<String> result = modelServiceHelper.readEdtModelInformation(DOWNARD_ACK_SUPPORTED_NODETYPES,VERSION);
        assertEquals(DownwardAckSupportedNodeTypes.ERBS.name(),result.get(0));
    }

    @Test
    public void testreadFmMediationServiceTypeModel_throwsException(){
        when(modelService.getDirectAccess()).thenReturn(null);
        final List<String> result = modelServiceHelper.readEdtModelInformation(DOWNARD_ACK_SUPPORTED_NODETYPES,VERSION);
        assertTrue(result.isEmpty());
    }

}
