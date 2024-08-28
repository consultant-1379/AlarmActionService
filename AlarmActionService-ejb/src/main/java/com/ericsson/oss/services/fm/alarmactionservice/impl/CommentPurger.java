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

import static com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants.OPENALARM;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;

/**
 * Bean responsible for removing the comment operation persistent objects.
 */
@Stateless
public class CommentPurger {

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void purgeComments(final DataBucket liveBucket, final List<Long> commentPoIds) {
        final List<PersistenceObject> commentOperationPos = liveBucket.findPosByIds(commentPoIds);
        for (final PersistenceObject commentOperationPo : commentOperationPos) {
            final Collection<PersistenceObject> openAlarm = commentOperationPo.getAssociations(OPENALARM);

            if (openAlarm == null || openAlarm.isEmpty()) {
                liveBucket.deletePo(commentOperationPo);
            }
        }
    }
}
