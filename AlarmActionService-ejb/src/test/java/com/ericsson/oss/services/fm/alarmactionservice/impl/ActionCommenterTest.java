
package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SUCCESS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionServiceSeverity;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

@RunWith(MockitoJUnitRunner.class)
public class ActionCommenterTest {

    @InjectMocks
    ActionCommenter actionCommenter;

    @Mock
    DataPersistenceService service;

    @Mock
    DataBucket liveBucket;

    @Mock
    PersistenceObject persistenceObject;

    @Mock
    ManagedObject managedObject;

    @Mock
    PersistenceObjectBuilder persistenceObjectBuilder;

    @Mock
    DpsUtil dpsUtil;

    @Mock
    QueryBuilder queryBuilder;

    @Mock
    Query<TypeRestrictionBuilder> typeQuery;

    @Mock
    TypeRestrictionBuilder typeRestrictionBuilder;

    @Mock
    QueryExecutor queryExecutor;

    @Mock
    RestrictionBuilder restrictionBuilder;

    @Mock
    SystemRecorder systemRecorder;

    @Mock
    public AlarmActionUtils alarmActionUtils;

    @Mock
    private EventSender<ProcessedAlarmEvent> modeledEventSender;

    @Mock
    private CommentHistoryRecorder commentHistoryRecorder;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    String fdn = "MeContext=41,ManagedElement=41,ENodeBFunction=41";
    String comment = "comment";
    String alarmState = "alarmState";
    String operatorName = "AASOperator";
    List<Long> alarmIdList = new ArrayList<Long>();
    List<AlarmActionResponse> response = mock(ArrayList.class);
    Map<String, String> responseEachAlarm = mock(HashMap.class);
    Map<String, Object> alarmDescriptionMap = new HashMap<String, Object>();
    Iterator<PersistenceObject> alarmDescIterator = mock(Iterator.class);
    Iterator<Object> poListIterator = mock(Iterator.class);
    AlarmActionData alarmActionData = new AlarmActionData();

