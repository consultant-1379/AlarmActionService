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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ACCESS_DENIED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.alarm.action.service.model.AlarmActionData;
import com.ericsson.oss.services.alarm.action.service.model.AlarmActionResponse;
import com.ericsson.oss.services.fm.common.tbac.FMTBACAccessControl;
import com.ericsson.oss.services.fm.common.tbac.FMTBACHandler;
import com.ericsson.oss.services.fm.common.tbac.FMTBACParamHandler;

/**
 * TBAC Parameter handler implementation for AlarmActionData type.
 */
@FMTBACHandler(handlerId = "FMTBACAlarmActionHandler")
public class FMTBACAlarmActionHandler implements FMTBACParamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FMTBACAlarmActionHandler.class);

    @Inject
    private FMTBACAlarmActionDpsManager dpsManager;

    private FMTBACAccessControl accessControlManager;
    private final List<AlarmActionResponse> filteredAlarmIds = new ArrayList<>();
    private final List<AlarmActionResponse> filteredEventPoIds = new ArrayList<>();
    private final List<AlarmActionResponse> filteredObjectOfReference = new ArrayList<>();

    private final Map<String, Boolean> authorizedMapByTarget = new HashMap<String, Boolean>();

    /**
     * Apply TBAC filtering on the given AlarmActionData object reference.
     * @param inputParameter
     *            the INPUT parameter
     * @return true if more action to process (call context.proceed()) or false if only postProcess() call is needed.
     */
    @Override
    public boolean preProcess(final FMTBACAccessControl accessControlManager, final Object inputParameter) {
        this.accessControlManager = accessControlManager;

        filteredAlarmIds.clear();
        filteredEventPoIds.clear();
        filteredObjectOfReference.clear();
        authorizedMapByTarget.clear();

        final AlarmActionData actionData = (AlarmActionData) inputParameter;

        LOGGER.debug("Filtering AlarmActionData: {} Current user: {}", actionData, accessControlManager.getAuthUserSubject().getSubjectId());

        Set<Long> poIdsSet = new HashSet<>();
        boolean result = false;
        List<Long> grantedPoIdListByObjectOfReference = new ArrayList<>();
        List<Long> grantedPoIdListByPoId = new ArrayList<>();

        final String objectOfReference = actionData.getObjectOfReference();
        if (isNotBlank(objectOfReference)) {
            final List<Object[]> alarmList = dpsManager.getAlarmListWithAttributes(objectOfReference);

            // generate granted list of poId and remove not authorized from ActionData
            grantedPoIdListByObjectOfReference = authorizeAlarmList(actionData, alarmList);
            LOGGER.trace("Number of alarm by objectOfReference {}: {} --> granted {}", objectOfReference, alarmList.size(),
                    grantedPoIdListByObjectOfReference.size());
        }

        if (actionData.getAlarmIds() != null) {
            poIdsSet.addAll(actionData.getAlarmIds());
        }
        if (actionData.getPoIds() != null) {
            poIdsSet.addAll(actionData.getPoIds());
        }
        if (!poIdsSet.isEmpty()) {
            final List<Long> poIds = new ArrayList<>(poIdsSet);
            final List<Object[]> alarmList = dpsManager.getAlarmListWithAttributes(poIds);
            // generate granted list of poId and remove not authorized from ActionData
            grantedPoIdListByPoId = authorizeAlarmList(actionData, alarmList);
            LOGGER.trace("Number PoIds received: {} --> granted {}", poIds.size(), grantedPoIdListByPoId.size());
        }

        // for object of reference case AlarmIds is used for processing (proceed input)
        if (!grantedPoIdListByObjectOfReference.isEmpty()) {
            if (actionData.getAlarmIds() == null) {
                actionData.setAlarmIds(new ArrayList<Long>());
            }
            poIdsSet = new HashSet<>(actionData.getAlarmIds());
            poIdsSet.addAll(new HashSet<>(grantedPoIdListByObjectOfReference));

            actionData.setAlarmIds(new ArrayList<>(poIdsSet));
            actionData.setObjectOfReference(null);
            LOGGER.debug("Number of AlarmIds {} loaded for object of reference case", actionData.getAlarmIds().size());
        }
        // true if more action to process (call context.proceed()) or false if only postProcess() call is needed.
        if (!grantedPoIdListByObjectOfReference.isEmpty() || !grantedPoIdListByPoId.isEmpty()) {
            result = true;
        }
        return result;
    }

    @Override
    public Object postProcess(final FMTBACAccessControl accessControlManager, final Object response) {
        List<AlarmActionResponse> responseList = (List<AlarmActionResponse>) response;
        if (responseList == null) {
            responseList = new ArrayList<>();
        }
        postProcessActionResponses(responseList);
        return responseList;
    }

    private void postProcessActionResponses(final List<AlarmActionResponse> responseList) {
        final List<String> tempResponsePoIdList = new ArrayList<>();
        for (final AlarmActionResponse alarmActionResponse : responseList) {
            tempResponsePoIdList.add(alarmActionResponse.getEventPoId());
        }
        addToResponse(responseList, filteredAlarmIds, tempResponsePoIdList);
        addToResponse(responseList, filteredEventPoIds, tempResponsePoIdList);
        addToResponse(responseList, filteredObjectOfReference, tempResponsePoIdList);
        LOGGER.debug("FMTBACAlarmActionHandler: response {}  ", responseList);
    }

    private void addPoIdToFilteredList(final List<AlarmActionResponse> filteredList, final Long poId, final Object[] attributes) {
        // Add response to the response List
        final AlarmActionResponse alarmActionResponse = new AlarmActionResponse();
        alarmActionResponse.setResponse(ACCESS_DENIED);
        alarmActionResponse.setEventPoId(poId.toString());
        alarmActionResponse.setObjectOfReference(FMTBACAlarmActionDpsManager.getObjectOfReference(attributes));
        // Not clear if it needed to fill all fields of AlarmActionResponse:
        // in this case we have to extend the FmTbacAlarmActionDpsManager and add the other fields in the executeProjection

        filteredList.add(alarmActionResponse);
    }

    /**
     * Generate granted list of poId and remove not authorized from ActionData
     * @param actionData
     *            input action data. not granted poIds will be removed
     * @param alarmListAttributes
     *            list of couple poId, fdn
     * @return list of granted poId
     */
    private List<Long> authorizeAlarmList(final AlarmActionData actionData, final List<Object[]> alarmListAttributes) {
        final List<Long> grantedPoIdList = new ArrayList<>();

        for (final Object[] alarmAttributes : alarmListAttributes) {
            final Long poId = FMTBACAlarmActionDpsManager.getPoId(alarmAttributes);
            final String fdn = FMTBACAlarmActionDpsManager.getFdn(alarmAttributes);
            final String targetName = FMTBACAlarmActionDpsManager.getTargetName(alarmAttributes);
            LOGGER.trace("Current PO data : poId {} fdn {} targetName {} ", poId, fdn, targetName);

            if (!isAuthorized(targetName)) {
                Boolean addedToFilteredList = false;
                LOGGER.debug("Current PO with targetName {} is not authorized :", targetName);
                if (actionData.getAlarmIds() != null) {
                    final boolean resultRemoving = actionData.getAlarmIds().remove(poId);
                    LOGGER.trace("Poid removed action: {} from AlarmIds list ", resultRemoving, poId);
                    if (resultRemoving) {
                        addedToFilteredList = true;
                        addPoIdToFilteredList(filteredAlarmIds, poId, alarmAttributes);
                    }
                }
                if (actionData.getPoIds() != null) {
                    final boolean resultRemoving = actionData.getPoIds().remove(poId);
                    LOGGER.trace("Poid removed action: {} from EventPoIds list ", resultRemoving, poId);
                    if (resultRemoving) {
                        addedToFilteredList = true;
                        addPoIdToFilteredList(filteredEventPoIds, poId, alarmAttributes);
                    }
                }
                if (!addedToFilteredList) {
                    LOGGER.trace("Poid {} only from get object of reference", poId);
                    addPoIdToFilteredList(filteredObjectOfReference, poId, alarmAttributes);
                }
            } else {
                grantedPoIdList.add(poId);
            }
        }
        return grantedPoIdList;
    }

    private Boolean isAuthorized(final String targetName) {
        if (authorizedMapByTarget.get(targetName) == null) {
            authorizedMapByTarget.put(targetName, accessControlManager.isAuthorized(targetName));
        }
        return authorizedMapByTarget.get(targetName);
    }

    private void addToResponse(final List<AlarmActionResponse> responseList, final List<AlarmActionResponse> filteredPoIdList,
            final List<String> referencePoIdList) {
        for (final AlarmActionResponse alarmActionResponse : filteredPoIdList) {
            if (!referencePoIdList.contains(alarmActionResponse.getEventPoId())) {
                responseList.add(alarmActionResponse);
                referencePoIdList.add(alarmActionResponse.getEventPoId());
            }
        }
    }
}
