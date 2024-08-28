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

package com.ericsson.oss.services.timerservice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.RestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.Projection;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.ProjectionBuilder;
import com.ericsson.oss.services.alarm.action.service.api.AlarmActionService;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.Constants;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DataPersistenceServiceProxy;
import com.ericsson.oss.services.fm.actionservice.alarmthreshold.util.DpsAccessManager;

@RunWith(MockitoJUnitRunner.class)
public class DpsAccessManagerTest {

    @InjectMocks
    private final DpsAccessManager dpsAccessManager = new DpsAccessManager();

    @Mock
    private DataPersistenceService dataPersistenceService;

    @Mock
    private DataPersistenceServiceProxy dataPersistenceServiceProxy;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private Query<TypeRestrictionBuilder> typeQuery;

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private Restriction restriction;

    @Mock
    private RestrictionBuilder restrictionBuilder;

    @Mock
    private Iterator<Object> poListIterator;

    @Mock
    private TypeRestrictionBuilder typeRestrictionBuilder;

    @Mock
    private AlarmActionData alarmActionData;

    @Mock
    AlarmActionService alarmActionService;

    @Mock
    AlarmActionResponse alarmActionResponse;

    @Mock
    private PersistenceObject poObject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(dataPersistenceServiceProxy.getService()).thenReturn(dataPersistenceService);
        when(dataPersistenceServiceProxy.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(dataPersistenceServiceProxy.getQueryBuilder()).thenReturn(queryBuilder);
    }

    @Test
    public void testGetActiveAlarmCount() {
        final List<Object> poIds = new ArrayList<Object>();
        poIds.add(123L);
        poIds.add(345L);

        when(queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM)).thenReturn(typeQuery);
        final Projection poIdProjection = ProjectionBuilder.field(ObjectField.PO_ID);
        when(queryExecutor.executeProjection(typeQuery, poIdProjection)).thenReturn(poIds);
        assertNotNull(dpsAccessManager.getActiveAlarmCount());
    }

    @Test
    public void testGetInternalAlarm() {
        when(queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM)).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(typeRestrictionBuilder.equalTo(Constants.SPECIFIC_PROBLEM, Constants.THRESHOLD_LIMIT_SPECIFIC_PROBLEM)).thenReturn(restriction);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        Mockito.when(poListIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(poListIterator.next()).thenReturn(poObject);
        assertTrue(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification());
    }

    @Test
    public void testGetInternalAlarmNotFound() {
        when(queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM)).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(typeRestrictionBuilder.equalTo(Constants.SPECIFIC_PROBLEM, Constants.THRESHOLD_LIMIT_SPECIFIC_PROBLEM)).thenReturn(restriction);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        Mockito.when(poListIterator.hasNext()).thenReturn(false);
        assertFalse(dpsAccessManager.isInternalAlarmRaisedForAlarmThresholdNotification());
    }

    @Test
    public void testAcknowledgeClearedAlarms() {
        final List<Object> poIds = new ArrayList<Object>();
        poIds.add(123L);
        poIds.add(345L);
        when(queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM)).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(typeRestrictionBuilder.equalTo(Constants.PRESENT_SEVERITY, Constants.CLEAR)).thenReturn(restriction);
        final Projection poIdProjection = ProjectionBuilder.field(ObjectField.PO_ID);
        when(queryExecutor.executeProjection(typeQuery, poIdProjection)).thenReturn(poIds);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        Mockito.when(poListIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(poListIterator.next()).thenReturn(poObject);

        dpsAccessManager.acknowledgeClearedAlarms(Arrays.asList(123L, 345L));
        when(alarmActionService.alarmActionUpdate(alarmActionData, true)).thenReturn(new ArrayList<AlarmActionResponse>());

        dpsAccessManager.acknowledgeClearedAlarms(new ArrayList<Long>());
        when(alarmActionService.alarmActionUpdate(alarmActionData, true)).thenReturn(new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testGetPoIds() {
        when(queryBuilder.createTypeQuery(Constants.OSS_FM, Constants.OPEN_ALARM)).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);

        dpsAccessManager.getPoIds();
        verify(dataPersistenceServiceProxy, times(1)).getLiveBucket();
        verify(dataPersistenceServiceProxy, times(1)).getQueryBuilder();
        verify(liveBucket, times(1)).getQueryExecutor();
    }
}
