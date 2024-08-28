/*------------------------------------------------------------------------------
 * ********************************************************************************
 * COPYRIGHT Ericsson 2012**The copyright to the computer program(s)herein is the property of
 * Ericsson Inc.The programs may be used and/or copied only with written
 * permission from Ericsson Inc.or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s)have been supplied.
 * *******************************************************************************
 * ----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR_CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR_MESSAGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NO_ALARMS_UNDERFDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.RECORDTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.PersistenceObjectBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.RestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;
import com.ericsson.oss.services.alarm.action.service.instrumentation.AASInstrumentedBean;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.DownwardOperationRequestSender;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.DownwardOperationSupportedHelperBean;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

@RunWith(MockitoJUnitRunner.class)
public class ActionClearerTest {

    @InjectMocks
    private ActionClearer actionClearer;

    @Mock
    private DataPersistenceService service;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject managedObject;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private AASInstrumentedBean aasInstrumentedBean;

    @Mock
    private Query<TypeRestrictionBuilder> typeQuery;

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private RestrictionBuilder restrictionBuilder;

    @Mock
    private TypeRestrictionBuilder typeRestrictionBuilder;

    @Mock
    private PersistenceObject persistenceObject;

    @Mock
    private PersistenceObjectBuilder persistenceObjectBuilder;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private SystemRecorder systemRecorder;

    @Mock
    private EventSender<ProcessedAlarmEvent> modeledEventSender;

    @Mock
    private AlarmActionUtils alarmActionUtils;

    @Mock
    private EventSender<MediationTaskRequest> mediationTaskSender;

    @Mock
    private DownwardOperationSupportedHelperBean downwardOperationSupportedHelperBean;

    @Mock
    private DownwardOperationRequestSender opRequestSender;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    String fdn = "MeContext=10,ManagedElement=10,ENodeBFunction=10";
    String comment = "comment";
    String alarmState = "alarmState";
    String operatorName = "AASOperator";
    AlarmActionData alarmActionData = new AlarmActionData();
    List<Long> alarmIdList = new ArrayList<Long>();
    List<Long> clearList = new ArrayList<Long>();

    Map<String, List<Long>> clearFdnList = new HashMap<String, List<Long>>();
    // Map<String, String> response =mock(HashMap.class);
    List<AlarmActionResponse> response = mock(ArrayList.class);
    Iterator<Object> poListIterator = mock(Iterator.class);
    Collection<PersistenceObject> alramDescList = mock(Collection.class);
    Iterator<PersistenceObject> alarmDescIterator = mock(Iterator.class);
    String[] fdnArray = { "MeContext", "ManagedElement", "ENodeBFunction" };

    public void setUp() {

        alarmIdList.add(1234567L);
        alarmActionData.setObjectOfReference(null);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(operatorName);
        alarmActionData.setComment(comment);
        alarmActionData.setAction(AlarmAction.CLEAR);

        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findPoById(Matchers.anyLong())).thenReturn(persistenceObject);

    }

    public void setUpPerformClearWithOnlyAlarmIDsWithAlarms() {
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) 1234567);
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
    }

    public void setUpPerformClearWithOnlyAlarmIDsWithErrors() {
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) 1234567);
        when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");
    }

    public void setUpAlarmDescList() {
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(alramDescList);
        when(alramDescList.iterator()).thenReturn(alarmDescIterator);
        when(alarmDescIterator.hasNext()).thenReturn(true, true, false);
        when(alarmDescIterator.next()).thenReturn(persistenceObject);
    }

    public void setUpPerformClearWithFDNAndNumbers() {
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        // String[] fdnArray ={"MeContext","ManagedElement","ENodeBFunction"};
        setUpPerformClearWithOnlyAlarmIDsWithAlarms();
    }

    public void setUpPerformClearWithFDNAndNumbersForErrors() {
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        setUpPerformClearWithOnlyAlarmIDsWithErrors();
    }

    public void setUpPerformClearWithOnlyFDN() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        alarmIdList.remove(0);
        alarmActionData.setAlarmIds(alarmIdList);
        setUpPerformClearWithOnlyAlarmIDsWithAlarms();
    }

    public void setUpPerformClearWithOnlyFDNForErrors() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        alarmIdList.remove(0);
        alarmActionData.setAlarmIds(alarmIdList);
        setUpPerformClearWithOnlyAlarmIDsWithErrors();
    }

    @Test
    public void testPerformClearWithOnlyAlarmIDsWithNoAlarms() {
        setUp();

        actionClearer.performClear(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformClearWithFDNAndNumbersPoNull() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(queryExecutor.execute(typeQuery)).thenReturn(null);

        actionClearer.performClear(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformClearWithFDNAndNumbers() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("fake");
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionClearer.performClear(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformClearWithFDNAndNumbersWithAlarmsClearedUnack() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), ALREADY_CLEAR);
    }

    @Test
    public void testPerformClearWithFDNAndNumbersWithErrors() {
        setUp();
        setUpPerformClearWithFDNAndNumbersForErrors();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals(ERROR_CLEAR, responses.get(0).getResponse());
    }

    @Test
    public void testPerformClearWithFDNAndNumbersWithAlarmsAckAlarmDescListNull() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(null);
        final Collection<PersistenceObject> alarmAssociaiton = new ArrayList<PersistenceObject>();
        alarmAssociaiton.add(persistenceObject);
        when(persistenceObject.getAssociations(AlarmActionConstants.ALARMDESCRIPTION)).thenReturn(alarmAssociaiton);
        when(liveBucket.findPoById(Matchers.anyLong())).thenReturn(persistenceObject);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(1);
        alarmMap.put("visibility", true);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    @Test
    public void testPerformClearWithFDNAndNumbersWithAlarmsAckAlarmDescListNotNull() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        setUpAlarmDescList();
        final Map<String, Object> alarmMap = new HashMap<String, Object>(1);
        alarmMap.put("visibility", true);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    @Test
    public void testPerformClearWithFDNAndNumbersWithAlarmsUnackAlarmDescListNull() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(null);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(1);
        alarmMap.put("visibility", true);

        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    @Test
    public void testPerformClearWithFDNAndNumbersWithAlarmsUnackAlarmDescListNotNull() {
        setUp();
        setUpPerformClearWithFDNAndNumbers();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        setUpAlarmDescList();
        final Map<String, Object> alarmMap = new HashMap<String, Object>(1);
        alarmMap.put("visibility", true);

        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    // *****************PerformClearWithOnlyFDN*****************//

    @Test
    public void testPerformClearWithOnlyFDNPoNull() {
        setUpPerformClearWithOnlyFDN();
        when(queryExecutor.execute(typeQuery)).thenReturn(null);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals(NO_ALARMS_UNDERFDN, responses.get(0).getResponse());
    }

    @Test
    public void testPerformClearWithOnlyFDN() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("fake");
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionClearer.performClear(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsClearedUnack() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        when(alarmActionsCacheManager.get((AlarmActionInformation) Matchers.anyObject())).thenReturn(null);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals(ALREADY_CLEAR, responses.get(0).getResponse());
    }

    @Test
    public void testPerformClearWithOnlyFDNWithErrors() {
        setUpPerformClearWithOnlyFDNForErrors();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), ERROR_CLEAR);
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsAckAlarmDescListNull() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(null);
        final Collection<PersistenceObject> alarmAssociaiton = new ArrayList<PersistenceObject>();
        alarmAssociaiton.add(persistenceObject);
        when(persistenceObject.getAssociations(AlarmActionConstants.ALARMDESCRIPTION)).thenReturn(alarmAssociaiton);
        when(liveBucket.findPoById(Matchers.anyLong())).thenReturn(persistenceObject);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);

        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsAckAlarmDescListNotNull() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        setUpAlarmDescList();
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsAckAlarmDescListNotNullAndMTR() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("fdn")).thenReturn("neFdn");

        alarmIdList.add(1234567L);
        setUpAlarmDescList();
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "alarmId:1234567L#sourceType:ECM");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsUnackAlarmDescListNull() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(null);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsUnackAlarmDescListNotNull() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        setUpAlarmDescList();
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    @Test
    public void testPerformClearWithOnlyFDNWithAlarmsUnackAlarmDescListNotNullAndMTR() {
        setUpPerformClearWithOnlyFDN();
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        setUpAlarmDescList();
        when(persistenceObject.getAttribute("fdn")).thenReturn("neFdn");
        when(downwardOperationSupportedHelperBean.isDownwardOperationSupported(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "alarmId:1234567L#sourceType:ECM");
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.performClear(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    @Test
    public void testperformClearforMultipleFdns() {

        clearList.add(123L);
        clearList.add(456L);
        clearList.add(678L);
        clearFdnList.put(fdn, clearList);
        alarmActionData.setClearFdnList(clearFdnList);
        // alarmActionData.setClearFdnList(clearFdnList);
        alarmActionData.setOperatorName(operatorName);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) 1234567);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");

        actionClearer.performClearforMultipleFdns(alarmActionData, new ArrayList<AlarmActionInformation>());

    }

    @Test
    public void testperformClearforMultipleFdnsWithNoErrors() {

        clearList.add(123L);
        clearList.add(456L);
        clearList.add(678L);
        clearFdnList.put(fdn, clearList);
        alarmActionData.setClearFdnList(clearFdnList);
        // alarmActionData.setClearFdnList(clearFdnList);
        alarmActionData.setOperatorName(operatorName);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) 1234567);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("FakeError");
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        actionClearer.performClearforMultipleFdns(alarmActionData, new ArrayList<AlarmActionInformation>());

    }

    @Test
    public void testClearWithErrorMessage() {

        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(123L);
        poIds.add(132L);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findPosByIds(poIds)).thenReturn(persistenceObjects);
        when(persistenceObject.getPoId()).thenReturn(123L);
        when(persistenceObject.getAttribute(RECORDTYPE)).thenReturn(ERROR_MESSAGE);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        actionClearer.clear(operatorName, poIds, responses);
        assertEquals(responses.get(0).getResponse(), ERROR_CLEAR);
        assertEquals(responses.get(0).getEventPoId(), "123");
        assertEquals(responses.get(1).getResponse(), ALARM_NOTFOUND);
        assertEquals(responses.get(1).getEventPoId(), "132");
    }

    @Test
    public void testClear() {

        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(123L);
        poIds.add(132L);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findPosByIds(poIds)).thenReturn(persistenceObjects);
        when(persistenceObject.getPoId()).thenReturn(123L);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(new HashMap<String, Object>());
        when(persistenceObject.getAttribute(ALARMSTATE)).thenReturn(UNACK);
        when(persistenceObject.getAttribute(ALARMNUMBER)).thenReturn(12345L);
        when(persistenceObject.getAttribute(OBJECTOFREFERENCE)).thenReturn(OBJECTOFREFERENCE);
        when(persistenceObject.getAttribute(RECORDTYPE)).thenReturn(ALARM);
        // when(persistenceObject.getAttribute(RECORDTYPE)).thenReturn(ERROR_MESSAGE);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionClearer.clear(operatorName, poIds, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(responses.get(0).getResponse(), SUCCESS);
    }

    public AlarmActionInformation prepareAlarmActionInformation() {
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setAlarmAction("CLEAR");
        alarmActionInformation.setOperatorName("Operator1");
        return alarmActionInformation;
    }
}
