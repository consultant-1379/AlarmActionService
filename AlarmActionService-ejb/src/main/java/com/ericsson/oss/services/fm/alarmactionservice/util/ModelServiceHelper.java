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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OSS_EDT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.direct.DirectModelAccess;
import com.ericsson.oss.itpf.modeling.schema.gen.oss_edt.EnumDataTypeDefinition;
import com.ericsson.oss.itpf.modeling.schema.gen.oss_edt.EnumDataTypeMember;

/**
 * Helper class to retrieve Information from ModelService.
 */
@ApplicationScoped
public class ModelServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceHelper.class);

    @Inject
    private ModelService modelService;

    /**
     * Reads the given EDT type of model with name and version using ModelService and returns list of all the members defined in the model.
     * @param modelName
     *            The model name to be read from model service
     * @param modelVersion
     *            The model version to be read from model service
     * @return the list of members in the provided EDT model.
     */
    public List<String> readEdtModelInformation(final String modelName, final String modelVersion) {
        final List<String> edtmodel = new ArrayList<String>();
        try {
            final DirectModelAccess directModelAccess = modelService.getDirectAccess();
            // TODO : Check if version is required or not.
            final ModelInfo modelInfo = new ModelInfo(OSS_EDT, FM, modelName, modelVersion);
            final EnumDataTypeDefinition eventTypeDef = directModelAccess.getAsJavaTree(modelInfo, EnumDataTypeDefinition.class);
            final Iterator<EnumDataTypeMember> eventAttrs = eventTypeDef.getMember().iterator();
            while (eventAttrs.hasNext()) {
                final EnumDataTypeMember enumDataTypeMember = eventAttrs.next();
                edtmodel.add(enumDataTypeMember.getName());
            }
            LOGGER.info("The values: {} retrieved from ModelService for modelName: {}", edtmodel, modelName);
        } catch (final Exception exception) {
            LOGGER.error("Exception in accessing ModelService : ", exception);
        }
        return edtmodel;
    }

}
