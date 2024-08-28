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
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACK;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ADDITIONAL_INFORMATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALREADY_UNACK;
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
public class ActionUnAcknowledgerTest {

    @InjectMocks
    private ActionUnAcknowledger actionUnAcknowledger;

    @Mock
    private DownwardOperationSupportedHelperBean downwardAckSupportedHelperBean;

    @Mock
    private AlarmActionData alarmActionData;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject managedObject;

    @Mock
    private DataPersistenceService service;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private AASInstrumentedBean aasInstrumentedBean;

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
    private AlarmActionUtils alarmActionUtils;

    @Mock
    private EventSender<ProcessedAlarmEvent> modeledEventSender;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Mock
    private AlarmActionsCacheManager alarmActionsCacheManager;

    String objectOfReference = "MeContext=41,ManagedElement=41,ENodeBFunction=41";
    String operatorName = "AASOperator";
    String commentText = "Comment";
    // Map<String, String> response = mock(HashMap.class);
    List<AlarmActionResponse> response = mock(ArrayList.class);
    Map<String, String> actionResponse = mock(HashMap.class);
    Map<String, String> mtrRequest = mock(HashMap.class);
    Map<String, Map<String, String>> responseForMediationNAndCLI = mock(HashMap.class);
    Iterator<PersistenceObject> alarmDescPoIterator = mock(Iterator.class);
    Iterator<Object> alarmDescIterator = mock(Iterator.class);
    List<Long> alarmIdList = new ArrayList<Long>();
    final Map<String, Object> alarmMap = new HashMap<String, Object>(2);

    public void setUpFdnNotNull() {
        when(configurationsChangeListener.getDownwardAck()).thenReturn(true);
        alarmMap.put(ADDITIONAL_INFORMATION, "sourceType:CPP#alarmId:_9");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        alarmIdList.add(1234567L);
        alarmIdList.add(2345678L);
        when(alarmActionData.getObjectOfReference()).thenReturn(objectOfReference);
        when(alarmActionData.getOperatorName()).thenReturn(operatorName);
        when(alarmActionData.getAlarmAction()).thenReturn(AlarmAction.UNACK);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
    }

