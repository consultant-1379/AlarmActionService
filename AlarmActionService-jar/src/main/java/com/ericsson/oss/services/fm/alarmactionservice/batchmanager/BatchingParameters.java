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

package com.ericsson.oss.services.fm.alarmactionservice.batchmanager;

/**
 * Utility class for parameters used in batching the alarms that needs to be acknowledged.
 *
 */
public class BatchingParameters {

    int checkFrequency;
    int thresholdEventBatchSize;
    int batchTimerTimeout;
    int ackCycleTimeUtilization;

    public int getCheckFrequency() {
        return checkFrequency;
    }

    public void setCheckFrequency(final int checkFrequency) {
        this.checkFrequency = checkFrequency;
    }

    public int getBatchTimerTimeout() {
        return batchTimerTimeout;
    }

    public int getThresholdEventBatchSize() {
        return thresholdEventBatchSize;
    }

    public void setThresholdEventBatchSize(final int thresholdEventBatchSize) {
        this.thresholdEventBatchSize = thresholdEventBatchSize;
    }

    public void setBatchTimerTimeout(final int batchTimerTimeout) {
        this.batchTimerTimeout = batchTimerTimeout;
    }

    public int getAckCycleTimeUtilization() {
        return ackCycleTimeUtilization;
    }

    public void setAckCycleTimeUtilization(final int ackCycleTimeUtilization) {
        this.ackCycleTimeUtilization = ackCycleTimeUtilization;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BatchigParameters [checkFrequency=").append(checkFrequency).append(", fixedBatchSize=").append(thresholdEventBatchSize)
                .append(", batchTimerTimeout=").append(batchTimerTimeout).append(", ackCycleTimeUtilization=").append(ackCycleTimeUtilization)
                .append("]");
        return builder.toString();
    }

}
