/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.fm.alarmactionservice.impl

import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_1
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_2
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.ROOT

import com.ericsson.oss.itpf.datalayer.dps.DataBucket
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.services.fm.common.addinfo.CorrelationType
import com.ericsson.oss.services.models.alarm.EventState

/**
 * This class is responsible to fill data into DB and also into Cache.
 *
 */
class TestSetupInitializer {
    static def correlationInfo = [
        [
            'additionalInfo' : "#alarmId:11#sourceType:ERBS#targetAdditionalInformation:CI ={\"C\": [{\"I\": \"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\": \"vRC\"}]} ",
            'root' : CorrelationType.PRIMARY,
            'ciFirstGroup' : '81d4fae-7dec-11d0-a765-00a0c91e6bf6',
            'ciSecondGroup' : '',
            'alarmState' : EventState.ACTIVE_UNACKNOWLEDGED.toString()
        ],
        [
            'additionalInfo' : "#alarmId:111#sourceType:ERBS#targetAdditionalInformation:CI ={\"C\": [{\"I\": \"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\": \"vRC\"}]}",
            'root' : CorrelationType.SECONDARY,
            'ciFirstGroup' : '81d4fae-7dec-11d0-a765-00a0c91e6bf6',
            'ciSecondGroup' : 'f91a6e32-e523-b217-7C3912ad3012',
            'alarmState' : EventState.ACTIVE_ACKNOWLEDGED.toString()
        ],
        [
            'additionalInfo' : "#alarmId:1111#sourceType:ERBS#",
            'root' : CorrelationType.NOT_APPLICABLE,
            'ciFirstGroup' : '',
            'ciSecondGroup' : '',
            'alarmState' : EventState.ACTIVE_UNACKNOWLEDGED.toString()
        ]
    ]


    void persistsVisibleAlarms( runtimeDps ) {

        for(int i=0;i<3;i++){
            final Map<String, Object> OpenAlarmMap = new HashMap<String, Object>()
            createVisibleAlarms(OpenAlarmMap, i, correlationInfo[i])
            def po = runtimeDps.addPersistenceObject().namespace("FM").type("OpenAlarm").addAttributes(OpenAlarmMap).build()
            def poId = po.getPoId()
            def visbility = po.getAttribute("visibility")
            def eventTime = po.getAttribute("eventTime")
            def fdn = po.getAttribute("fdn")
            println("Alarm Created with PoId $poId and fdn $fdn and eventTime $eventTime")
        }
    }

    void createVisibleAlarms(Map OpenAlarmMap, int i, additionalInformation) {
        OpenAlarmMap.put("alarmNumber", (long) i+1);
        OpenAlarmMap.put("alarmId", (long) i+1);
        OpenAlarmMap.put('alarmState', additionalInformation.alarmState);
        OpenAlarmMap.put("fdn", "NetworkElement=AQS_GROOVY0034")
        OpenAlarmMap.put("objectOfReference", "NetworkElement=AQS_GROOVY0034")
        OpenAlarmMap.put("eventTime", new Date(1532614958671 - i*60000))
        OpenAlarmMap.put("presentSeverity", "MAJOR")
        OpenAlarmMap.put("visibility", true)
        OpenAlarmMap.put("probableCause", "probableCause")
        OpenAlarmMap.put("specificProblem", "specificProblem")
        OpenAlarmMap.put("additionalInformation", additionalInformation.additionalInfo)
        OpenAlarmMap.put("operatorName", "")
        OpenAlarmMap.put("comment", "no comment")
        OpenAlarmMap.put(ROOT, additionalInformation.root)
        OpenAlarmMap.put(CI_GROUP_1, additionalInformation.ciFirstGroup)
        OpenAlarmMap.put(CI_GROUP_2, additionalInformation.ciSecondGroup)
        println("OpenAlarm Created with alarmState ${additionalInformation.alarmState}")
        
    }

    void persistsNodes( runtimeDps ) {
        DataBucket liveBucket = runtimeDps.build().getLiveBucket()
        for(int i=0;i<3;i++){
            liveBucket = runtimeDps.build().getLiveBucket()
            final ManagedObject networkElement = liveBucket.getMibRootBuilder().namespace("OSS_NE_DEF").version("2.0.0").name("AQS_GROOVY00"+i).type("NetworkElement").create()
            String fdn = networkElement.getFdn()
            println("Node Created is $fdn")
        }
    }
}
