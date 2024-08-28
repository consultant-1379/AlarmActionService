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
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT_PURGER_BATCH_SIZE;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.Projection;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.ProjectionBuilder;
import com.ericsson.oss.services.fm.alarmactionservice.cluster.AlarmActionServiceClusterListener;
import com.ericsson.oss.services.fm.alarmactionservice.configuration.ConfigurationsChangeListener;
import com.ericsson.oss.services.fm.alarmactionservice.impl.CommentPurger;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

/**
 * Bean responsible for starting the timer for purging of Comment Operation PO of the associated Open Alarm PO which has been removed from the DB.
 */
// TODO: Timer runs in transaction. There is possibility of rollback exception if there are many POs to be acted upon.So timer needs to be executed
// without transaction.
@Singleton
@Startup
public class CommentPurgingTimerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentPurgingTimerService.class);

    @Resource
    private TimerService service;

    @Inject
    private ConfigurationsChangeListener configurationsChangeListener;

    @Inject
    private DpsUtil dpsUtil;

    @Inject
    private CommentPurger commentPurger;

    @Inject
    private AlarmActionServiceClusterListener alarmActionServiceClusterListener;

    private Timer commentPurgingTimer;

    @PostConstruct
    public void init() {
        startAlarmTimer(configurationsChangeListener.getCommentPurgingTimerInterval());
    }

    public void processCommentPurgingTimerChangeEvent(final int commentPurgingInterval) {
        commentPurgingTimer.cancel();
        startAlarmTimer(commentPurgingInterval);
    }

    public void startAlarmTimer(final Integer checkInterval) {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);

        commentPurgingTimer = service.createIntervalTimer((long) checkInterval * 60 * 1000, (long) checkInterval * 60 * 1000, timerConfig);
        LOGGER.info("Timer for CommentOperation PO purging is started with {} ", checkInterval * 60 * 1000);
    }

    @Timeout
    public void handleTimeout(final Timer timer) {
        if (alarmActionServiceClusterListener.getMasterState()) {
            final DataBucket liveBucket = dpsUtil.getLiveBucket();
            final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
            final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, COMMENT_OPERATION);
            final Projection poIdProjection = ProjectionBuilder.field(ObjectField.PO_ID);

            final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
            final List<Long> commentOperationPoIds = queryExecutor.executeProjection(typeQuery, poIdProjection);

            if (commentOperationPoIds != null) {
                final List<List<Long>> commentOperationPoIdsBatches = new ArrayList<List<Long>>();
                for (int counter = 0; counter < commentOperationPoIds.size(); counter += COMMENT_PURGER_BATCH_SIZE) {
                    commentOperationPoIdsBatches.add(commentOperationPoIds.subList(counter,
                            counter + Math.min(COMMENT_PURGER_BATCH_SIZE, commentOperationPoIds.size() - counter)));
                }
                for (final List<Long> commentOperationPoIdsBatch : commentOperationPoIdsBatches) {
                    LOGGER.debug("The commentOperationPOIdsBatch batch size is {} and COMMENT_PURGER_BATCH_SIZE is {} ",
                            commentOperationPoIdsBatch.size(), COMMENT_PURGER_BATCH_SIZE);
                    commentPurger.purgeComments(liveBucket, commentOperationPoIds);
                }
            }
        }
    }
}
