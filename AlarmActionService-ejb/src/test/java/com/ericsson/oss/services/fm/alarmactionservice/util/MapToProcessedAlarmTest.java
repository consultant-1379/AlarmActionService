/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.fm.alarmactionservice.util;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKSTATE_CHANGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACKTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ADDITIONAL_INFORMATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMNUMBER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARMSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.BACKUPOBJECTINSTANCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.BACKUPSTATUS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASEOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASETIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CHANGE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEAR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CLEARED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CORRELATEDEVENTPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CRITICAL;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HASH_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HBALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HISTORYALARMPOID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INDETERMINATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INSERTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTALARMOPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.LASTUPDATED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MAJOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MINOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NEW;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.NODESUSPENDED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OUT_OF_SYNC;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PRESENTSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PREVIOUSSEVERITY;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBABLECAUSE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBLEMDETAIL;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROBLEMTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.PROPOSEDREPAIRACTION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.RECORDTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.REPEATCOUNT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SPECIFICPROBLEM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SYNCABORT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SYNCALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SYNCIGNORED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.SYNCSTATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.TRENDINDICATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UNDEFINED;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.UPDATE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.WARNING;

import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_1;
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_2;
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.ROOT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.fm.alarmactionservice.util.ProcessedAlarmEventBuilder;
import com.ericsson.oss.services.fm.common.addinfo.CorrelationType;
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent;

@RunWith(MockitoJUnitRunner.class)
public class MapToProcessedAlarmTest {

    @InjectMocks
    ProcessedAlarmEventBuilder processedAlarmEventBuilder;

    public Map<String, Object> fillValuesInMap(final Map<String, Object> map) {

        map.put(OBJECTOFREFERENCE, "testOOR");
        map.put(FDN, "testFDN");
        map.put(EVENTTYPE, "testEVENTTYPE");
        map.put(EVENTTIME, new Date());
        map.put(PROBABLECAUSE, "testPROBABLECAUSE");
        map.put(SPECIFICPROBLEM, "testSPECIFICPROBLEM");
        map.put(BACKUPSTATUS, true);
        map.put(BACKUPOBJECTINSTANCE, "testBACKUPOBJECTINSTANCE");
        map.put(PROPOSEDREPAIRACTION, "testPROPOSEDREPAIRACTION");
        map.put(ALARMNUMBER, 12347L);
        map.put(ALARMID, 2345678L);
        map.put(CEASETIME, new Date());
        map.put(CEASEOPERATOR, "testCEASEOPERATOR");
        map.put(ACKTIME, new Date());
        map.put(ACKOPERATOR, "testACKOPERATOR");
        map.put(INSERTTIME, new Date());
        map.put(SYNCSTATE, true);
        map.put(HISTORYALARMPOID, 56897412L);
        map.put(CORRELATEDEVENTPOID, 86786453L);
        map.put(COMMENTTEXT, "testCOMMENTTEXT");
        map.put(LASTUPDATED, new Date());
        map.put(LASTALARMOPERATION, UNDEFINED);
        map.put(REPEATCOUNT, "3");

        map.put(PRESENTSEVERITY, INDETERMINATE);
        map.put(PREVIOUSSEVERITY, CLEARED);
        map.put(RECORDTYPE, ALARM);
        map.put(ALARMSTATE, "ACTIVE_ACKNOWLEDGED");
        map.put(TRENDINDICATION, "LESS_SEVERE");
        map.put(PROBLEMTEXT, "testProblemText");
        map.put(PROBLEMDETAIL, "testProblemDetail");
        map.put(ADDITIONAL_INFORMATION, "testAdditionalInformation");
        return map;
    }

