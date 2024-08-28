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

package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ADDITIONAL_INFORMATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMDESCRIPTION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_NOTFOUND;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SPECIFICPROBLEM;
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
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.RestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.alarm.action.service.instrumentation.AASInstrumentedBean;
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.DownwardOperationRequestSender;
import com.ericsson.oss.services.fm.alarmactionservice.actionupdates.sender.DownwardOperationSupportedHelperBean;
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionUtils;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

@RunWith(MockitoJUnitRunner.class)
public class ActionAcknowledgerTest {

    @InjectMocks
    private ActionAcknowledger actionAcknowledger;

    @Mock
    private DownwardOperationSupportedHelperBean downwardAckSupportedHelperBean;

    @Mock
    private AlarmActionData alarmActionData;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private AASInstrumentedBean aasInstrumentedBean;

    @Mock
    private ManagedObject managedObject;

    @Mock
    private DataPersistenceService service;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private Query<TypeRestrictionBuilder> typeQuery;

    @Mock
    private TypeRestrictionBuilder typeRestrictionBuilder;

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private Restriction restriction;

    @Mock
    private RestrictionBuilder restrictionBuilder;

    @Mock
    private PersistenceObject persistenceObject;

    @Mock
    private DownwardOperationRequestSender ackRequestSender;

    @Mock
    private SystemRecorder systemRecorder;

    @Mock
    public AlarmActionUtils alarmActionUtils;

    @Mock
    private EventSender<ProcessedAlarmEvent> modeledEventSender;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    String objectOfReference = "MeContext=41,ManagedElement=41,ENodeBFunction=41";
    String operatorName = "AASOperator";
    String commentText = "Comment";
    List<AlarmActionResponse> response = mock(ArrayList.class);
    Map<String, String> actionResponse = mock(HashMap.class);
    Map<String, String> mtrRequest = mock(HashMap.class);
    Map<String, Map<String, String>> responseForMediationNAndCLI = mock(HashMap.class);
    Iterator<Object> alarmDescIterator = mock(Iterator.class);
    Iterator<PersistenceObject> alarmDescPoIterator = mock(Iterator.class);
    List<Long> alarmIdList = new ArrayList<Long>();
    final Map<String, Object> alarmMap = new HashMap<String, Object>(2);

    public void setUpFdnNotNull() {
        alarmMap.put(ADDITIONAL_INFORMATION, "sourceType:CPP#alarmId:_9");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        alarmIdList.add(1234567l);
        alarmIdList.add(2345678l);
        when(alarmActionData.getObjectOfReference()).thenReturn(objectOfReference);
        when(alarmActionData.getOperatorName()).thenReturn(operatorName);
        when(alarmActionData.getOperatorName()).thenReturn("Administrator");
        when(alarmActionData.getAlarmAction()).thenReturn(AlarmAction.ACK);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
    }

