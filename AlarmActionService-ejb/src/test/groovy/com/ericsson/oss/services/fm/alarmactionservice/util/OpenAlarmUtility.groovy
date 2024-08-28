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
package com.ericsson.oss.services.fm.alarmactionservice.util;

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.fm.common.addinfo.CorrelationType

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

    def basicAdditionalInformation = "#sourceType:RadioNode#targetAdditionalInformation:DN2=ManagedElement=1;"

    def basicCI = "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]};"

    def basicReducedCI = "CI={};"

    def buildOpenAlarms(cdiInjectorRule, root, ciReduced) {
        RuntimeConfigurableDps configurableDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        def randomAlarmId = Math.abs(random.nextInt()%99999)
        Map<String, Object> alarmAttributes = buildAttributeMap(randomAlarmId, root, ciReduced)
        persistenceObjects.add(configurableDps.addPersistenceObject().namespace("FM").type('OpenAlarm').version("1.0.1").addAttributes(alarmAttributes).build())
        return alarmAttributes;
    }

    def buildMalformedOpenAlarms(cdiInjectorRule, root, ciReduced, noGroup, noRoot) {
        RuntimeConfigurableDps configurableDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        def randomAlarmId = Math.abs(random.nextInt()%99999)
        Map<String, Object> alarmAttributes = buildMalformedAttributeMap(randomAlarmId, root, ciReduced, noGroup, noRoot)
        persistenceObjects.add(configurableDps.addPersistenceObject().namespace("FM").type('OpenAlarm').version("1.0.1").addAttributes(alarmAttributes).build())
        return alarmAttributes;
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

    def builBasicAttributeMap(number, ciReduced){
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        final String additionalInformation = ciReduced ? (basicAdditionalInformation + basicReducedCI) : (basicAdditionalInformation + basicCI)
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
        alarmAttributes.put('alarmNumber', Long.valueOf(number));
        alarmAttributes.put('fdn', "NE_"+number);
        alarmAttributes.put('objectOfReference', "Network Element");
        alarmAttributes.put('additionalInformation', additionalInformation);
        return alarmAttributes;
    }

    def buildAttributeMap(number, root, ciReduced){
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes = builBasicAttributeMap(number, ciReduced);
        alarmAttributes.put('root', root);
        if (root != null && root != CorrelationType.NOT_APPLICABLE.toString()) {
            alarmAttributes.put('ciFirstGroup', "81d4fae-7dec-11d0-a765-00a0c91e6bf6");
            if (root == "SECONDARY") {
                alarmAttributes.put('ciSecondGroup', "f91a6e32-e523-b217-7C3912ad3012");
            }
        }
        return alarmAttributes;
    }

    def buildMalformedAttributeMap(number, root, ciReduced, noGroup, noRoot){
        final Map<String, Object> alarmAttributes = new HashMap<String, Object>();
        alarmAttributes = builBasicAttributeMap(number, ciReduced);
        if (!noRoot) {
            alarmAttributes.put('root', root);
            if (!noGroup) {
                if (root == null || root == CorrelationType.NOT_APPLICABLE.toString()) {
                    alarmAttributes.put('ciFirstGroup', "81d4fae-7dec-11d0-a765-00a0c91e6bf6");
                    alarmAttributes.put('ciSecondGroup', "f91a6e32-e523-b217-7C3912ad3012");
                } else if (root == "PRIMARY") {
                    alarmAttributes.put('ciSecondGroup', "f91a6e32-e523-b217-7C3912ad3012");
                } else if (root == "SECONDARY") {
                    alarmAttributes.put('ciSecondGroup', "f91a6e32-e523-b217-7C3912ad3012");
                }
            }
        }
        return alarmAttributes;
    }
}
