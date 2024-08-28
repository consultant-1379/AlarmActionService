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
 *---------------------------------------------------------------------------- */

package com.ericsson.oss.services.fm.alarmactionservice.batchmanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class AlarmAckBatchManagerTest {

    @InjectMocks
    private AlarmAckBatchManager alarmAckBatchManager;

    @Test
    public void tesGetPoIdBatchesForDelayedAck() {
        final List<Long> poIds = new ArrayList<Long>();
        for (long count = 0; count < 9000; count++) {
            poIds.add(count);
        }
        final BatchingParameters batchingParameters = createBatchingParameters(4, 80);
        final LinkedList<List<Long>> poIdBatchesForCurrentCycle = alarmAckBatchManager.getpoIdBatches(poIds, batchingParameters);
        int totalSize = 0;
        for (final List<Long> poIdBatch : poIdBatchesForCurrentCycle) {
            totalSize = totalSize + poIdBatch.size();
        }
        Assert.assertEquals(8400, totalSize);
    }

    @Test
    public void tesGetPoIdBatchesForThresholdAck() {
        final List<Long> poIds = new ArrayList<Long>();
        for (long count = 0; count < 9000; count++) {
            poIds.add(count);
        }
        final BatchingParameters batchingParameters = createBatchingParameters(4, 90);
        batchingParameters.setAckCycleTimeUtilization(95);
        final LinkedList<List<Long>> poIdBatchesForCurrentCycle = alarmAckBatchManager.getpoIdBatches(poIds, batchingParameters);
        int totalSize = 0;
        for (final List<Long> poIdBatch : poIdBatchesForCurrentCycle) {
            totalSize = totalSize + poIdBatch.size();
        }
        Assert.assertEquals(9000, totalSize);
    }

    private BatchingParameters createBatchingParameters(final int checkFrequency, final int timeUtilizationFactor) {
        final BatchingParameters batchingParameters = new BatchingParameters();
        // Setting all default values
        batchingParameters.setCheckFrequency(checkFrequency);
        batchingParameters.setAckCycleTimeUtilization(timeUtilizationFactor);
        batchingParameters.setBatchTimerTimeout(4);
        batchingParameters.setThresholdEventBatchSize(175);
        return batchingParameters;
    }
}
