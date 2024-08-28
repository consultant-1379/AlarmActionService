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

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

/**
 * Helper class for performing database operation in a new transaction.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class DpsAccessTransactionalFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(DpsAccessTransactionalFacade.class);

    @Inject
    private DpsUtil dpsUtil;

    public List<Long> getPoIds(final Integer age, final String qualifier) {
        LOGGER.debug("A separate transaction is started to get the PoIds of {}s older than the age {}", qualifier, age);
        return dpsUtil.getPoIds(age, qualifier);
    }
}
