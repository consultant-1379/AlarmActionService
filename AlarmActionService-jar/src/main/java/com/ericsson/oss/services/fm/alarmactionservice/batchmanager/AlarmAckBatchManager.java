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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible batching the alarm poIds for acknowledgement.
 */
public class AlarmAckBatchManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmAckBatchManager.class);

    /**
     * Method is responsible for splitting the alarms into batches and sends for acknowledgment.<br>
     * Number of bathes equal to the 80% of the cycle time/ estimatedTimeForActions [expected time for acknowledgment of each batch which is 4secs].
     *
     * @param poIds
     *            - Event PoIds of alarms/Events eligible for acknowledgement.
     * @param batchingParameters
     *            The parameters {@link BatchingParameters} used in batching the poIds to be acknowledged.
     * @return The list of poIds in batches.
     */
    public LinkedList<List<Long>> getpoIdBatches(final List<Long> poIds, final BatchingParameters batchingParameters) {
        final int checkFrequency = batchingParameters.getCheckFrequency();
        final int fixedBatchSize = batchingParameters.getThresholdEventBatchSize();
        final int numberOfAlarmsForCurrentCycle = checkFrequency * 60 / batchingParameters.getBatchTimerTimeout() * fixedBatchSize
                * batchingParameters.getAckCycleTimeUtilization() / 100;
        final List<Long> poIdsForCurrentCycle = new ArrayList<Long>();
        poIdsForCurrentCycle.addAll(poIds.subList(0, Math.min(numberOfAlarmsForCurrentCycle, poIds.size())));
        LOGGER.info("Eligible number of PoIds for the current cycle are {}  ", numberOfAlarmsForCurrentCycle);
        return getPoIdBatches(poIdsForCurrentCycle, fixedBatchSize);
    }

    private LinkedList<List<Long>> getPoIdBatches(final List<Long> poIdsForCurrentCycle, final int fixedBatchSize) {
        final LinkedList<List<Long>> poIdSubBatches = new LinkedList<List<Long>>();
        if (!poIdsForCurrentCycle.isEmpty()) {
            for (int counter = 0; counter < poIdsForCurrentCycle.size(); counter += fixedBatchSize) {
                poIdSubBatches.add(poIdsForCurrentCycle.subList(counter, counter + Math.min(fixedBatchSize, poIdsForCurrentCycle.size() - counter)));
            }
        }
        return poIdSubBatches;
    }
}
