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

package com.ericsson.oss.services.fm.alarmactionservice.cache;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.ALARM_ACTIONS_CACHE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.HASH_DELIMITER;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.MAX_READ_ENTRIES_FROM_CACHE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.cache.classic.CacheProviderBean;
import com.ericsson.oss.itpf.sdk.cache.infinispan.producer.CacheEntryIterator;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

/**
 * Wrapper class for reading data from or writing data or remove data from AlarmActionsCache.
 */
@ApplicationScoped
public class AlarmActionsCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmActionsCacheManager.class);

    @Inject
    private SystemRecorder systemRecorder;

    private Cache<String, AlarmActionInformation> alarmActionsCache;

    private CacheProviderBean cacheProviderBean;

    public void put(final AlarmActionInformation alarmActionInformation) {
        try {
            getCache().put(prepareKey(alarmActionInformation), alarmActionInformation);
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while addding alarm action in cache: ", exception);
        }
    }

    public boolean remove(final AlarmActionInformation alarmActionInformation) {
        try {
            return getCache().remove(prepareKey(alarmActionInformation));
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while removing alarm action from cache: ", exception);
        }
        return false;
    }

    public AlarmActionInformation get(final AlarmActionInformation alarmActionInformation) {
        try {
            return getCache().get(prepareKey(alarmActionInformation));
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while fetching alarm action from cache: ", exception);
        }
        return null;
    }

    public void removeAll(final List<AlarmActionInformation> alarmActionInformations) {
        for (final AlarmActionInformation alarmAction : alarmActionInformations) {
            remove(alarmAction);
        }
    }

	public Map<String, AlarmActionInformation> readFailedAlarmActionsFromCache(final List<String> failedJbossInstances) {
        final Map<String, AlarmActionInformation> alarmActionRecords = new HashMap<String, AlarmActionInformation>();
        CacheEntryIterator<String, AlarmActionInformation> cacheIterator = null;
        try {
            int i = 0;
            final long start = System.currentTimeMillis();
            cacheIterator = (CacheEntryIterator) getCache().iterator();
            while (cacheIterator.hasNext()) {
                final Entry<String, AlarmActionInformation> entry = cacheIterator.next();
                if (i >= MAX_READ_ENTRIES_FROM_CACHE) {
                    break;
                }
                final AlarmActionInformation alarmActionInformation = entry.getValue();
                if (failedJbossInstances.contains(alarmActionInformation.getJbossNodeId())) {
                    alarmActionRecords.put(entry.getKey(), alarmActionInformation);
                }
                ++i;
            }
            final long end = System.currentTimeMillis();
            LOGGER.trace("Total alarm actions fetched from Cache. {} and total time taken(ms) :{}", i, end - start);
        } catch (final Exception exception) {
            LOGGER.error("Exception in reading alarm action information from AlarmActionsCache : ", exception);
        }finally {
            if(cacheIterator != null)
               cacheIterator.close();
        }

        return alarmActionRecords;
    }

    private Cache<String, AlarmActionInformation> getCache() {
        if (alarmActionsCache == null) {
            if (cacheProviderBean == null) {
                cacheProviderBean = new CacheProviderBean();
            }
            alarmActionsCache = cacheProviderBean.createOrGetModeledCache(ALARM_ACTIONS_CACHE);
            systemRecorder.recordEvent("Cache Initialization", EventLevel.DETAILED, ALARM_ACTIONS_CACHE,
                    "AlarmActionsCache initialization successfully completed.", "");
        }
        return alarmActionsCache;
    }

    private String prepareKey(final AlarmActionInformation alarmActionInformation) {
        final String operatorName = alarmActionInformation.getOperatorName();
        final Long poId = alarmActionInformation.getPoId();
        final String alarmAction = alarmActionInformation.getAlarmAction();
        return new StringBuilder("").append(operatorName).append(HASH_DELIMITER).append(alarmAction).append(HASH_DELIMITER).append(poId).toString();
    }
}
