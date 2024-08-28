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
package com.ericsson.oss.services.fm.alarmactionservice.tbac

import javax.inject.Inject

import org.apache.commons.lang.StringUtils

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataBucket
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.query.Query
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil
import com.ericsson.oss.services.fm.common.tbac.FMTBACAccessControl

class FMTBACAlarmActionHandlerTest extends CdiSpecification {

    @ObjectUnderTest
    FMTBACAlarmActionHandler fmTbacAlarmActionHandler = new FMTBACAlarmActionHandler()

    @MockedImplementation
    FMTBACAccessControl accessControlMock

    @MockedImplementation
    ESecuritySubject securitySubjectMock

    @MockedImplementation
    ConfigurationsChangeListener actionConfigurationListenerMock

    @Inject
    private DpsUtil dpsUtil

    def openAlarmUtility = new OpenAlarmUtility()

    final AlarmActionData actionData = new AlarmActionData()
    final List<Long> alarmIds = new ArrayList<Long>()
    final List<Long> poIds = new ArrayList<Long>()
    List<AlarmActionResponse> alarmActionResponse = new ArrayList<AlarmActionResponse>()

    def setup() {
        openAlarmUtility.buildOpenAlarms(cdiInjectorRule, 3)
        securitySubjectMock.getSubjectId() >> "admin"
        accessControlMock.getAuthUserSubject() >> securitySubjectMock
        actionConfigurationListenerMock.getAlarmActionBatchSize() >> 100
    }

    def printAllAlarms() {
        final DataBucket liveBucket = dpsUtil.getLiveBucket()
        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder()
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery("FM", "OpenAlarm")
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor()
        final Iterator<PersistenceObject> openAlarmItr = queryExecutor.execute(typeQuery)
        while (openAlarmItr.hasNext()) {
            final PersistenceObject mePo = openAlarmItr.next()
            println 'Found OPEN ALARM -> PoID: ' +  mePo.getPoId() + ' {'
            for (final Map.Entry<String, Object> attributeEntry : mePo.getAllAttributes().entrySet()) {
                println '\tKEY: \"' + attributeEntry.getKey() + '\" ,VALUE: \"' + attributeEntry.getValue() +'\"'
            }
            println '}'
        }
    }

    def "Checking preProcess method case negative"(isAlarmIdsNull, isPoIdsNull, isObjectOfReferenceNull, alarmId, poId, objectOfReference, result) {
        given: "input parameters"
        if (isAlarmIdsNull) {
            actionData.setAlarmIds(null)
        } else {
            actionData.setAlarmIds(new ArrayList<Long>())
        }
        if (isPoIdsNull) {
            actionData.setPoIds(null)
        } else {
            actionData.setPoIds(new ArrayList<Long>())
        }
        if (isObjectOfReferenceNull) {
            actionData.setObjectOfReference(null)
        } else {
            actionData.setObjectOfReference("")
        }
        when: ""
        boolean output = fmTbacAlarmActionHandler.preProcess(accessControlMock, actionData)
        then: ""
        fmTbacAlarmActionHandler.filteredAlarmIds.size() == 0
        fmTbacAlarmActionHandler.filteredEventPoIds.size() == 0
        fmTbacAlarmActionHandler.filteredObjectOfReference.size() == 0
        actionData.getAlarmIds() == alarmId
        actionData.getPoIds() == poId
        actionData.getObjectOfReference() == objectOfReference
        output == result
        where: ""
        isAlarmIdsNull | isPoIdsNull | isObjectOfReferenceNull || alarmId | poId | objectOfReference | result
        true           | true        | true                    || null    | null | null              | false
        true           | true        | false                   || null    | null | ""                | false
        true           | false       | true                    || null    | []   | null              | false
        true           | false       | false                   || null    | []   | ""                | false
        false          | true        | true                    || []      | null | null              | false
        false          | true        | false                   || []      | null | ""                | false
        false          | false       | true                    || []      | []   | null              | false
        false          | false       | false                   || []      | []   | ""                | false
    }

