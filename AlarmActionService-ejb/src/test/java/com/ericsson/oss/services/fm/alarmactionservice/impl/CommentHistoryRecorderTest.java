/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.fm.alarmactionservice.impl;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTOPERATOR;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INDEX;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.PersistenceObjectBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.services.fm.alarmactionservice.impl.CommentHistoryRecorder;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

@RunWith(MockitoJUnitRunner.class)
public class CommentHistoryRecorderTest {

    @InjectMocks
    private CommentHistoryRecorder commentHistoryRecorder;

    @Mock
    private DataPersistenceService service;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private PersistenceObject persistenceObject;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private Query<TypeRestrictionBuilder> typeQuery;

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private TypeRestrictionBuilder typeRestrictionBuilder;

    @Mock
    private PersistenceObjectBuilder persistenceObjectBuilder;

    @Mock
    private Iterator<Object> poListIterator;

    private void createPO() {
        when(persistenceObjectBuilder.namespace("FM")).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.type("CommentOperation")).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.version("1.0.1")).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.addAttributes(anyMap())).thenReturn(persistenceObjectBuilder);
        when(persistenceObjectBuilder.create()).thenReturn(persistenceObject);
    }

    private void setUpForDataBase() {
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery("FM", "CommentOperation")).thenReturn(typeQuery);
        when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.getPersistenceObjectBuilder()).thenReturn(persistenceObjectBuilder);
    }

    @Test
    public void testStoreCommentHistoryForCreatePO() {

        setUpForDataBase();
        createPO();
        commentHistoryRecorder.storeCommentHistory("commentText", "Admin", new Date(), persistenceObject);
        verify(persistenceObject, times(1)).addAssociation("OpenAlarm", persistenceObject);
    }

    @Test
    public void testStoreCommentHistoryForCreatePO_updatePO() {

        final Date date = new Date();
        final List<Map<String, Object>> historyComments = new ArrayList<Map<String, Object>>();
        final Map<String, Object> lastUpdatedComment = new HashMap<String, Object>();
        lastUpdatedComment.put("index", 1L);
        historyComments.add(lastUpdatedComment);

        final Map<String, Object> commentMap = new HashMap<String, Object>(4);
        commentMap.put(INDEX, 2L);
        commentMap.put(COMMENTOPERATOR, "Admin");
        commentMap.put(COMMENTTEXT, "commentText");
        commentMap.put(COMMENTTIME, date);

        historyComments.add(commentMap);

        setUpForDataBase();
        when(poListIterator.hasNext()).thenReturn(true, true, false);
        when(poListIterator.next()).thenReturn(persistenceObject);
        when(persistenceObject.getAttribute("comments")).thenReturn(historyComments);

        createPO();
        commentHistoryRecorder.storeCommentHistory("commentText", "Admin", date, persistenceObject);
        verify(persistenceObject, times(1)).setAttribute((String) Mockito.any(), Mockito.any());
    }
}