    @Test
    public void testGetProcessedAlarm_ALARM() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_ERROR() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "ERROR_MESSAGE");
        map.put(ALARMSTATE, "ACTIVE_UNACKNOWLEDGED");
        map.put(TRENDINDICATION, "MORE_SEVERE");
        map.put(PREVIOUSSEVERITY, MAJOR);
        map.put(LASTALARMOPERATION, NEW);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_NO_SYNCHABLE() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "NO_SYNCHABLE_ALARM");
        map.put(ALARMSTATE, "CLEARED_ACKNOWLEDGED");
        map.put(TRENDINDICATION, "NO_CHANGE");
        map.put(PREVIOUSSEVERITY, CRITICAL);
        map.put(LASTALARMOPERATION, CHANGE);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_REPEATED() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "REPEATED_ALARM");
        map.put(ALARMSTATE, "CLEARED_UNACKNOWLEDGED");
        map.put(PREVIOUSSEVERITY, MINOR);
        map.put(LASTALARMOPERATION, ACKSTATE_CHANGE);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_SYNCALARM() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, SYNCALARM);
        map.put(ALARMSTATE, "CLOSED");
        map.put(PREVIOUSSEVERITY, WARNING);
        map.put(LASTALARMOPERATION, CLEAR);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_HBALARM() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, HBALARM);
        map.put(PREVIOUSSEVERITY, "Default");
        map.put(LASTALARMOPERATION, COMMENT);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_SYNCABORT() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, SYNCABORT);
        map.put(LASTALARMOPERATION, "Default");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_SYNCIGNORED() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, SYNCIGNORED);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_CLEAR_LIST() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "CLEAR_LIST");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_REPEATED_ERROR() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "REPEATED_ERROR_MESSAGE");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_REPEATED_NON() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "REPEATED_NON_SYNCHABLE");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_UPDATE() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, UPDATE);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_NODESUSPENDED() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, NODESUSPENDED);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_HB_FAILURE() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "HB_FAILURE_NO_SYNCH");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_SYNC_NETWORK() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "SYNC_NETWORK");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_TECHNICIAN_PRESENT() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "TECHNICIAN_PRESENT");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_ALARM_SUPPRESSED_ALARM() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "ALARM_SUPPRESSED_ALARM");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_OSCILLATORY_HB_ALARM() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, "OSCILLATORY_HB_ALARM");

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_OUT_OF_SYNC() {
        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(RECORDTYPE, OUT_OF_SYNC);
        map.put(PREVIOUSSEVERITY, INDETERMINATE);
        map.put(LASTALARMOPERATION, COMMENT);

        processedAlarmEventBuilder.getProcessedAlarm(map);
    }

    @Test
    public void testGetProcessedAlarm_ENRICH_CI() {
        final String REGISTERED_ADDITIONAL_INFORMATION = "DN2=ManagedElement=1,Equipment=1,RbsSubrack=RUW1,RbsSlot=5,AuxPlugInUnit=RUW-2,DeviceGroup=RUW,AiDeviceSet=1,AiDevice=1;CI ={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]};Direction=UL and DL AntennaUnitGroup=2,RfBranch=1.;";
        final String ENRICHMENT_CI = "\"S\":[\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"f91a6e32-e523-b217-7C3912ad3012\"]";
        final String REGISTERED_ADDITIONAL_INFORMATION_ENRICHED = "DN2=ManagedElement=1,Equipment=1,RbsSubrack=RUW1,RbsSlot=5,AuxPlugInUnit=RUW-2,DeviceGroup=RUW,AiDeviceSet=1,AiDevice=1;CI ={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]," + ENRICHMENT_CI + "};Direction=UL and DL AntennaUnitGroup=2,RfBranch=1.;";
        final String ADD_INFO_SOURCE_TYPE = HASH_DELIMITER + "sourceType:" + "RADIONODE";
        final String ADD_INFO_ALARM_ID = HASH_DELIMITER + "alarmId:" + 2345678L;
        final String ADD_INFO_TARGET_ADDITIONAL_INFORMATION = HASH_DELIMITER + "targetAdditionalInformation:";

        final String additionalInformation = ADD_INFO_SOURCE_TYPE + ADD_INFO_ALARM_ID + ADD_INFO_TARGET_ADDITIONAL_INFORMATION; 
        final String expectedAdditionalInformation = additionalInformation + REGISTERED_ADDITIONAL_INFORMATION_ENRICHED; 

        Map<String, Object> map = new HashMap<String, Object>();
        map = fillValuesInMap(map);
        map.put(ADDITIONAL_INFORMATION, additionalInformation + REGISTERED_ADDITIONAL_INFORMATION);
        map.put(ROOT, CorrelationType.SECONDARY);
        map.put(CI_GROUP_1, "81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        map.put(CI_GROUP_2, "f91a6e32-e523-b217-7C3912ad3012");

        final ProcessedAlarmEvent processedAlarmEvent = processedAlarmEventBuilder.getProcessedAlarm(map);

        assertEquals(expectedAdditionalInformation.length(), processedAlarmEvent.getAdditionalInformationString().length());
        assertTrue(processedAlarmEvent.getAdditionalInformationString().contains(ADD_INFO_SOURCE_TYPE));
        assertTrue(processedAlarmEvent.getAdditionalInformationString().contains(ADD_INFO_ALARM_ID));
        assertTrue(processedAlarmEvent.getAdditionalInformationString().contains(ADD_INFO_TARGET_ADDITIONAL_INFORMATION));
        assertTrue(processedAlarmEvent.getAdditionalInformationString().contains(ENRICHMENT_CI));      
    }
}
