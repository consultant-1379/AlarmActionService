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

package com.ericsson.oss.services.fm.alarmactionservice.rest.client;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HOST_PARAM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_HISTORY_SERVICE_HOST;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_HISTORY_SERVICE_URL;

import java.util.List;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import  com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

/**
 * Fires a rest request for sending Failed alarms only to history,which were not send to alarm action updates consumers(JMS Queues) and upon those
 * alarm action updates some others action updates.
 */
public class FailedAlarmActionUpdatesRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedAlarmActionUpdatesRestClient.class);

    public void sendFailedAlarmsToHistory(final List<ProcessedAlarmEvent> processedAlarmEvents) {
        try {
            LOGGER.debug("Received failed alarm count:{}", processedAlarmEvents.size());
            final ClientRequest request = new ClientRequest(ALARM_HISTORY_SERVICE_URL);
            request.header(HOST_PARAM, ALARM_HISTORY_SERVICE_HOST);
            request.accept(MediaType.APPLICATION_JSON);
            final String inputJsonString = buildAlarmsAsString(processedAlarmEvents);
            request.body(MediaType.APPLICATION_JSON, inputJsonString);
            final ClientResponse<String> response = request.post(String.class);
            if (response == null || response.getStatus() != 200) {
                final StringBuilder exceptionMessage =
                        new StringBuilder("Failed to store alarm action updates in history and status");
                if (response != null && response.getStatus() != 200) {
                    exceptionMessage.append(response.getStatus()).toString();
                }
                throw new RuntimeException(exceptionMessage.toString());
            }
        } catch (final Exception exception) {
            LOGGER.warn("Exception occurred while storing failed alarm action updates in history and Exception Details: ", exception);
            throw new RuntimeException(exception);
        }
    }

    private String buildAlarmsAsString(final List<ProcessedAlarmEvent> processedAlarmEvents) throws Exception {
        String alarmsAsString = "";
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        alarmsAsString = objectMapper.writeValueAsString(processedAlarmEvents);
        return alarmsAsString;
    }

}