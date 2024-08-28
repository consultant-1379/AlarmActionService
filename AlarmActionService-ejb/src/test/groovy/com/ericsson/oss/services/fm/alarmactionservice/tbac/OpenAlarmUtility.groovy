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
package com.ericsson.oss.services.fm.alarmactionservice.tbac;

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps

class OpenAlarmUtility {
    
    final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>()

    Random random = new Random()
    def severity = [
        'WARNING',
        'CLEARED',
        'CRITICAL',
        'MAJOR',
        'MINOR',
        'INDETERMINATE'
    ]

    def buildOpenAlarms(cdiInjectorRule, numberOfAlarms) {
        if(numberOfAlarms < 0){
            println 'Error ! Number of alarms selected is not valid: ' + numberOfAlarms
            return;
        }
        RuntimeConfigurableDps configurableDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        for(int i=0;i<numberOfAlarms;i++){
            def randomAlarmId = Math.abs(random.nextInt()%99999)
            Map<String, Object> alarmAttributes = buildAttributeMap(randomAlarmId)
            persistenceObjects.add(configurableDps.addPersistenceObject().namespace("FM").type('OpenAlarm').version("1.0.1").addAttributes(alarmAttributes).build())
        }
    }
    
    List<PersistenceObject> getPersistenceObjects() {
        return persistenceObjects
    }

    def randomDate() {
        def day = Math.abs(random.nextInt()%28)
        def month = Math.abs(random.nextInt()%12)
        def hour = Math.abs(random.nextInt()%24)
        def minute = Math.abs(random.nextInt()%60)
        def second = Math.abs(random.nextInt()%60)
        def str = day + '.' + month + '.2017 ' + hour +':' + minute+':'+second
        def extendedDate = new Date().parse("dd.MM.yyy HH:mm:ss", str)
        //println ' new random date ' + extendedDate
        return extendedDate;
    }
    def randomSeverity() {
        def severityIndex = Math.abs(random.nextInt()%6)
        def randSeverity = severity[severityIndex]
        //println ' new severity ' +  randSeverity
        return randSeverity
    }

    def buildAttributeMap(number){
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes.put('EVENT_TYPE', "EventType_"+number);
        alarmAttributes.put('LAST_UPDATED', randomDate());
        alarmAttributes.put('PREVIOUS_SEVERITY', randomSeverity());
        alarmAttributes.put('PRESENT_SEVERITY', randomSeverity());
        alarmAttributes.put('PSEUDO_PRESENT_SEVERITY', randomSeverity());
        alarmAttributes.put('PSEUDO_PREVIOUS_SEVERITY', randomSeverity());
        alarmAttributes.put('ALARM_STATE', "AlarmState_"+number);
        alarmAttributes.put('PROBLEM_TEXT', "ProblemText_"+number);
        alarmAttributes.put('PROBLEM_CAUSE', "ProblemCause_"+number);
        alarmAttributes.put('SPECIFIC_PROBLEM', "SpecificProblem_"+number);
        alarmAttributes.put('alarmNumber', number);
        alarmAttributes.put('fdn', "NE_"+number);
        alarmAttributes.put('objectOfReference', "Network Element");
        return alarmAttributes;
    }
}