    public void setUpFdnNull() {
        alarmMap.put(ADDITIONAL_INFORMATION, "sourceType:CPP#alarmId:_9");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        alarmIdList.add(1234567l);
        alarmIdList.add(2345678l);
        when(alarmActionData.getObjectOfReference()).thenReturn(null);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        alarmActionData.setAction(AlarmAction.ACK);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findPoById(Matchers.anyLong())).thenReturn(persistenceObject);
    }

    public void setUpAlramDescList() {
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        final Collection<PersistenceObject> alramDescList = mock(Collection.class);
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(alramDescList);
        when(alramDescList.iterator()).thenReturn(alarmDescPoIterator);
        when(alarmDescIterator.hasNext()).thenReturn(true, true, false);
        when(alarmDescIterator.next()).thenReturn(persistenceObject);
    }

    public void setUpOorContainsFdn() {
        setUpFdnNotNull();
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        when(liveBucket.findPoById(Matchers.anyLong())).thenReturn(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
    }

    public void setUp() {
        when(configurationsChangeListener.getDownwardAck()).thenReturn(true);
        setUpFdnNotNull();
        when(alarmActionData.getAlarmIds()).thenReturn(null);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
    }

    @Test
    public void testPerformAckWithAlarmIdsNoAlarms() {
        setUpFdnNull();
        when(persistenceObject.getType()).thenReturn("fake");
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckAlarmNotNullCliRequestActiveAck() {
        setUpFdnNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        final AlarmActionData actionData = new AlarmActionData();
        actionData.setAction(AlarmAction.ACK);
        actionData.setOperatorName("Operator1");
        alarmIdList.add(1234567l);
        alarmIdList.add(2345678l);
        actionData.setAlarmIds(alarmIdList);

        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionAcknowledger.performAckWithoutBatching(actionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckWithDifferentPoTypes() {
        setUpFdnNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);

        when(persistenceObject.getType()).thenReturn("fake");
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckAlarmNotNullMtrRequestClearedUnAck() {
        setUpFdnNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getPoId()).thenReturn(1234567l);
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        final Collection<PersistenceObject> alarmAssociaiton = new ArrayList<PersistenceObject>();
        alarmAssociaiton.add(persistenceObject);
        when(persistenceObject.getAssociations(ALARMDESCRIPTION)).thenReturn(alarmAssociaiton);
        setUpAlramDescList();
        alarmMap.put("fdn", objectOfReference);
        alarmMap.put("visibility", true);
        alarmMap.put("alarmState", "ACTIVE_UNACKNOWLEDGED");
        alarmMap.put("objectOfReference", objectOfReference);
        alarmMap.put("alarmNumber", (long) (1234567));
        alarmMap.put("recordType", "fake");

        final AlarmActionData actionData = new AlarmActionData();
        actionData.setAction(AlarmAction.UNACK);
        actionData.setOperatorName("Operator1");
        alarmIdList.add(1234567l);
        alarmIdList.add(2345678l);
        actionData.setAlarmIds(alarmIdList);
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(actionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckAlarmNotNullMtrRuquestForNotOpenAlarms() {
        setUpFdnNull();

        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();

        when(persistenceObject.getType()).thenReturn("Fake");
        persistenceObjects.add(persistenceObject);

        final List<Long> alarmIdList = alarmActionData.getAlarmIds();
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);

        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals("Alarm Not Found", responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckAlarmNotNullUnAck() {
        setUpFdnNull();
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(liveBucket.deletePo(persistenceObject)).thenReturn(0);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        setUpAlramDescList();
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals("Alarm Not Found", responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckAlarmNotNullUnAckAlarmDecNull() {
        setUpFdnNull();
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(liveBucket.deletePo(persistenceObject)).thenReturn(0);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        final Collection<PersistenceObject> alramDescList = mock(Collection.class);
        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(null);
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());

        when(persistenceObject.getAssociations("alarmDescription")).thenReturn(alramDescList);
        when(alramDescList.iterator()).thenReturn(alarmDescPoIterator);
        when(alarmDescPoIterator.hasNext()).thenReturn(true, true, false);
        when(alarmDescPoIterator.next()).thenReturn(persistenceObject);
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());

    }

    @Test
    public void testPerformAckWithOnlyAlarmIDsWithNoAlarms() {
        setUpFdnNotNull();
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals("Alarm Not Found Under the FDN", responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckWithOnlyAlarmIDsWithNoAlarmsForAlarmNotFoundUnderFdn() {
        setUpFdnNotNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();

        when(persistenceObject.getType()).thenReturn("Fake");
        persistenceObjects.add(persistenceObject);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        // final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals("Alarm Not Found Under the FDN", responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckWithOnlyAlarmIDsWithNoAlarmsForSuccess() {
        setUpFdnNotNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();

        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getPoId()).thenReturn(1234567l);
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        persistenceObjects.add(persistenceObject);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put("visibility", true);
        alarmAttributes.put("alarmNumber", (long) (1234567));
        alarmAttributes.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(alarmActionData, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckWithAlarmIDsAndOORWithOORMisMatch() {
        setUpFdnNotNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();

        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        persistenceObjects.add(persistenceObject);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        // final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");

        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation = actionAcknowledger.performAckWithoutBatching(alarmActionData, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals("Alarm Not Found Under the FDN", responses.get(0).getResponse());
    }

    @Test
    public void testPerformAckWithFDNAndAlarmIDsWithOorNotContainsFdn() {
        setUpOorContainsFdn();
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckWithFDNAndAlarmIDsWithOorContainsFdn() {
        setUpOorContainsFdn();
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
        when(persistenceObject.getAttribute("alarmState")).thenReturn("fake");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckWithOnlyFDNWithNoAlarms() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(null);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckWithOnlyFDNWithAlarms() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put("visibility", true);
        alarmAttributes.put("recordType", "fake");
        alarmAttributes.put("alarmNumber", (long) (1234567));
        alarmAttributes.put("alarmState", "CLEARED_UNACKNOWLEDGED");
        alarmAttributes.put("fdn", objectOfReference);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformAckWithOnlyFDNWithAlarmsAndError() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put("visibility", true);
        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionAcknowledger.performAckWithoutBatching(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testProcessAckForSingleBatch_AlarmNot_Found() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put("visibility", true);
        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        // when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(12345L);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformation =
                actionAcknowledger.processAckForSingleBatch(operatorName, poIds, responses);
        assertEquals(true, alarmActionInformation.isEmpty());
        assertEquals(ALARM_NOTFOUND, responses.get(0).getResponse());
    }

    @Test
    public void testProcessAckForSingleBatch() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put("visibility", true);
        alarmAttributes.put(ALARMNUMBER, 1234567L);
        alarmAttributes.put(OBJECTOFREFERENCE, objectOfReference);
        alarmAttributes.put(FDN, "LTE02ERBS0001");
        alarmAttributes.put(SPECIFICPROBLEM, SPECIFICPROBLEM);
        alarmAttributes.put(ALARMSTATE, UNACK);
        alarmAttributes.put(LASTUPDATED, new Date());

        when(persistenceObject.getAllAttributes()).thenReturn(alarmAttributes);
        // when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");
        when(persistenceObject.getAttribute(ALARMSTATE)).thenReturn(UNACK);
        when(persistenceObject.getAttribute(ALARMNUMBER)).thenReturn(1234567L);
        when(persistenceObject.getAttribute(OBJECTOFREFERENCE)).thenReturn(objectOfReference);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<Long> poIds = new ArrayList<Long>();
        poIds.add(12345L);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(liveBucket.findPosByIds(poIds)).thenReturn(persistenceObjects);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        when(persistenceObject.getPoId()).thenReturn(12345L);
        when(alarmActionUtils.prepareAlarmActionInformation("ACK", operatorName, 12345L)).thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionInformation> alarmActionInformation1 =
                actionAcknowledger.processAckForSingleBatch(operatorName, poIds, responses);
        assertEquals(true, !alarmActionInformation1.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    public AlarmActionInformation prepareAlarmActionInformation() {
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setAlarmAction("ACK");
        alarmActionInformation.setOperatorName("Operator1");
        return alarmActionInformation;
    }

}
