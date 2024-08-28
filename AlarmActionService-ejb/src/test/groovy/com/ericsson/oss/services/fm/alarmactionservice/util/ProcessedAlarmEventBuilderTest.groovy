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
package com.ericsson.oss.services.fm.alarmactionservice.util

import javax.inject.Inject

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent

class ProcessedAlarmEventBuilderTest extends CdiSpecification {

    @Inject
    private DpsUtil dpsUtil

    def openAlarmUtility = new OpenAlarmUtility()

    def "Checking erichement of Additional Information attribute of the ProcessedAlarmEvent with Correlation Information"(root, correlationInformation, ciReduced) {
        given: "alarm in database"
        Map<String, Object> alarmAttributes = openAlarmUtility.buildOpenAlarms(cdiInjectorRule, root, ciReduced)
        when: ""
        final ProcessedAlarmEvent processedAlarmEvent = ProcessedAlarmEventBuilder.getProcessedAlarm(alarmAttributes)
        then: ""
        processedAlarmEvent.getAdditionalInformationString().contains(correlationInformation)
        where: ""
        root             || correlationInformation                                                                       || ciReduced
        "PRIMARY"        || "\"P\":\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\""                                              || false
        "SECONDARY"      || "S\":[\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"f91a6e32-e523-b217-7C3912ad3012\"]"          || false
        "PRIMARY"        || "CI={\"P\":\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\"}"                                         || true
        "SECONDARY"      || "CI={\"S\":[\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"f91a6e32-e523-b217-7C3912ad3012\"]}"   || true
        "NOT_APPLICABLE" || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"                || false
        "NOT_APPLICABLE" || "CI={}"                                                                                      || true
        null             || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"                || false
        null             || "CI={}"                                                                                      || true
    }

    def "Negative cases - Checking erichement of Additional Information attribute of the ProcessedAlarmEvent with Correlation Information"(root, correlationInformation, ciReduced, noGroup, noRoot) {
        given: "alarm in database"
        Map<String, Object> alarmAttributes = openAlarmUtility.buildMalformedOpenAlarms(cdiInjectorRule, root, ciReduced, noGroup, noRoot)
        when: ""
        final ProcessedAlarmEvent processedAlarmEvent = ProcessedAlarmEventBuilder.getProcessedAlarm(alarmAttributes)
        then: ""
        processedAlarmEvent.getAdditionalInformationString().contains(correlationInformation)
        where: ""
        root             || correlationInformation                                                              || ciReduced || noGroup || noRoot
        "NOT_APPLICABLE" || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"       || false     || false   || false
        "NOT_APPLICABLE" || "CI={}"                                                                             || true      || false   || false
        null             || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"       || false     || false   || false
        null             || "CI={}"                                                                             || true      || false   || false
        "PRIMARY"        || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"       || false     || false   || false
        "PRIMARY"        || "CI={}"                                                                             || true      || false   || false
        "SECONDARY"      || "CI={\"S\":[\"f91a6e32-e523-b217-7C3912ad3012\"]}"                                  || true      || false   || false
        "SECONDARY"      || "S\":[\"f91a6e32-e523-b217-7C3912ad3012\"]"                                         || false     || false   || false
        "PRIMARY"        || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"       || false     || true    || false
        "PRIMARY"        || "CI={}"                                                                             || true      || true    || false
        "SECONDARY"      || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"       || false     || true    || false
        "SECONDARY"      || "CI={}"                                                                             || true      || true    || false
        "no root"        || "CI={\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]}"       || false     || true    || true
        
    }
}