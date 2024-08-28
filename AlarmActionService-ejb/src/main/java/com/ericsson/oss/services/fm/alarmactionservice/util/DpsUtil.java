/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.fm.alarmactionservice.util;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.CEASETIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ERROR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.EVENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FMFUNCTION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.RECORDTYPE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.REPEATED_ERROR;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.RestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;

/**
 * This class is DPS helper class which provides LiveBucket and queryBuilder to all the classes in Alarm Action Service.
 **/
@ApplicationScoped
public class DpsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DpsUtil.class);

    @EServiceRef
    private DataPersistenceService dataPersistenceService;

    public DataBucket getLiveBucket() {
        final DataBucket liveBucket = dataPersistenceService.getLiveBucket();
        return liveBucket;
    }

    public QueryBuilder getQueryBuilder() {
        final QueryBuilder queryBuilder = dataPersistenceService.getQueryBuilder();
        return queryBuilder;
    }

    /**
     * Fetches all the PoIds of the Alarms/Events created in FM before the given age.
     *
     * @param age
     *            - Age of the Alarm/Event (i.e: Number of hours ago that the Alarm/Event is created in FM)
     * @param qualifier
     *            - Qualifier to differentiate between ALARM and EVENT.
     */
    public List<Long> getPoIds(final Integer age, final String qualifier) {
        Restriction finalRestriction = null;
        Restriction restrictTime = null;
        Restriction restrictRecordType = null;
        final QueryBuilder queryBuilder = getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, OPENALARM);
        final RestrictionBuilder restrictionBuilder = typeQuery.getRestrictionBuilder();
        // current time
        final Date currentTime = new Date();
        // operation time is age hours before the current time
        final Date operationTime = new Date(currentTime.getTime() - 3600000 * age);
        dataPersistenceService.setWriteAccess(false);
        if (EVENT.equalsIgnoreCase(qualifier)) {
            restrictTime = typeQuery.getRestrictionBuilder().lessThan(EVENTTIME, operationTime);
            final Restriction restrictError = typeQuery.getRestrictionBuilder().equalTo(RECORDTYPE, ERROR);
            final Restriction restrictRepeatError = typeQuery.getRestrictionBuilder().equalTo(RECORDTYPE, REPEATED_ERROR);
            restrictRecordType = restrictionBuilder.anyOf(restrictError, restrictRepeatError);
            finalRestriction = restrictionBuilder.allOf(restrictRecordType, restrictTime);
        } else {
            restrictTime = typeQuery.getRestrictionBuilder().lessThan(CEASETIME, operationTime);
            finalRestriction = restrictTime;
        }

        typeQuery.setRestriction(finalRestriction);
        final QueryExecutor queryExecutor = getLiveBucket().getQueryExecutor();
        final Iterator<PersistenceObject> poListIterator = queryExecutor.execute(typeQuery);
        final List<Long> poIdList = new ArrayList<Long>();

        while (poListIterator.hasNext()) {
            final PersistenceObject po = poListIterator.next();
            final Long eventPoId = po.getPoId();
            poIdList.add(eventPoId);
        }
        return poIdList;
    }

    public void updateFmFunction(final String fdn, final String attribute) {
        final String fmFunctionFdn = fdn.concat(FMFUNCTION);
        final ManagedObject fmFunctionMo = getLiveBucket().findMoByFdn(fmFunctionFdn);
        if (fmFunctionMo != null) {
            fmFunctionMo.setAttribute(attribute, false);
            LOGGER.debug("{} is cleared for the node {} ", attribute, fdn);
        } else {
            LOGGER.debug("FmFunction Mo is not found for the node {} ", fdn);
        }
    }
}