    public void setUp() {
        alarmIdList.add(1234567L);
        alarmActionData.setObjectOfReference(null);
        alarmActionData.setAlarmIds(alarmIdList);
        alarmActionData.setOperatorName(operatorName);
        alarmActionData.setComment(comment);
        alarmActionData.setAction(AlarmAction.ACK);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findPoById(Matchers.anyLong())).thenReturn(persistenceObject);

    }

    public void setUpCreateDescPersistenceObject() {
        when(liveBucket.getPersistenceObjectBuilder()).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.namespace(AlarmActionConstants.FM)).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.type("AlarmDescription")).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.version("1.0.1")).thenReturn(persistenceObjectBuilder);
        alarmDescriptionMap.put("commentText", comment);
        when(persistenceObjectBuilder.addAttributes(alarmDescriptionMap)).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.create()).thenReturn(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(fdn);
    }

    public void setUpAlramDescList() {
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        final Collection<PersistenceObject> alarmDescList = mock(Collection.class);
        when(alarmDescList.size()).thenReturn(3);
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(alarmDescList);
        when(alarmDescList.iterator()).thenReturn(alarmDescIterator);
        when(alarmDescIterator.hasNext()).thenReturn(true, true, false);
        when(alarmDescIterator.next()).thenReturn(persistenceObject);
    }

    public void setUpPerformCommentWithFilters() {
        alarmIdList.remove(0);
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
    }

    public void setUpPerformCommentWithOnlyFDN() {
        alarmActionData.setObjectOfReference(fdn);
        alarmActionData.setAction(AlarmAction.ACK);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
    }

    @Test
    public void testPerformCommentWithOnlyAlarmIDsWithNoAlarms() {
        setUp();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("fake");

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOnlyAlarmIDsForOpenAlarms() {
        setUp();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOnlyAlarmIDsForOpenAlarms1() {
        setUp();
        final List<Long> alarmIdList1 = new ArrayList<Long>();
        alarmIdList1.add(1234567L);
        alarmIdList1.add(12567L);
        alarmActionData.setAlarmIds(alarmIdList1);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOORAndAlarmIdsWithoutOpenAlarms() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("fake");

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOORAndAlarmIdsWithWrongOOR() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOnlyAlarmIDsWithAlarms() {
        setUp();
        setUpCreateDescPersistenceObject();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    // @Test
    // public void testPerformCommentImproperfdn()
    // {
    // setUp();
    // alarmActionData.setfdn("fake,test,data");
    // when(liveBucket.findMoByFdn("fake,test,data")).thenReturn(managedObject);
    // actionCommenter.performComment(alarmActionData);
    // }

    @Test
    public void testPerformCommentWithFDNAndNumbersWithNoAlarms() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        when(persistenceObject.getType()).thenReturn("fake");
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFDNAndNumbersWithAlarms() {
        setUp();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();

        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getAttribute("eventPoId")).thenReturn(alarmIdList);
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(fdn);
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        setUpCreateDescPersistenceObject();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFDNAndNumberAlramDescListNotNull() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        setUpCreateDescPersistenceObject();
        setUpAlramDescList();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFDNAndNumbersWithAlarmsOorNotContainsFdn() {
        setUp();
        alarmActionData.setObjectOfReference(fdn);
        when(liveBucket.findMoByFdn(fdn)).thenReturn(managedObject);
        setUpCreateDescPersistenceObject();
        when(persistenceObject.getAttribute("alarmState")).thenReturn(alarmState);
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithNullPoIterator() {
        setUp();
        alarmActionData.setSpecificProblem("SpecificProblem");
        setUpPerformCommentWithFilters();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithSp() {
        setUp();
        alarmActionData.setSpecificProblem("SpecificProblem");
        setUpPerformCommentWithFilters();
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        setUpCreateDescPersistenceObject();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPc() {
        setUp();
        alarmActionData.setProbableCause("ProbableCause");
        setUpPerformCommentWithFilters();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithEt() {
        setUp();
        alarmActionData.setEventType("EventType");
        setUpPerformCommentWithFilters();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPsIndeterminate() {
        setUp();
        setUpPerformCommentWithFilters();
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.INDETERMINATE);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPsCleared() {
        setUp();
        setUpPerformCommentWithFilters();
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.CLEARED);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPsMajor() {
        setUp();
        setUpPerformCommentWithFilters();
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.MAJOR);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPsCritical() {
        setUp();
        setUpPerformCommentWithFilters();
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.CRITICAL);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPsMinor() {
        setUp();
        setUpPerformCommentWithFilters();
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.MINOR);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithFiltersWithPsWarning() {
        setUp();
        setUpPerformCommentWithFilters();
        alarmActionData.setPresentSeverity(AlarmActionServiceSeverity.WARNING);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOnlyFDNWithNullPoIterator() {
        setUpPerformCommentWithOnlyFDN();
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOnlyFDN() {
        setUpPerformCommentWithOnlyFDN();

        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        setUpCreateDescPersistenceObject();
        when(persistenceObjectBuilder.addAttributes(Matchers.anyMap())).thenReturn(persistenceObjectBuilder);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOutFDN() {
        setUpPerformCommentWithOnlyFDN();
        // alarmActionData.setObjectOfReference(null);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        final List<Long> alarmIdList = new ArrayList<Long>();
        alarmIdList.add(1234L);
        when((Long) persistenceObject.getAttribute("alarmNumber")).thenReturn(1234L);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        setUpCreateDescPersistenceObject();
        when(persistenceObjectBuilder.addAttributes(Matchers.anyMap())).thenReturn(persistenceObjectBuilder);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformCommentWithOnlyFDNResponse() {
        setUpPerformCommentWithOnlyFDN();

        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        setUpCreateDescPersistenceObject();
        when(persistenceObjectBuilder.addAttributes(Matchers.anyMap())).thenReturn(persistenceObjectBuilder);
        actionCommenter.performComment(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testProcessComment() {
        setUp();
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);

        setUpAlramDescList();
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put(ALARMNUMBER, 1234567L);
        alarmAttributes.put(OBJECTOFREFERENCE, OBJECTOFREFERENCE);
        alarmAttributes.put(LASTUPDATED, new Date());

        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        // when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");
        when(persistenceObject.getAttribute(ALARMSTATE)).thenReturn(UNACK);
        when(persistenceObject.getAttribute(ALARMNUMBER)).thenReturn(1234567L);
        when(persistenceObject.getAttribute(OBJECTOFREFERENCE)).thenReturn(OBJECTOFREFERENCE);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(12345L);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(liveBucket.findPosByIds(poIds)).thenReturn(persistenceObjects);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        when(persistenceObject.getPoId()).thenReturn(12345L);
        when(alarmActionUtils.prepareAlarmActionInformation("COMMENT", operatorName, 12345L)).thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionInformation> alarmActionInformation1 =
                actionCommenter.processComment(operatorName, "comment", poIds, responses);
        assertEquals(true, !alarmActionInformation1.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    public AlarmActionInformation prepareAlarmActionInformation() {
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setAlarmAction("COMMENT");
        alarmActionInformation.setOperatorName("Operator1");
        alarmActionInformation.setComment(comment);
        return alarmActionInformation;
    }
}