    def "Checking preProcess method case AlarmIds"(isPoIdsNull, isObjectOfReferenceNull, authorized, filteredAlarmIdsSize, response, eventPoId, alarmId, poId, objectOfReference, result) {
        given: "input parameters"
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            if (po.getPoId() < 3L)
                alarmIds.add(po.getPoId())
        }
        actionData.setAlarmIds(alarmIds)
        if (isPoIdsNull) {
            actionData.setPoIds(null)
        } else {
            actionData.setPoIds(new ArrayList<Long>())
        }
        if (isObjectOfReferenceNull) {
            actionData.setObjectOfReference(null)
        } else {
            actionData.setObjectOfReference("")
        }
        accessControlMock.isAuthorized(*_) >> authorized
        when: ""
        boolean output = fmTbacAlarmActionHandler.preProcess(accessControlMock, actionData)
        then: ""
        fmTbacAlarmActionHandler.filteredAlarmIds.size() == filteredAlarmIdsSize
        fmTbacAlarmActionHandler.filteredEventPoIds.size() == 0
        fmTbacAlarmActionHandler.filteredObjectOfReference.size() == 0
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : fmTbacAlarmActionHandler.filteredAlarmIds) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        responses == response
        eventPoIds == eventPoId
        actionData.getAlarmIds() == alarmId
        actionData.getPoIds() == poId
        actionData.getObjectOfReference() == objectOfReference
        output == result
        where: ""
        isPoIdsNull | isObjectOfReferenceNull | authorized || filteredAlarmIdsSize | response                     | eventPoId | alarmId | poId | objectOfReference | result
        true        | true                    | false      || 2                    | "Access deniedAccess denied" | "12"      | []      | null | null              | false
        true        | false                   | false      || 2                    | "Access deniedAccess denied" | "12"      | []      | null | ""                | false
        false       | true                    | false      || 2                    | "Access deniedAccess denied" | "12"      | []      | []   | null              | false
        false       | false                   | false      || 2                    | "Access deniedAccess denied" | "12"      | []      | []   | ""                | false
        true        | true                    | true       || 0                    | ""                           | ""        | [1, 2]  | null | null              | true
        true        | false                   | true       || 0                    | ""                           | ""        | [1, 2]  | null | ""                | true
        false       | true                    | true       || 0                    | ""                           | ""        | [1, 2]  | []   | null              | true
        false       | false                   | true       || 0                    | ""                           | ""        | [1, 2]  | []   | ""                | true
    }

    def "Checking preProcess method case PoIds"(isAlarmIdsNull, isObjectOfReferenceNull, authorized, filteredPoIdsSize, response, eventPoId, alarmId, poId, objectOfReference, result) {
        given: "input parameters"
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            if (po.getPoId() < 3L)
                poIds.add(po.getPoId())
        }
        actionData.setPoIds(poIds)
        if (isAlarmIdsNull) {
            actionData.setAlarmIds(null)
        } else {
            actionData.setAlarmIds(new ArrayList<Long>())
        }
        if (isObjectOfReferenceNull) {
            actionData.setObjectOfReference(null)
        } else {
            actionData.setObjectOfReference("")
        }
        accessControlMock.isAuthorized(*_) >> authorized
        when: ""
        boolean output = fmTbacAlarmActionHandler.preProcess(accessControlMock, actionData)
        then: ""
        fmTbacAlarmActionHandler.filteredEventPoIds.size() == filteredPoIdsSize
        fmTbacAlarmActionHandler.filteredAlarmIds.size() == 0
        fmTbacAlarmActionHandler.filteredObjectOfReference.size() == 0
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : fmTbacAlarmActionHandler.filteredEventPoIds) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        responses == response
        eventPoIds == eventPoId
        actionData.getAlarmIds() == alarmId
        actionData.getPoIds() == poId
        actionData.getObjectOfReference() == objectOfReference
        output == result
        where: ""
        isAlarmIdsNull | isObjectOfReferenceNull | authorized || filteredPoIdsSize | response                     | eventPoId | alarmId | poId   | objectOfReference | result
        true           | true                    | false      || 2                 | "Access deniedAccess denied" | "12"      | null    | []     | null              | false
        true           | false                   | false      || 2                 | "Access deniedAccess denied" | "12"      | null    | []     | ""                | false
        false          | true                    | false      || 2                 | "Access deniedAccess denied" | "12"      | []      | []     | null              | false
        false          | false                   | false      || 2                 | "Access deniedAccess denied" | "12"      | []      | []     | ""                | false
        true           | true                    | true       || 0                 | ""                           | ""        | null    | [1, 2] | null              | true
        true           | false                   | true       || 0                 | ""                           | ""        | null    | [1, 2] | ""                | true
        false          | true                    | true       || 0                 | ""                           | ""        | []      | [1, 2] | null              | true
        false          | false                   | true       || 0                 | ""                           | ""        | []      | [1, 2] | ""                | true
    }

    def "Checking preProcess method case ObjectOfReference"(isAlarmIdsNull, isPoIdsNull, authorized, filteredObjectOfReferenceSize, response, eventPoId, alarmId, poId, objectOfReference, result) {
        given: "input parameters"
        actionData.setObjectOfReference("Network Element")
        if (isAlarmIdsNull) {
            actionData.setAlarmIds(null)
        } else {
            actionData.setAlarmIds(new ArrayList<Long>())
        }
        if (isPoIdsNull) {
            actionData.setPoIds(null)
        } else {
            actionData.setPoIds(new ArrayList<Long>())
        }
        accessControlMock.isAuthorized(*_) >> authorized
        when: ""
        boolean output = fmTbacAlarmActionHandler.preProcess(accessControlMock, actionData)
        then: ""
        fmTbacAlarmActionHandler.filteredAlarmIds.size() == 0
        fmTbacAlarmActionHandler.filteredEventPoIds.size() == 0
        fmTbacAlarmActionHandler.filteredObjectOfReference.size() == filteredObjectOfReferenceSize
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : fmTbacAlarmActionHandler.filteredObjectOfReference) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        responses.contains(response)
        eventPoIds == eventPoId
        actionData.getAlarmIds() == alarmId
        actionData.getPoIds() == poId
        actionData.getObjectOfReference() == objectOfReference
        output == result
        where: ""
        isAlarmIdsNull | isPoIdsNull | authorized || filteredObjectOfReferenceSize | response        | eventPoId | alarmId   | poId | objectOfReference | result
        true           | true        | false      || 3                             | "Access denied" | "123"     | null      | null | "Network Element" | false
        true           | false       | false      || 3                             | "Access denied" | "123"     | null      | []   | "Network Element" | false
        false          | true        | false      || 3                             | "Access denied" | "123"     | []        | null | "Network Element" | false
        false          | false       | false      || 3                             | "Access denied" | "123"     | []        | []   | "Network Element" | false
        true           | true        | true       || 0                             | ""              | ""        | [1, 2, 3] | null | null              | true
        true           | false       | true       || 0                             | ""              | ""        | [1, 2, 3] | []   | null              | true
        false          | true        | true       || 0                             | ""              | ""        | [1, 2, 3] | null | null              | true
        false          | false       | true       || 0                             | ""              | ""        | [1, 2, 3] | []   | null              | true
    }

    def "Checking preProcess method case AlarmIds, PoIds and ObjectOfReference"(sameInfo, authorized, filteredAlarmIdsSize, filteredPoIdsSize, filteredObjectOfReferenceSize, response1, response2, eventPoId1, eventPoId2, eventPoId3, alarmId, poId, objectOfReference, result) {
        given: "input parameters"
        actionData.setAlarmIds(new ArrayList<Long>())
        actionData.setPoIds(new ArrayList<Long>())
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            final Long id = po.getPoId()
            if ((sameInfo && id < 3L) || (!sameInfo && id == 1L)) {
                alarmIds.add(id)
            }
            if ((sameInfo && id < 3L) || (!sameInfo && id == 2L)) {
                poIds.add(id)
            }
        }
        actionData.setAlarmIds(alarmIds)
        actionData.setPoIds(poIds)
        actionData.setObjectOfReference("Network Element")
        accessControlMock.isAuthorized(*_) >> authorized
        when: ""
        boolean output = fmTbacAlarmActionHandler.preProcess(accessControlMock, actionData)
        then: ""
        fmTbacAlarmActionHandler.filteredAlarmIds.size() == filteredAlarmIdsSize
        fmTbacAlarmActionHandler.filteredEventPoIds.size() == filteredPoIdsSize
        fmTbacAlarmActionHandler.filteredObjectOfReference.size() == filteredObjectOfReferenceSize

        String responsesAlarmIds = ""
        String eventPoIdsAlarmIds = ""
        for (AlarmActionResponse actionResponse : fmTbacAlarmActionHandler.filteredAlarmIds) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responsesAlarmIds = responsesAlarmIds + msg
            eventPoIdsAlarmIds = eventPoIdsAlarmIds + ePoId
        }
        responsesAlarmIds == response1
        eventPoIdsAlarmIds == eventPoId1

        String responsesPoIds = ""
        String eventPoIdsPods = ""
        for (AlarmActionResponse actionResponse : fmTbacAlarmActionHandler.filteredEventPoIds) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responsesPoIds = responsesPoIds + msg
            eventPoIdsPods = eventPoIdsPods + ePoId
        }
        responsesPoIds == response1
        eventPoIdsPods == eventPoId2

        String responsesObjectOfReference = ""
        String eventPoIdsObjectOfReference = ""
        for (AlarmActionResponse actionResponse : fmTbacAlarmActionHandler.filteredObjectOfReference) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responsesObjectOfReference = responsesObjectOfReference + msg
            eventPoIdsObjectOfReference = eventPoIdsObjectOfReference + ePoId
        }
        responsesObjectOfReference.contains(response2)
        eventPoIdsObjectOfReference == eventPoId3

        actionData.getAlarmIds() == alarmId
        actionData.getPoIds() == poId
        actionData.getObjectOfReference() == objectOfReference
        output == result
        where: ""
        sameInfo | authorized || filteredAlarmIdsSize | filteredPoIdsSize | filteredObjectOfReferenceSize | response1                    | response2       | eventPoId1 | eventPoId2 | eventPoId3 | alarmId   | poId   | objectOfReference | result
        true     | false      || 2                    | 2                 | 1                             | "Access deniedAccess denied" | "Access denied" | "12"       | "12"       | "3"        | []        | []     | "Network Element" | false
        false    | false      || 1                    | 1                 | 1                             | "Access denied"              | "Access denied" | "1"        | "2"        | "3"        | []        | []     | "Network Element" | false
        true     | true       || 0                    | 0                 | 0                             | ""                           | ""              | ""         | ""         | ""         | [1, 2, 3] | [1, 2] | null              | true
        false    | true       || 0                    | 0                 | 0                             | ""                           | ""              | ""         | ""         | ""         | [1, 2, 3] | [2]    | null              | true
    }

    def "Checking postProcess method case negative"() {
        given: "input parameters"
        alarmActionResponse = null
        fmTbacAlarmActionHandler.filteredAlarmIds.clear()
        fmTbacAlarmActionHandler.filteredEventPoIds.clear()
        fmTbacAlarmActionHandler.filteredObjectOfReference.clear()
        when: ""
        List<AlarmActionResponse> output = fmTbacAlarmActionHandler.postProcess(accessControlMock, alarmActionResponse)
        then: ""
        output == []
    }

    def "Checking postProcess method case filteredAlarmIds not empty"(isResponseNull, sameInfo, alarmActionResponseSize, deny, eventPoId) {
        given: "input parameters"
        alarmActionResponse.clear()
        fmTbacAlarmActionHandler.filteredAlarmIds.clear()
        fmTbacAlarmActionHandler.filteredEventPoIds.clear()
        fmTbacAlarmActionHandler.filteredObjectOfReference.clear()
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            final Long id = po.getPoId()
            fmTbacAlarmActionHandler.addPoIdToFilteredList(fmTbacAlarmActionHandler.filteredAlarmIds, id, po)
        }
        if (isResponseNull) {
            alarmActionResponse = null
        } else if (sameInfo) {
            alarmActionResponse = fmTbacAlarmActionHandler.filteredAlarmIds
        } else {
            AlarmActionResponse actionResponse = new AlarmActionResponse()
            actionResponse.setResponse("Access ok")
            actionResponse.setObjectOfReference("Network Element")
            actionResponse.setAlarmNumber("1")
            actionResponse.setEventPoId("3")
            alarmActionResponse.add(actionResponse)
        }
        when: ""
        List<AlarmActionResponse> output = fmTbacAlarmActionHandler.postProcess(accessControlMock, alarmActionResponse)
        then: ""
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : output) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        output.size() == alarmActionResponseSize
        StringUtils.countMatches(responses, "Access denied") == deny
        eventPoIds == eventPoId
        where: ""
        isResponseNull | sameInfo || alarmActionResponseSize | deny | eventPoId
        true           | false    || 3                       | 3    | "123"
        false          | true     || 3                       | 3    | "123"
        false          | false    || 3                       | 2    | "312"
    }

    def "Checking postProcess method case filteredEventPoIds not empty"(isResponseNull, sameInfo, alarmActionResponseSize, deny, eventPoId) {
        given: "input parameters"
        alarmActionResponse.clear()
        fmTbacAlarmActionHandler.filteredAlarmIds.clear()
        fmTbacAlarmActionHandler.filteredEventPoIds.clear()
        fmTbacAlarmActionHandler.filteredObjectOfReference.clear()
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            final Long id = po.getPoId()
            fmTbacAlarmActionHandler.addPoIdToFilteredList(fmTbacAlarmActionHandler.filteredEventPoIds, id, po)
        }
        if (isResponseNull) {
            alarmActionResponse = null
        } else if (sameInfo) {
            alarmActionResponse = fmTbacAlarmActionHandler.filteredEventPoIds
        } else {
            AlarmActionResponse actionResponse = new AlarmActionResponse()
            actionResponse.setResponse("Access ok")
            actionResponse.setObjectOfReference("Network Element")
            actionResponse.setAlarmNumber("1")
            actionResponse.setEventPoId("3")
            alarmActionResponse.add(actionResponse)
        }
        when: ""
        List<AlarmActionResponse> output = fmTbacAlarmActionHandler.postProcess(accessControlMock, alarmActionResponse)
        then: ""
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : output) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        output.size() == alarmActionResponseSize
        StringUtils.countMatches(responses, "Access denied") == deny
        eventPoIds == eventPoId
        where: ""
        isResponseNull | sameInfo || alarmActionResponseSize | deny | eventPoId
        true           | false    || 3                       | 3    | "123"
        false          | true     || 3                       | 3    | "123"
        false          | false    || 3                       | 2    | "312"
    }

    def "Checking postProcess method case filteredObjectOfReference not empty"(isResponseNull, sameInfo, alarmActionResponseSize, deny, ok, eventPoId) {
        given: "input parameters"
        alarmActionResponse.clear()
        fmTbacAlarmActionHandler.filteredAlarmIds.clear()
        fmTbacAlarmActionHandler.filteredEventPoIds.clear()
        fmTbacAlarmActionHandler.filteredObjectOfReference.clear()
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            final Long id = po.getPoId()
            fmTbacAlarmActionHandler.addPoIdToFilteredList(fmTbacAlarmActionHandler.filteredObjectOfReference, id, po)
        }
        if (isResponseNull) {
            alarmActionResponse = null
        } else if (sameInfo) {
            alarmActionResponse = fmTbacAlarmActionHandler.filteredObjectOfReference
        } else {
            AlarmActionResponse actionResponse = new AlarmActionResponse()
            actionResponse.setResponse("Access ok")
            actionResponse.setObjectOfReference("Network Element")
            actionResponse.setAlarmNumber("1")
            actionResponse.setEventPoId("3")
            alarmActionResponse.add(actionResponse)
        }
        when: ""
        List<AlarmActionResponse> output = fmTbacAlarmActionHandler.postProcess(accessControlMock, alarmActionResponse)
        then: ""
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : output) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        output.size() == alarmActionResponseSize
        StringUtils.countMatches(responses, "Access ok") == ok
        StringUtils.countMatches(responses, "Access denied") == deny
        eventPoIds == eventPoId
        where: ""
        isResponseNull | sameInfo || alarmActionResponseSize | deny | ok | eventPoId
        true           | false    || 3                       | 3    | 0  | "123"
        false          | true     || 3                       | 3    | 0  | "123"
        false          | false    || 3                       | 2    | 1  | "312"
    }

    def "Checking postProcess method case filteredAlarmIds, filteredEventPoIds and filteredObjectOfReference not empty"(isResponseNull, sameInfoForResponse, sameInfoForFiltered, alarmActionResponseSize, deny, ok, eventPoId) {
        given: "input parameters"
        alarmActionResponse.clear()
        fmTbacAlarmActionHandler.filteredAlarmIds.clear()
        fmTbacAlarmActionHandler.filteredEventPoIds.clear()
        fmTbacAlarmActionHandler.filteredObjectOfReference.clear()
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            final Long id = po.getPoId()
            if (sameInfoForFiltered || (!sameInfoForFiltered && id == 1L)) {
                fmTbacAlarmActionHandler.addPoIdToFilteredList(fmTbacAlarmActionHandler.filteredAlarmIds, id, po)
            }
            if (sameInfoForFiltered || (!sameInfoForFiltered && id == 2L)) {
                fmTbacAlarmActionHandler.addPoIdToFilteredList(fmTbacAlarmActionHandler.filteredEventPoIds, id, po)
            }
            fmTbacAlarmActionHandler.addPoIdToFilteredList(fmTbacAlarmActionHandler.filteredObjectOfReference, id, po)
        }
        if (isResponseNull) {
            alarmActionResponse = null
        } else if (sameInfoForResponse) {
            alarmActionResponse = fmTbacAlarmActionHandler.filteredObjectOfReference
        } else {
            AlarmActionResponse actionResponse = new AlarmActionResponse()
            actionResponse.setResponse("Access ok")
            actionResponse.setObjectOfReference("Network Element")
            actionResponse.setAlarmNumber("1")
            actionResponse.setEventPoId("3")
            alarmActionResponse.add(actionResponse)
        }
        when: ""
        List<AlarmActionResponse> output = fmTbacAlarmActionHandler.postProcess(accessControlMock, alarmActionResponse)
        then: ""
        String responses = ""
        String eventPoIds = ""
        for (AlarmActionResponse actionResponse : output) {
            String msg = actionResponse.getResponse()
            String ePoId = actionResponse.getEventPoId()
            responses = responses + msg
            eventPoIds = eventPoIds + ePoId
        }
        output.size() == alarmActionResponseSize

        StringUtils.countMatches(responses, "Access ok") == ok
        StringUtils.countMatches(responses, "Access denied") == deny
        eventPoIds == eventPoId
        where: ""
        isResponseNull | sameInfoForResponse | sameInfoForFiltered || alarmActionResponseSize | deny | ok | eventPoId
        true           | false               | true                || 3                       | 3    | 0  | "123"
        true           | false               | false               || 3                       | 3    | 0  | "123"
        false          | true                | true                || 3                       | 3    | 0  | "123"
        false          | true                | false               || 3                       | 3    | 0  | "123"
        false          | false               | true                || 3                       | 2    | 1  | "312"
        false          | false               | false               || 3                       | 2    | 1  | "312"
    }
}
