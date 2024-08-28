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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil

class FMTBACAlarmActionDpsManagerTest extends CdiSpecification {

    @ObjectUnderTest
    FMTBACAlarmActionDpsManager fmTbacAlarmActionDpsManager = new FMTBACAlarmActionDpsManager()

    @MockedImplementation
    ConfigurationsChangeListener actionConfigurationListenerMock
    
    @MockedImplementation
    DpsUtil dpsUtilMock
    
    @Inject
    DataPersistenceService dataPersistenceService

    def openAlarmUtility = new OpenAlarmUtility()
    
    List<Object[]> getPersistenceObjects() {
        List<Object[]> persistenceObjectList = new ArrayList<Object[]>()
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            Object[] persistenceObject = new Object[3]
            persistenceObject[0] = po.getPoId()
            persistenceObject[1] = po.getAttribute("fdn")
            persistenceObject[2] = po.getAttribute("objectOfReference")
            persistenceObjectList.add(persistenceObject)
        }
        return persistenceObjectList
    }
    
    List<Long> getPoIds() {
        final List<Long> poIds = new ArrayList<Long>()
        for (final PersistenceObject po : openAlarmUtility.getPersistenceObjects()) {
            poIds.add(po.getPoId())
        }
        return poIds
    }
    
    List<Object[]> sortOpenAlarms(final List<Object[]> openAlarmsList) {
        final List<Object[]> sortedOpenAlarmsList = new ArrayList<Object[]>()
        final Map<Long, Object> openAlarmsMap = new HashMap<Long, Object>()
        for (final Object[] obj : openAlarmsList) {
            long key = obj[0] - 1
            openAlarmsMap.put(key, obj)
        }
        for (long index; index<openAlarmsList.size(); index++) {
            sortedOpenAlarmsList.add(openAlarmsMap.get(index))
        }
        return sortedOpenAlarmsList
    }

    def "Checking getAlarmListWithAttributes method case object of reference"(numOfAlarms) {
        given: "input parameters"
        openAlarmUtility.buildOpenAlarms(cdiInjectorRule, numOfAlarms)
        dpsUtilMock.getLiveBucket() >> dataPersistenceService.getLiveBucket()
        dpsUtilMock.getQueryBuilder() >> dataPersistenceService.getQueryBuilder()
        def objectOfReference = "Network Element"
        when: ""
        List<Object[]> openAlarms = fmTbacAlarmActionDpsManager.getAlarmListWithAttributes(objectOfReference)
        then: ""
        openAlarms.size() == numOfAlarms
        sortOpenAlarms(openAlarms) == getPersistenceObjects()
        where: ""
        numOfAlarms << [10, 20, 100]
    }

    def "Checking getAlarmListWithAttributes method case list of poIds"(numOfAlarms, batchSize, numOfBatches) {
        given: "input parameters"
        openAlarmUtility.buildOpenAlarms(cdiInjectorRule, numOfAlarms)
        actionConfigurationListenerMock.getAlarmActionBatchSize() >> batchSize
        dpsUtilMock.getLiveBucket() >> dataPersistenceService.getLiveBucket()
        dpsUtilMock.getQueryBuilder() >> dataPersistenceService.getQueryBuilder()
        when: ""
        List<Object[]> openAlarms = fmTbacAlarmActionDpsManager.getAlarmListWithAttributes(getPoIds())
        then: ""
        numOfBatches * dpsUtilMock.getQueryBuilder() >> dataPersistenceService.getQueryBuilder()
        openAlarms.size() == numOfAlarms
        sortOpenAlarms(openAlarms) == getPersistenceObjects()
        where: ""
        numOfAlarms | batchSize || numOfBatches
        3           | 10        || 1
        10          | 3         || 4
        10          | 10        || 1
        20          | 8         || 3
        20          | 10        || 2
        100         | 23        || 5
    }
}
