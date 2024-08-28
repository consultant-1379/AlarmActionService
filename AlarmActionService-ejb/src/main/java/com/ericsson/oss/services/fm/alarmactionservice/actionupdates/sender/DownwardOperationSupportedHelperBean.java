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

package com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.DOWNARD_ACK_SUPPORTED_NODETYPES;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.VERSION;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.fm.capability.util.ModelCapabilities;
import com.ericsson.oss.fm.capability.util.constants.ModelCapabilitiesConstants;
import com.ericsson.oss.services.fm.alarmactionservice.util.ModelServiceHelper;

/**
 * Bean responsible for reading EDT DownwardAckSupportedNodeTypes and provide information if downward operation is supported for a particular node
 * type.
 */
@ApplicationScoped
public class DownwardOperationSupportedHelperBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownwardOperationSupportedHelperBean.class);

    private List<String> downwardAckSupportedNodeTypes;

    @Inject
    private ModelServiceHelper modelServiceHelper;

    @Inject
    private ModelCapabilities modelCapabilities;

    /**
     * Reads the EDT model EDT DownwardAckSupportedNodeTypes at timeout which contains the downward ack supported node types.
     */
    public void readDownwardSupportedNodeTypes() {
        downwardAckSupportedNodeTypes = modelServiceHelper.readEdtModelInformation(DOWNARD_ACK_SUPPORTED_NODETYPES, VERSION);
    }

    /**
     * Checks if downward ack supports or not by reading the corresponding models.
     * @param targetType
     *            The target type of the node for which downward ack support needs to be checked.
     * @return true of the targetType supplied is available in list of supported node types.
     */
    public boolean isDownwardOperationSupported(final String targetType, final String operationType) {
        // TODO check on DownwardAckSupportedNodeTypes model will be deprecated as soon as capability support models will be available for CPP.
        LOGGER.debug("isDownwardOperationSupported targetType:{} and operationType:{} ", targetType, operationType);
        if (ModelCapabilitiesConstants.ALARM_UNACK_CAPABILITY.equals(operationType)
                || ModelCapabilitiesConstants.ALARM_ACK_CAPABILITY.equals(operationType)) {
            if (downwardAckSupportedNodeTypes == null) {
                LOGGER.info("Reading the downward ack supported node types from model service as list is empty!!");
                readDownwardSupportedNodeTypes();
            }
            return downwardAckSupportedNodeTypes.contains(targetType)
                    || modelCapabilities.isAlarmOperationSupportedByTarget(targetType, operationType);
        } else {
            return modelCapabilities.isAlarmOperationSupportedByTarget(targetType, operationType);
        }
    }
}
