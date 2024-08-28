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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FDN;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OBJECTOFREFERENCE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.ProjectionBuilder;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

/**
 * Class responsible to manage the DPS connections for OpenAlarm access.
 */
@Stateless
public class FMTBACAlarmActionDpsManager {
    private static final Integer PO_ID_POS = 0;
    private static final Integer FDN_POS = 1;
    private static final Integer OBJECTOFREFERENCE_POS = 2;

    @Inject
    private DpsUtil dpsUtil;

    @Inject
    private ConfigurationsChangeListener actionConfigurationListener;

    /**
     * Returns a list of Open Alarms with (restricted) attribute list [PoId, Fdn, ObjectOfReference] by object of reference.
     * @param objectOfReference
     *            the object of reference attribute
     * @return list of Open Alarms with (restricted) attribute list [PoId, Fdn, ObjectOfReference]
     */
    public List<Object[]> getAlarmListWithAttributes(final String objectOfReference) {
        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(OBJECTOFREFERENCE, objectOfReference);

        return getPoIdFdnList(queryBuilder, restriction);
    }

    /**
     * Returns a list of Open Alarms with (restricted) attribute list [PoId, Fdn, ObjectOfReference] by list of poIds Request is divided by batches to
     * avoid to big dps request.
     * @param poIds
     *            the po identifier list
     * @return returns a list of Open Alarms with (restricted) attribute list [PoId, Fdn, ObjectOfReference]
     */
    public List<Object[]> getAlarmListWithAttributes(final List<Long> poIds) {
        final List<Object[]> fdnList = new ArrayList<Object[]>();

        final int batchSize = actionConfigurationListener.getAlarmActionBatchSize();
        final List<List<Long>> poIdBatches = new ArrayList<>();
        // collect batches of po ids...
        for (int counter = 0; counter < poIds.size(); counter += batchSize) {
            poIdBatches.add(poIds.subList(counter, counter + Math.min(batchSize, poIds.size() - counter)));
        }
        // cycle through batches of po ids...
        for (final List<Long> poIdsBatch : poIdBatches) {
            final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
            final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
            final Restriction restriction = typeQuery.getRestrictionBuilder().in(ObjectField.PO_ID, poIdsBatch.toArray());
            fdnList.addAll(getPoIdFdnList(queryBuilder, restriction));
        }
        return fdnList;
    }

    /**
     * Returns a list of Open Alarms with attribute list [PoId, Fdn, ObjectOfReference] from restriction.<br>
     * <br>
     * Position of attributes are defines by the order of projection then: PO_ID_POS = 0; FDN_POS = 1; OBJECTOFREFERENCE_POS = 2;
     */
    private List<Object[]> getPoIdFdnList(final QueryBuilder queryBuilder, final Restriction restriction) {
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        typeQuery.setRestriction(restriction);
        final DataBucket liveBucket = dpsUtil.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();

        final List<Object[]> result = queryExecutor.executeProjection(typeQuery, ProjectionBuilder.field(ObjectField.PO_ID),
                ProjectionBuilder.attribute(FDN), ProjectionBuilder.attribute(OBJECTOFREFERENCE));
        return result;
    }

    /**
     * Utility to get poId from the position 0 of attributes. The position was decided in executeProjection in the getPoIdFdnList method.
     * @param attributes
     *            list of attribute as per executeProjection
     * @return the poId
     */
    public static Long getPoId(final Object[] attributes) {
        if (attributes.length > PO_ID_POS) {
            if (attributes[0] instanceof Long) {
                return (Long) attributes[PO_ID_POS];
            }
        }
        return null;
    }

    /**
     * Utility to get fdn from the position 1 of attributes. The position was decided in executeProjection in the getPoIdFdnList method.
     * @param attributes
     *            list of attribute as per executeProjection
     * @return the fdn
     */
    public static String getFdn(final Object[] attributes) {
        if (attributes.length > FDN_POS) {
            if (attributes[1] instanceof String) {
                return (String) attributes[FDN_POS];
            }
        }
        return null;
    }

    /**
     * Utility to get objectOfReference from the position 2 of attributes. The position was decided in executeProjection in the getPoIdFdnList method.
     * @param attributes
     *            list of attribute as per executeProjection
     * @return the fdn
     */
    public static String getObjectOfReference(final Object[] attributes) {
        if (attributes.length > OBJECTOFREFERENCE_POS) {
            if (attributes[1] instanceof String) {
                return (String) attributes[OBJECTOFREFERENCE_POS];
            }
        }
        return null;
    }

    /**
     * Utility to get target name.
     * @return the target name of the alarm source fdn
     */
    public static String getTargetName(final Object[] attributes) {
        final String fdn = getFdn(attributes);
        if (fdn != null) {
            final int equalIndex = fdn.lastIndexOf('=');
            return fdn.substring(equalIndex + 1);
        }
        return null;
    }

}
