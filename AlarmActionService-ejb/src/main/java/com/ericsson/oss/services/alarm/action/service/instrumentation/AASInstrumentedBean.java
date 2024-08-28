/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.alarm.action.service.instrumentation;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.instrument.annotation.InstrumentedBean;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.CollectionType;
import com.ericsson.oss.itpf.sdk.instrument.annotation.MonitoredAttribute.Visibility;
import com.ericsson.oss.itpf.sdk.instrument.annotation.Profiled;

/**
 * Bean responsible for managing the data about the number of ack or unack or comment or operations performed.
 */
@InstrumentedBean(displayName = "Alarm Action Metrics", description = "Alarm Action Records")
@ApplicationScoped
@Profiled
public class AASInstrumentedBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AASInstrumentedBean.class);

    private int ackAlarmCount;

    private int unAckAlarmCount;

    private int manualClearAlarmCount;

    private int failedAckAlarmCount;

    private int failedUnAckAlarmCount;

    private int failedClearAlarmCount;

    public AASInstrumentedBean() {
        ackAlarmCount = 0;
        unAckAlarmCount = 0;
        manualClearAlarmCount = 0;
        failedAckAlarmCount = 0;
        failedUnAckAlarmCount = 0;
        failedClearAlarmCount = 0;
    }

    @MonitoredAttribute(displayName = "Number of Alarms Acknowledged", visibility = Visibility.ALL, collectionType = CollectionType.TRENDSUP)
    public int getAckAlarmCount() {
        return this.ackAlarmCount;
    }

    @MonitoredAttribute(displayName = "Number of Alarms Un-Acknowledged", visibility = Visibility.ALL, collectionType = CollectionType.TRENDSUP)
    public int getUnAckAlarmCount() {
        return this.unAckAlarmCount;
    }

    @MonitoredAttribute(displayName = "Number of Alarms Manually Cleared", visibility = Visibility.ALL, collectionType = CollectionType.TRENDSUP)
    public int getManualClearAlarmCount() {
        return this.manualClearAlarmCount;
    }

    @MonitoredAttribute(displayName = "Number of Failed Acknowledged Alarms", visibility = Visibility.ALL, collectionType = CollectionType.TRENDSUP)
    public int getFailedAckAlarmCount() {
        return this.failedAckAlarmCount;
    }

    @MonitoredAttribute(displayName = "Number of Failed Un-Acknowledged Alarms", visibility = Visibility.ALL,
            collectionType = CollectionType.TRENDSUP)
    public int getFailedUnAckAlarmCount() {
        return this.failedUnAckAlarmCount;
    }

    @MonitoredAttribute(displayName = "Number of Failed Manually Cleared Alarms", visibility = Visibility.ALL,
            collectionType = CollectionType.TRENDSUP)
    public int getFailedClearAlarmCount() {
        return this.failedClearAlarmCount;
    }

    public void increaseClearAlarmCount() {
        this.manualClearAlarmCount++;
        LOGGER.debug("Increased the ClearalarmCount: {}", this.manualClearAlarmCount);
    }

    public void increaseFailedClearAlarms() {
        this.failedClearAlarmCount++;
        LOGGER.debug("Increased the Clear failedAlarmCount: ", this.failedClearAlarmCount);
    }

    public void increasAckalarmCount() {
        this.ackAlarmCount++;
        LOGGER.debug("Increased the AckalarmCount: {}", this.ackAlarmCount);
    }

    public void increaseFailedAckAlarms() {
        this.failedAckAlarmCount++;
        LOGGER.debug("Increased the Ack failedAlarmCount: {}", this.failedAckAlarmCount);
    }

    public void increaseUnAckalarmCount() {
        this.unAckAlarmCount++;
        LOGGER.debug("Increased the UnAckalarmCount: {}", this.unAckAlarmCount);
    }

    public void increaseFailedUnAckAlarms() {
        this.failedUnAckAlarmCount++;
        LOGGER.debug(" Increased the Unack failedAlarmCount: {}", this.failedUnAckAlarmCount);
    }
}
