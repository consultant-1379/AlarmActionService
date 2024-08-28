/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.alarm.action.service.integration.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.SecurityPrivilegeServiceMock;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.TargetGroupRegistry;

import com.ericsson.oss.services.fm.common.addinfo.CorrelationType;

import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_1;
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.CI_GROUP_2;
import static com.ericsson.oss.services.fm.common.constants.AddInfoConstants.ROOT;

@Singleton
@Startup
public class DummyDataCreator {

    final static Logger LOGGER = LoggerFactory.getLogger(DummyDataCreator.class);

    final static String REGISTERED_ADDITIONAL_INFORMATION = "DN2=ManagedElement=1,Equipment=1,RbsSubrack=RUW1,RbsSlot=5,AuxPlugInUnit=RUW-2,DeviceGroup=RUW,AiDeviceSet=1,AiDevice=1;CI ={\"C\": [{\"I\": \"201f0123-88ca-23a2-7451-8B5872ac457b\",\"n\": \"vRC\"}]};Direction = UL and DL AntennaUnitGroup=2,RfBranch=1.;";

    @EServiceRef
    private DataPersistenceService service;

    @Inject
    private TargetGroupRegistry targetGroupRegistry;

    @PostConstruct
    public void createDummydata() {
        try {
            for (Integer i = 1; i < 5; i++) {
                createNetworkElement(i.toString());
                createVirtualNetworkFunctionManager(i.toString());
                targetGroupRegistry.addNodeToTargetGroup(i.toString(), SecurityPrivilegeServiceMock.SEC_TARGET_GROUP_1);
                targetGroupRegistry.addNodeToTargetGroup(i.toString(), SecurityPrivilegeServiceMock.SEC_TARGET_GROUP_ALL);
            }
            for (Integer i = 5; i < 9; i++) {
                createNetworkElement(i.toString());
                createVirtualNetworkFunctionManager(i.toString());
                targetGroupRegistry.addNodeToTargetGroup(i.toString(), SecurityPrivilegeServiceMock.SEC_TARGET_GROUP_2);
                targetGroupRegistry.addNodeToTargetGroup(i.toString(), SecurityPrivilegeServiceMock.SEC_TARGET_GROUP_ALL);
            }
        } catch (final Exception e) {
            LOGGER.error("Network Element not created ");
        }
    }

    @PreDestroy
    public void clearDummydata() {
        removeNetworkElement();
        removeVirtualNetworkFunctionManager();
        removeTestAlarms();
    }

    public void createNetworkElement(final String nodeName) {
        LOGGER.info(" Creating Network Element {}", nodeName);
        final DataBucket liveBucket = service.getLiveBucket();
        final Map<String, Object> moAttributes = new HashMap<String, Object>();
        moAttributes.put("networkElementId", "testId");
        moAttributes.put("neType", "ERBS");
        moAttributes.put("platformType", "CPP");
        moAttributes.put("ossPrefix", "MeContext=" + nodeName);
        moAttributes.put("ossModelIdentity", "1294-439-662");

        final ManagedObject networkElement = liveBucket.getMibRootBuilder().type("NetworkElement").namespace("OSS_NE_DEF").version("2.0.0")
                .name(nodeName).addAttributes(moAttributes).create();

        LOGGER.info(" Network Element Created with FDN: {} ", networkElement.getFdn());
    }

    public void createVirtualNetworkFunctionManager(final String vnfmName) {
        LOGGER.info(" Creating Virtual Network Funtion Manager {}", vnfmName);
        final DataBucket liveBucket = service.getLiveBucket();
        final Map<String, Object> moAttributes = new HashMap<String, Object>();
        final List<String> tenants = new ArrayList<String>();
        tenants.add("tenant01");
        moAttributes.put("vmType", "ECM");
        moAttributes.put("tenants", tenants);

        final ManagedObject vnfm = liveBucket.getMibRootBuilder().type("VirtualNetworkFunctionManager").namespace("OSS_NE_DEF").version("1.0.0")
                .name(vnfmName).addAttributes(moAttributes).create();
        LOGGER.info(" Virtual Network Function Manager Created with FDN: {} ", vnfm.getFdn());
    }

    public void removeNetworkElement() {
        LOGGER.info(" Removing Network Element which is created for testing ");
        final DataBucket liveBucket = service.getLiveBucket();
        final QueryBuilder queryBuilder = service.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery("OSS_NE_DEF", "NetworkElement");
        final Iterator<PersistenceObject> iterator = liveBucket.getQueryExecutor().execute(typeQuery);
        while (iterator.hasNext()) {
            final PersistenceObject persistenceObject = iterator.next();
            liveBucket.deletePo(persistenceObject);
        }
    }

    public void removeVirtualNetworkFunctionManager() {
        LOGGER.info(" Removing Virtual Network Function Manager which is created for testing ");
        final DataBucket liveBucket = service.getLiveBucket();
        final QueryBuilder queryBuilder = service.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery("OSS_NE_DEF", "VirtualNetworkFunctionManager");
        final Iterator<PersistenceObject> iterator = liveBucket.getQueryExecutor().execute(typeQuery);
        while (iterator.hasNext()) {
            final PersistenceObject persistenceObject = iterator.next();
            liveBucket.deletePo(persistenceObject);
        }
    }

