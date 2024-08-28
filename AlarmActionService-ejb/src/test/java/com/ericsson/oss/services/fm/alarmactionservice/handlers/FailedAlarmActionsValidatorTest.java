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

package com.ericsson.oss.services.fm.alarmactionservice.handlers;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED_ACKNOWLEDGED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class FailedAlarmActionsValidatorTest {

    @InjectMocks
    private FailedAlarmActionsValidator failedAlarmActionsValidator;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private DataBucket dataBucket;

    @Mock
    private PersistenceObject persistenceObject;

    @Test
    public void readOpenAlarms_test() {
        when(dpsUtil.getLiveBucket()).thenReturn(dataBucket);
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(12345L);
        final List<PersistenceObject> processedAlarmEvents = new ArrayList<PersistenceObject>();
        processedAlarmEvents.add(persistenceObject);
        when(dataBucket.findPosByIds(poIds)).thenReturn(processedAlarmEvents);
        when(persistenceObject.getPoId()).thenReturn(12345L);
        when(persistenceObject.getAllAttributes()).thenReturn(new HashMap<String, Object>());
        assertNotNull(failedAlarmActionsValidator.readOpenAlarms(poIds));

    }

    @Test
    public void validateFailedActionsWithDB_test() {
        when(dpsUtil.getLiveBucket()).thenReturn(dataBucket);
        final Collection<AlarmActionInformation> failedActionsInCache = new ArrayList<AlarmActionInformation>();
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setAlarmAction("ACK");
        alarmActionInformation.setActionUpdatedInDb(true);
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setOperatorName("Operator");
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put(ALARMSTATE, CLEARED_ACKNOWLEDGED);
        final Date lastUpdted = new Date();
        alarmAttributes.put(LASTUPDATED, lastUpdted);
        alarmActionInformation.setAlarmAttributes(alarmAttributes);
        failedActionsInCache.add(alarmActionInformation);
        final AlarmActionInformation alarmActionInformation1 = new AlarmActionInformation();
        alarmActionInformation1.setPoId(1234L);
        alarmActionInformation1.setActionUpdatedInDb(true);
        alarmActionInformation1.setAlarmAttributes(alarmAttributes);
        failedActionsInCache.add(alarmActionInformation1);
        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        when(persistenceObject.getAttribute(LASTUPDATED)).thenReturn(lastUpdted);
        when(dataBucket.findPoById(12345L)).thenReturn(persistenceObject);
        assertNotNull(failedAlarmActionsValidator.validateFailedActionsWithDB(failedActionsInCache));
    }

}