    public void setUpFdnNull() {
        when(configurationsChangeListener.getDownwardAck()).thenReturn(true);
        alarmIdList.add(1234567L);
        alarmIdList.add(2345678L);
        when(alarmActionData.getObjectOfReference()).thenReturn(null);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(alarmActionData.getAlarmAction()).thenReturn(AlarmAction.UNACK);
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
        setUpFdnNotNull();
        when(alarmActionData.getAlarmIds()).thenReturn(null);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, "OpenAlarm")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
    }

    @Test
    public void testPerformUnAckWithOnlyAlarmIDsWithNoAlarms() {
        setUpFdnNull();

        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("fake");
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        // final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        // actionUnAcknowledger.performUnAck(alarmActionData);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals("Alarm Not Found", responses.get(0).getResponse());
    }

    @Test
    public void testPerformUnAckWithOnlyAlarmIdsForActiveUnAck() {
        setUpFdnNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        // final List<PersistenceObject> persistenceObjects = liveBucket.findPosByIds(alarmIdList);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals("Alarm Is already Un-Acknowledged", responses.get(0).getResponse());
    }

    @Test
    public void testPerformUnAckWithOnlyAlarmIdsForActiveClearedUnAck() {
        setUpFdnNull();

        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals(ALREADY_UNACK, responses.get(0).getResponse());
    }

    @Test
    public void testPerformUnAckWithFdnAndAlarmIDsWithNoAlarms() {
        setUpFdnNotNull();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(persistenceObject.getType()).thenReturn("OpenAlarm");
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        when(liveBucket.findMoByFdn(objectOfReference)).thenReturn(managedObject);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals("Alarm Is already Un-Acknowledged", responses.get(0).getResponse());

    }

    @Test
    public void testPerformUnAckWithFDNAndAlarmIDsWithOorNotContainsFdn() {
        setUpOorContainsFdn();
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals("Alarm Not Found Under the FDN", responses.get(0).getResponse());
    }

    @Test
    public void testPerformUnAckWithFDNAndAlarmIDsWithOorNotContainsFdnWithFakeAlarms() {
        setUpOorContainsFdn();
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(alarmActionData.getAlarmIds()).thenReturn(alarmIdList);
        when(persistenceObject.getType()).thenReturn("fake");
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));

        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        // actionUnAcknowledger.performUnAck(alarmActionData);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals("Alarm Not Found Under the FDN", responses.get(0).getResponse());
    }

    @Test
    public void testPerformUnAckWithFDNAndAlarmIDsWithOorContainsFdn() {
        setUpOorContainsFdn();

        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn(objectOfReference);
        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals(ALREADY_UNACK, responses.get(0).getResponse());

        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        final List<AlarmActionResponse> responses1 = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations1 = actionUnAcknowledger.performUnAck(alarmActionData, responses1);
        assertEquals(true, !alarmActionInformations1.isEmpty());
        assertEquals("SUCCESS", responses1.get(0).getResponse());

        when(persistenceObject.getAttribute("alarmState")).thenReturn("fake");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        actionUnAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>());

    }

    @Test
    public void testPerformUnAckWithFDNAndAlarmIDsWithoutOorContainsFdnForN() {
        setUpOorContainsFdn();

        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        when(persistenceObject.getAttribute("objectOfReference")).thenReturn("fake");
        persistenceObjects.add(persistenceObject);

        when(persistenceObject.getAttribute("recordType")).thenReturn("fake");
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("CLEARED_UNACKNOWLEDGED");

        when(liveBucket.findPosByIds(alarmIdList)).thenReturn(persistenceObjects);
        final List<AlarmActionResponse> responses = new ArrayList<AlarmActionResponse>();
        final List<AlarmActionInformation> alarmActionInformations = actionUnAcknowledger.performUnAck(alarmActionData, responses);
        assertEquals(true, alarmActionInformations.isEmpty());
        assertEquals("Alarm Not Found Under the FDN", responses.get(0).getResponse());

    }

    @Test
    public void testPerformUnAckWithOnlyFDNWithNoAlarms() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(null);
        actionUnAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformUnAckWithOnlyFDNWithAlarms() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionUnAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformUnAckWithOnlyFDNWithErrors() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ERROR_MESSAGE");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionUnAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformUnAckWithOnlyFDNWithRepeatedErrors() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_ACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("REPEATED_ERROR_MESSAGE");
        when(persistenceObject.getAttribute("fdn")).thenReturn(objectOfReference);
        final Map<String, Object> alarmMap = new HashMap<String, Object>(2);
        alarmMap.put("visibility", true);
        alarmMap.put("additionalInformation", "additionalInformation");
        when(persistenceObject.getAllAttributes()).thenReturn(alarmMap);
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionUnAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testPerformUnAckWithOnlyFDNForActiveUnAck() {
        setUp();
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(alarmDescIterator);
        setUpAlramDescList();
        when(persistenceObject.getAttribute("alarmNumber")).thenReturn((long) (1234567));
        when(persistenceObject.getAttribute("alarmState")).thenReturn("ACTIVE_UNACKNOWLEDGED");
        when(persistenceObject.getAttribute("recordType")).thenReturn("ALARM");
        when(alarmActionUtils.prepareAlarmActionInformation(Matchers.anyString(), Matchers.anyString(), Matchers.anyLong()))
                .thenReturn(prepareAlarmActionInformation());
        actionUnAcknowledger.performUnAck(alarmActionData, new ArrayList<AlarmActionResponse>());
    }

    @Test
    public void testProcessUnAck() {
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
        when(persistenceObject.getAttribute(ALARMSTATE)).thenReturn(ACK);
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
        when(alarmActionUtils.prepareAlarmActionInformation("UNACK", operatorName, 12345L)).thenReturn(prepareAlarmActionInformation());
        final List<AlarmActionInformation> alarmActionInformation =
                actionUnAcknowledger.processUnAck(operatorName, poIds, responses);
        assertEquals(true, !alarmActionInformation.isEmpty());
        assertEquals(SUCCESS, responses.get(0).getResponse());
    }

    public AlarmActionInformation prepareAlarmActionInformation() {
        final AlarmActionInformation alarmActionInformation = new AlarmActionInformation();
        alarmActionInformation.setPoId(12345L);
        alarmActionInformation.setAlarmAction("UNACK");
        alarmActionInformation.setOperatorName("Operator1");
        return alarmActionInformation;
    }
}