    public List<Long> createTestAlarms(final Integer from, final Integer to) {
        final List<Long> alarmIdsList = new ArrayList<Long>();
        for (Integer i = from; i < to; i++) {
            alarmIdsList.add(createAlarm("ACTIVE_UNACKNOWLEDGED", "CRITICAL", "sntpServerDown", "Communicationsalarm", "LossOfSignal", 111,
                    "MeContext=" + i, "NetworkElement=" + i, "ERBS"));
            alarmIdsList.add(createAlarm("ACTIVE_ACKNOWLEDGED", "MAJOR", "tempTooLow", "Environmentalalarm",
                    "SoftwareProgramAbnormallyTerminated", 222, "MeContext=" + i, "NetworkElement=" + i, "ERBS"));
            alarmIdsList.add(createAlarm("CLEARED_UNACKNOWLEDGED", "CLEARED", "cardFailure", "Equipmentalarm", "ThresholdCrossed", 333,
                    "MeContext=" + i, "NetworkElement=" + i, "ERBS"));
        }
        return alarmIdsList;
    }

    public List<Long> createEcmTestAlarms(final Integer from, final Integer to) {
        final List<Long> alarmIdsList = new ArrayList<Long>();
        for (Integer i = from; i < to; i++) {
            alarmIdsList.add(createAlarm("ACTIVE_UNACKNOWLEDGED", "MAJOR", "sntpServerDown", "Communicationsalarm", "LossOfSignal", 111,
                    "VirtualNetworkFunctionManager=" + i, "VirtualNetworkFunctionManager=" + i, "ECM"));
            alarmIdsList.add(createAlarm("ACTIVE_ACKNOWLEDGED", "MAJOR", "tempTooLow", "Environmentalalarm",
                    "SoftwareProgramAbnormallyTerminated", 222, "VirtualNetworkFunctionManager=" + i, "VirtualNetworkFunctionManager=" + i, "ECM"));
            alarmIdsList.add(createAlarm("CLEARED_UNACKNOWLEDGED", "CLEARED", "cardFailure", "Equipmentalarm", "ThresholdCrossed", 333,
                    "VirtualNetworkFunctionManager=" + i, "VirtualNetworkFunctionManager=" + i, "ECM"));
        }
        return alarmIdsList;
    }

    public void removeTestAlarms() {
        final DataBucket liveBucket = service.getLiveBucket();
        final QueryBuilder queryBuilder = service.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery("FM", "OpenAlarm");
        final Iterator<PersistenceObject> iterator = liveBucket.getQueryExecutor().execute(typeQuery);
        while (iterator.hasNext()) {
            final PersistenceObject persistenceObject = iterator.next();
            liveBucket.deletePo(persistenceObject);
        }
    }

    public Long createAlarm(final String alarmState, final String presentSeverity, final String specificProblem,
            final String eventType, final String probableCause, final long alarmNumber, final String objectOfReference, final String fdn,
            final String sourceType) {
        final DataBucket liveBucket = service.getLiveBucket();
        final Map<String, Object> OpenAlarmMap = new HashMap<String, Object>();

        OpenAlarmMap.put("objectOfReference", objectOfReference);
        OpenAlarmMap.put("fdn", fdn);
        OpenAlarmMap.put("eventTime", new Date());
        OpenAlarmMap.put("presentSeverity", presentSeverity);
        OpenAlarmMap.put("probableCause", probableCause);
        OpenAlarmMap.put("specificProblem", specificProblem);
        OpenAlarmMap.put("alarmNumber", alarmNumber);
        OpenAlarmMap.put("eventType", eventType);
        OpenAlarmMap.put("backupObjectInstance", "Unknown");
        OpenAlarmMap.put("recordType", "ALARM");
        OpenAlarmMap.put("backupStatus", true);
        OpenAlarmMap.put("trendIndication", "LESS_SEVERE");
        OpenAlarmMap.put("previousSeverity", "CRITICAL");
        OpenAlarmMap.put("proposedRepairAction", "Unknown");
        OpenAlarmMap.put("alarmId", alarmNumber);
        OpenAlarmMap.put("alarmState", alarmState);
        OpenAlarmMap.put("commentText", "Hello World ");
        // OpenAlarmMap.put("ceaseTime", new Date());
        OpenAlarmMap.put("ceaseOperator", "AAS Operator");
        OpenAlarmMap.put("ackTime", new Date());
        OpenAlarmMap.put("ackOperator", "APSOperator");
        OpenAlarmMap.put("syncState", true);
        OpenAlarmMap.put("repeatCount", 1);
        final String additionalInformation = "#sourceType:" + sourceType + "#alarmId:" + alarmNumber + "#targetAdditionalInformation:" + REGISTERED_ADDITIONAL_INFORMATION; 
        OpenAlarmMap.put("additionalInformation", additionalInformation);
        OpenAlarmMap.put("problemDetail", "problemDetail");
        OpenAlarmMap.put("problemText", "problemText");
        OpenAlarmMap.put(ROOT, CorrelationType.SECONDARY.toString());
        OpenAlarmMap.put(CI_GROUP_1, "81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        OpenAlarmMap.put(CI_GROUP_2, "f91a6e32-e523-b217-7C3912ad3012");

        final PersistenceObject po = liveBucket.getPersistenceObjectBuilder().namespace("FM").type("OpenAlarm").version("1.0.1")
                .addAttributes(OpenAlarmMap).create();
        final Long alaramRecordPoId = po.getPoId();
        LOGGER.info("OpenAlarm PO  stored in DPS with POId: {} and alarm number: {} OOR: {}", alaramRecordPoId, alarmNumber, objectOfReference);
        return alaramRecordPoId;
    }
}
