/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.actionservice.alarmthreshold.util;

import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_THRESHOLD_FOR_FORCE_ACK;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ALARM_THRESHOLD_FOR_NOTIFICATION;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the change in configuration parameter values which are used in alarm action service.
 */
@ApplicationScoped
public class ConfigurationParameterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationParameterUtil.class);

    public void updateParameter(final String name, final int value) {
        final String configParameter = "/opt/ericsson/PlatformIntegrationBridge/etc/config.py update"
                + " --app_server_address=$(/bin/hostname):8080 --name=" + name + " --value=" + value;
        Process configParamterProcess = null;
        try {
            final String[] command = { "/bin/bash", "-c" , configParameter };
            configParamterProcess = Runtime.getRuntime().exec(command);
            configParamterProcess.waitFor();
            LOGGER.info("Updating the value of the parameter {} to {} as the range for Small ENM is "
                    + " {}(5000-60000) and {}(1000-40000)", name, value, ALARM_THRESHOLD_FOR_FORCE_ACK, ALARM_THRESHOLD_FOR_NOTIFICATION);
        } catch (final Exception e) {
            LOGGER.error("Error while getting the number of instances : ", e);
        } finally {
            try {
                if(configParamterProcess != null){
                    configParamterProcess.destroy();
                }
            } catch (final Exception exp) {
                LOGGER.error("Error while closing resources : ", exp);
            }
        }
    }

    public boolean isSmallEnm() {
        String instancesCheck = "/bin/cat /ericsson/tor/data/global.properties | grep 'fmhistory='"
                + " | cut -d'=' -f2 | tr ',' '\n' | wc -l";
        String deploymentTypeCheck = "/bin/cat /ericsson/tor/data/global.properties | grep 'enm_deployment_type='"
                + " | cut -d'=' -f2";
        String deploymentType = executeCommand(deploymentTypeCheck);
        int noOfInstances = Integer.parseInt(executeCommand(instancesCheck));
        LOGGER.info("noOfInstances = {}", noOfInstances);
        LOGGER.info("deploymentType = {}", deploymentType.equalsIgnoreCase("OSIENM_transport_only"));
        return (noOfInstances <= 1 || deploymentType.equalsIgnoreCase("OSIENM_transport_only"));
    }

    public int getNumberOfReplicas() {
        String replicasCheck = "echo $FMHISTORY_REPLICAS";
        int noOfReplicas = Integer.parseInt(executeCommand(replicasCheck));
        LOGGER.info("noOfReplicas = {}", noOfReplicas);
        return noOfReplicas;
    }

    private String executeCommand(String input) {
        BufferedReader bufferedReaderForInstances = null;
        Process instancesCheckProcess = null;
        String output = "0";
        try {
            final String[] command = { "/bin/bash", "-c" , input };
            instancesCheckProcess = Runtime.getRuntime().exec(command);
            instancesCheckProcess.waitFor();
            bufferedReaderForInstances = new BufferedReader(new InputStreamReader(instancesCheckProcess.getInputStream()));
            final String readOutput = bufferedReaderForInstances.readLine();
            LOGGER.info("the string read from the console is {}", readOutput);
            if (readOutput != null && !readOutput.isEmpty()) {
                 output = readOutput;
            }
        } catch (final Exception e) {
            LOGGER.error("Error while getting the number of instances : ", e);
        } finally {
            try {
                if(bufferedReaderForInstances != null){
                    bufferedReaderForInstances.close();
                }
                if(instancesCheckProcess != null){
                    instancesCheckProcess.destroy();
                }
            } catch (final Exception exp) {
                LOGGER.error("Error while closing resources : ", exp);
            }
        }
        return output;
    }

}