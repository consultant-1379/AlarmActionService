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

import com.ericsson.cds.cdi.support.configuration.InjectionProperties
import com.ericsson.cds.cdi.support.providers.custom.model.ClasspathModelServiceProvider
import com.ericsson.cds.cdi.support.providers.custom.model.ModelPattern
import com.ericsson.cds.cdi.support.providers.custom.model.RealModelServiceProvider
import com.ericsson.cds.cdi.support.rule.ImplementationInstance
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.cache.annotation.NamedCache
import com.ericsson.oss.itpf.sdk.eventbus.EventConfiguration
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest
import com.ericsson.oss.services.alarm.action.service.model.AlarmAction
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse
import com.ericsson.oss.services.fm.alarmactionservice.cache.AlarmActionsCacheManager
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener
import com.ericsson.oss.services.fm.models.AlarmActionInformation
import com.ericsson.oss.services.fm.models.processedevent.ProcessedAlarmEvent
import com.ericsson.oss.services.fm.service.model.FmMediationAckRequest
import com.ericsson.oss.services.fm.service.model.FmMediationClearRequest
import com.ericsson.oss.services.models.alarm.EventState
import spock.lang.Unroll

import javax.cache.Cache
import javax.inject.Inject

class AlarmActionServiceTest extends CdiSpecification {

    @ObjectUnderTest
    AlarmActionServiceImpl alarmActionServiceImpl

    @ImplementationInstance
    ConfigurationsChangeListener actionConfigurationListener = new ConfigurationsChangeListener(alarmActionBatchSize: 10L, downwardAck: true, enableAutoAckOnManualClearedAlarms: true)

    @Inject
    AlarmActionsCacheManager alarmActionsCacheManager

    @Inject
    @NamedCache("AlarmActionsCache")
    Cache<Boolean, AlarmActionInformation> alarmActionsCache

    @Inject
    EventSender<MediationTaskRequest> eventSender

    @Inject
    EventSender<ProcessedAlarmEvent> nbiSender

    /**
     * Real Model Provider allows the test to use the real model service using the JAr dependencies
     * (just declare the models you need as a test scope dependency)
     */
    static RealModelServiceProvider realModelServiceProvider = new ClasspathModelServiceProvider([
            new ModelPattern('dps_primarytype', 'FM', 'DynamicAlarmAttributeInformation', '.*'),
            new ModelPattern('dps_primarytype', 'FM', 'OpenAlarm', '.*'),
            new ModelPattern('dps_primarytype', 'FM', 'CommentOperation', '.*'),
            new ModelPattern('oss_edt', 'FM', 'DownwardAckSupportedNodeTypes', '.*')
    ])

    @Override
    Object addAdditionalInjectionProperties(InjectionProperties injectionProperties) {
        injectionProperties.addInjectionProvider(realModelServiceProvider)
    }

    @Unroll("Update Alarm with #actionName action and poId #poId")
    def "Update Alarm"() {
        given: "any CompositeNodeCriteria"
        def actionNameToString = [
                (AlarmAction.ACK)  : 'ACKNOWLEDGE',
                (AlarmAction.UNACK): 'UNACKNOWLEDGE',
                (AlarmAction.CLEAR): 'CLEAR'
        ]

        def actionNameToMTR = [
                (AlarmAction.ACK)  : FmMediationAckRequest.class,
                (AlarmAction.UNACK): FmMediationAckRequest.class,
                (AlarmAction.CLEAR): FmMediationClearRequest.class
        ]

        def alarmActionData = new AlarmActionData(
                operatorName: "",
                comment: comment,
                action: actionName,
                alarmIds: [poId])

        when: "execute action #actionName on alarms"
        List<AlarmActionResponse> alarmActionResponse = alarmActionServiceImpl.alarmActionUpdate(alarmActionData)

        then: "alarmActionResponse is #response,alarmState change to #eventState and #numberOfMtr MTR are sent to mediation, #numberOfEventToNBI events to NBI"
        println("alarmActionResponse= " + alarmActionResponse)
        alarmActionResponse[0].response == response
        alarmActionResponse[0].eventPoId == poId.toString()
        alarmActionResponse[0].objectOfReference == objectOfReference
        alarmActionResponse.size() == numberOfPoIdsFound
        getVisibleAlarmsByPoid(poId).getAttribute("alarmState") == state.toString()
        getVisibleAlarmsByPoid(poId).getAttribute("commentText") == expectedOuput

        numberOfMtr * eventSender.send({ request -> request.getClass() == actionNameToMTR[actionName] && request?.strAckStatus == actionNameToString[actionName] })
        numberOfEventToNBI * nbiSender.send({ alarmEvent -> alarmEvent.getAdditionalInformation().get("targetAdditionalInformation") == expectedCI } as ProcessedAlarmEvent, _ as String, _ as EventConfiguration)

        where: "Multiple inputs supplied to AlarmActionData "
        objectOfReference               | actionName          | poId | comment    || response                           | numberOfPoIdsFound | state                             | numberOfEventToNBI | numberOfMtr | expectedOuput | expectedCI
        "NetworkElement=AQS_GROOVY0034" | AlarmAction.ACK     | 4L   | ""         || 'SUCCESS'                          | 1                  | EventState.ACTIVE_ACKNOWLEDGED    | 3                  | 1           | null          | "CI={\"P\":\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]};"
        "NetworkElement=AQS_GROOVY0034" | AlarmAction.UNACK   | 4L   | ""         || 'Alarm Is already Un-Acknowledged' | 1                  | EventState.ACTIVE_UNACKNOWLEDGED  | 0                  | 0           | null          | "CI={\"P\":\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]};"
        "NetworkElement=AQS_GROOVY0034" | AlarmAction.COMMENT | 5L   | "????1234" || 'SUCCESS'                          | 1                  | EventState.ACTIVE_ACKNOWLEDGED    | 3                  | 0           | "????1234"    | "CI={\"S\":[\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"f91a6e32-e523-b217-7C3912ad3012\"],\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]};"
        "NetworkElement=AQS_GROOVY0034" | AlarmAction.CLEAR   | 4L   | ""         || 'SUCCESS'                          | 1                  | EventState.CLEARED_UNACKNOWLEDGED | 3                  | 0           | null          | "CI={\"P\":\"81d4fae-7dec-11d0-a765-00a0c91e6bf6\",\"C\":[{\"I\":\"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\":\"vRC\"}]};"
    }

    PersistenceObject getVisibleAlarmsByPoid(poid) {
        def runtimeDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        return runtimeDps.build().getLiveBucket().findPoById(poid)
    }

    def setup() {
        def runtimeDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        def testSetupInitializer = new TestSetupInitializer()
        testSetupInitializer.persistsNodes(runtimeDps)
        testSetupInitializer.persistsVisibleAlarms(runtimeDps)
        runtimeDps.resetTransactionalState()
        alarmActionsCacheManager.alarmActionsCache = alarmActionsCache
    }
}