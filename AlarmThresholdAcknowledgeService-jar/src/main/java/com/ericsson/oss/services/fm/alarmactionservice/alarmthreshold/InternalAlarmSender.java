/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.alarmthreshold;

import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.ADDITIONAL_TEXT;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.APPLICATION_NAME;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.THRESHOLD_LIMIT_EVENT_TYPE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.FDN;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.INTERNAL_ALARM_FDN;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.NOTIF_TYPE_ALARM;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.THRESHOLD_LIMIT_PROBABLE_CAUSE;
import static com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants.THRESHOLD_LIMIT_SPECIFIC_PROBLEM;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.mediation.translator.model.EventNotification;
import com.ericsson.oss.mediation.translator.model.EventNotificationBatch;
import com.ericsson.oss.services.fm.service.util.EventNotificationConverter;

/**
 * Utility class for sending internal alarm when open alarm count is increased or decreased threshold limit.
 */
@Singleton
public class InternalAlarmSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalAlarmSender.class);

    @Inject
    @Modeled
    private EventSender<EventNotificationBatch> eventSender;

    public void sendInternalAlarmNotification(final String severity, final long activeAlarmInboundThreshold) {
        final EventNotification eventNotification = new EventNotification();
        final String problemText = "Open alarm count in the database is more than " + activeAlarmInboundThreshold;
        eventNotification.setRecordType(NOTIF_TYPE_ALARM);
        eventNotification.setManagedObjectInstance(APPLICATION_NAME);
        eventNotification.setPerceivedSeverity(severity);
        eventNotification.setSpecificProblem(THRESHOLD_LIMIT_SPECIFIC_PROBLEM);
        eventNotification.setProbableCause(THRESHOLD_LIMIT_PROBABLE_CAUSE);
        eventNotification.setEventType(THRESHOLD_LIMIT_EVENT_TYPE);
        eventNotification.addAdditionalAttribute(FDN, INTERNAL_ALARM_FDN);
        eventNotification.addAdditionalAttribute(ADDITIONAL_TEXT, problemText);

        final List<EventNotification> notifList = new ArrayList<EventNotification>(1);
        notifList.add(eventNotification);
        eventSender.send(EventNotificationConverter.serializeObject(notifList));
        LOGGER.info("Sent internal alarm notification to APS for notifying Active alarm count is : {}", eventNotification);
    }
}