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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.fm.alarmactionservice.impl.CommentPurger;

@RunWith(MockitoJUnitRunner.class)
public class CommentPurgerTest {

    @InjectMocks
    private CommentPurger commentPurger;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private PersistenceObject persistenceObject;

    final List<Long> poIds = new ArrayList<Long>();

    private void setUpForDatabase() {
        poIds.add(123456L);
        poIds.add(456789L);
        final List<PersistenceObject> persistenceObjects = new ArrayList<PersistenceObject>();
        persistenceObjects.add(persistenceObject);
        when(liveBucket.findPosByIds(poIds)).thenReturn(persistenceObjects);
    }

    @Test
    public void testPurgeComments() {

        setUpForDatabase();
        commentPurger.purgeComments(liveBucket, poIds);
        verify(liveBucket, VerificationModeFactory.times(1)).deletePo(persistenceObject);

    }
}
