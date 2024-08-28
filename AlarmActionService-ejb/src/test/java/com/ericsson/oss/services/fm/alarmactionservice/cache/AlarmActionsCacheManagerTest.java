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

package com.ericsson.oss.services.fm.alarmactionservice.cache;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import com.ericsson.oss.itpf.sdk.cache.infinispan.producer.CacheEntryIterator;
import java.util.List;

import javax.cache.Cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.when;

import com.ericsson.oss.itpf.sdk.cache.classic.CacheProviderBean;
import com.ericsson.oss.services.fm.models.AlarmActionInformation;

@RunWith(MockitoJUnitRunner.class)
public class AlarmActionsCacheManagerTest {

    @InjectMocks
    private AlarmActionsCacheManager alarmActionsCacheManager;

    @Mock
    private Cache<String, AlarmActionInformation> alarmActionsCache;

    @Mock
    private CacheProviderBean cacheProviderBean;

    @Mock
    private CacheEntryIterator<String, AlarmActionInformation> cacheIterator;

    @Mock
    private javax.cache.Cache.Entry<String, AlarmActionInformation> entry;

    @Test
    public void test() {
        final AlarmActionInformation actionInformation = new AlarmActionInformation();
        actionInformation.setAlarmAction("ACK");
        actionInformation.setOperatorName("Operator");
        actionInformation.setPoId(12345L);
        actionInformation.setAlarmAttributes(new HashMap<String, Object>());
        alarmActionsCacheManager.put(actionInformation);
        alarmActionsCacheManager.get(actionInformation);
        alarmActionsCacheManager.remove(actionInformation);
        final List<AlarmActionInformation> alarmActionInformations = new ArrayList<AlarmActionInformation>();
        alarmActionInformations.add(actionInformation);
        alarmActionsCacheManager.removeAll(alarmActionInformations);
    }

    @Test
    public void readFailedAlarmActionsFromCache_test() {
        final List<String> failedJbossInstances = new ArrayList<String>();
        failedJbossInstances.add("svc-2-fmhistory");
        final AlarmActionInformation actionInformation = new AlarmActionInformation();
        actionInformation.setAlarmAction("ACK");
        actionInformation.setOperatorName("Operator");
        actionInformation.setPoId(12345L);
        actionInformation.setAlarmAttributes(new HashMap<String, Object>());
        actionInformation.setJbossNodeId("svc-2-fmhistory");
        when(alarmActionsCache.iterator()).thenReturn(cacheIterator);
        when(cacheIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(cacheIterator.next()).thenReturn(entry);
        when(entry.getValue()).thenReturn(actionInformation);
        alarmActionsCacheManager.put(actionInformation);
        assertNotNull(alarmActionsCacheManager.readFailedAlarmActionsFromCache(failedJbossInstances));

    }
}
