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
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTS;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTEXT;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENTTIME;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.COMMENT_OPERATION;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.FM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.INDEX;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPEN_ALARM_POID;
import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.VERSION;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

/**
 * HistoryCommentRecorder is responsible for storing each comment to CommentOperation PO. If the Comment Operation PO is not present for the open
 * alarm PO ID, then a new Comment Operation PO is created and first comment for Open Alarm is stored in the Comment Operation PO. For storing further
 * comments for same Open Alarm PO ID the Comment Operation PO gets updated. The Comment Operation PO is used to show history of comments in Alarm
 * Detail APP.
 */
public class CommentHistoryRecorder {

    @Inject
    private DpsUtil dpsUtil;

    public void storeCommentHistory(final String comment, final String operatorName, final Date lastUpdated,
                                    final PersistenceObject openAlarmPersistentObject) {
        final Long openAlarmPoId = openAlarmPersistentObject.getPoId();
        final QueryBuilder queryBuilder = dpsUtil.getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(FM, COMMENT_OPERATION);
        final Restriction poIdRestriction = typeQuery.getRestrictionBuilder().equalTo(OPEN_ALARM_POID, openAlarmPoId);
        typeQuery.setRestriction(poIdRestriction);

        final QueryExecutor queryExecutor = dpsUtil.getLiveBucket().getQueryExecutor();
        final Iterator<PersistenceObject> commentOperationPoIterator = queryExecutor.execute(typeQuery);

        if (!commentOperationPoIterator.hasNext()) {
            final PersistenceObject commentOperationPo = createCommentPersistentObject(comment, operatorName, lastUpdated, openAlarmPoId);
            commentOperationPo.addAssociation(OPENALARM, openAlarmPersistentObject);
        } else {
            while (commentOperationPoIterator.hasNext()) {
                final PersistenceObject commentOperationPo = commentOperationPoIterator.next();
                updateCommentPersistentObject(comment, operatorName, lastUpdated, commentOperationPo);
            }
        }
    }

    private PersistenceObject createCommentPersistentObject(final String comment, final String operatorName, final Date lastUpdated,
                                                            final Long openAlarmPoId) {
        final Map<String, Object> commentMap = new HashMap<>(4);
        commentMap.put(COMMENTOPERATOR, operatorName);
        commentMap.put(COMMENTTEXT, comment);
        commentMap.put(COMMENTTIME, lastUpdated);
        commentMap.put(INDEX, 1L);

        final List<Map<String, Object>> historyComments = new ArrayList<Map<String, Object>>(1);
        historyComments.add(commentMap);

        final Map<String, Object> commentOperationAttributes = new HashMap<String, Object>(2);
        commentOperationAttributes.put(OPEN_ALARM_POID, openAlarmPoId);
        commentOperationAttributes.put(COMMENTS, historyComments);

        final PersistenceObject historyCommentPo = dpsUtil.getLiveBucket().getPersistenceObjectBuilder().namespace(FM).type(COMMENT_OPERATION)
                .version(VERSION).addAttributes(commentOperationAttributes).create();
        return historyCommentPo;
    }

    private void updateCommentPersistentObject(final String commentText, final String commentOperator, final Date commentTime,
                                 final PersistenceObject commentOperationPo) {
        final List<Map<String, Object>> historyComments = commentOperationPo.getAttribute(COMMENTS);

        final Map<String, Object> lastUpdatedComment = historyComments.get(historyComments.size() - 1);
        final long lastIndex = (long) lastUpdatedComment.get(INDEX);

        final Map<String, Object> commentMap = new HashMap<String, Object>(4);
        commentMap.put(INDEX, lastIndex + 1L);
        commentMap.put(COMMENTOPERATOR, commentOperator);
        commentMap.put(COMMENTTEXT, commentText);
        commentMap.put(COMMENTTIME, commentTime);

        historyComments.add(commentMap);

        commentOperationPo.setAttribute(COMMENTS, historyComments);
    }
}
