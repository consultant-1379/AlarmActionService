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

package com.ericsson.oss.services.fm.alarmactionservice.timer;

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT_OPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Timer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.Projection;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.ProjectionBuilder;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.impl.CommentPurger;
import com.ericsson.oss.services.fm.alarmactionservice.timer.CommentPurgingTimerService;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

@RunWith(MockitoJUnitRunner.class)
public class CommentPurgingTimerServiceTest {

    @InjectMocks
    CommentPurgingTimerService commentPurgingTimerService;

    @Mock
    private ConfigurationsChangeListener configurationsChangeListener;

    @Mock
    private DpsUtil dpsUtil;

    @Mock
    private CommentPurger commentPurger;

    @Mock
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private Query<TypeRestrictionBuilder> typeQuery;

    @Mock
    private Projection poIdProjection;

    @Mock
    private ProjectionBuilder projectionBuilder;

    final List<Long> poIds = new ArrayList<Long>();

    @Before
    public void setUpForDatabase() {
        final List<Object> poIdsList = new ArrayList<Object>();
        poIdsList.add(123L);
        when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
        when(dpsUtil.getQueryBuilder()).thenReturn(queryBuilder);
        when(queryBuilder.createTypeQuery(FM, COMMENT_OPERATION)).thenReturn(typeQuery);
        when(liveBucket.getQueryExecutor()).thenReturn(queryExecutor);
        when(queryExecutor.executeProjection((Query<TypeRestrictionBuilder>) Mockito.anyObject(), (Projection) Mockito.anyObject())).thenReturn(
                poIdsList);
    }

    @Test
    public void testHandleTimeOut() {

        final Timer timer = null;
        poIds.add(123L);
        when(alarmActionServiceClusterListener.getMasterState()).thenReturn(true);
        commentPurgingTimerService.handleTimeout(timer);
        verify(commentPurger, VerificationModeFactory.times(1)).purgeComments(liveBucket, poIds);
    }

}
