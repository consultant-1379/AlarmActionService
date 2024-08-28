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

package com.ericsson.oss.services.alarm.action.service.integration.util;

import java.util.concurrent.CountDownLatch;

import javax.ejb.Singleton;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.eventbus.model.ModeledEvent;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;
import com.ericsson.oss.services.fm.service.model.FmMediationAckRequest;
import com.ericsson.oss.services.fm.service.model.FmMediationClearRequest;

@Singleton
public class MediationTaskRequestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediationTaskRequestListener.class);

    public CountDownLatch LATCH;

    public void receiveMediationTaskRequest(@Observes @Modeled(eventUrn="//global/MediationTaskRequest/1.0.0") final ModeledEvent taskRequestEvent) {
        final MediationTaskRequest mtr = (MediationTaskRequest) taskRequestEvent.extractAs(MediationTaskRequest.class);
        if (mtr instanceof FmMediationAckRequest) {
            final FmMediationAckRequest ackRequest = (FmMediationAckRequest) mtr;
            LOGGER.info("Received mediation ack request with alarm ids: {} and fdn: {}", ackRequest.getAlarmIds(), ackRequest.getNodeAddress());
        }
        if (mtr instanceof FmMediationClearRequest) {
            final FmMediationClearRequest clearRequest = (FmMediationClearRequest) mtr;
            LOGGER.info("Received mediation clear request with alarm ids: {} and fdn: {}", clearRequest.getAlarmIds(), clearRequest.getNodeAddress());
        }
        LATCH.countDown();
    }

    public MediationTaskRequestListener() {
        LATCH = new CountDownLatch(1);
    }

    public long getCountInLatch() {
        return LATCH.getCount();
    }

    public void resetLatch(final int count) {
        LATCH = null;
        LATCH = new CountDownLatch(count);
    }
}
