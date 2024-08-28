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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.*;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants;
import com.ericsson.oss.services.fm.alarmactionservice.util.DpsUtil;

@RunWith(MockitoJUnitRunner.class)
public class DpsUtilTest
{
	
	
	@InjectMocks
	DpsUtil dpsUtil;
	
	@Mock
	DataPersistenceService service;
	
	@Mock
	DataBucket liveBucket;
	
	@Mock
	QueryBuilder queryBuilder;
	
	@Mock
	Query<TypeRestrictionBuilder> typeQuery;
	
	@Mock
	TypeRestrictionBuilder typeRestrictionBuilder;
	
	@Mock
	Restriction restriction;
	
	@Mock
	RestrictionBuilder restrictionBuilder;
	
	@Mock
	QueryExecutor queryExecutor;
	
	@Mock
	PersistenceObject persistenceObject;
	
	@Test
	public void testLiveBucket() 
	{
		
		when(service.getLiveBucket()).thenReturn(liveBucket);
		assertNotNull(dpsUtil.getLiveBucket());
	}

	@Test
	public void testQueryBuilder() 
	{
		when(service.getQueryBuilder()).thenReturn(queryBuilder);
		assertNotNull(dpsUtil.getQueryBuilder());
	}
	
	@Test
	public void testGetPoIdList() 
	{
		Iterator<Object> poListIterator=mock(Iterator.class);
		String qualifier = "EVENT";
		when(service.getQueryBuilder()).thenReturn(queryBuilder);
		when(queryBuilder.createTypeQuery(AlarmActionConstants.FM, AlarmActionConstants.OPENALARM)).thenReturn(typeQuery);
		when(typeQuery.getRestrictionBuilder()).thenReturn(typeRestrictionBuilder);
		final Date currentTime = new Date(); 
		final Date operationTime = new Date(currentTime.getTime() - TimeUnit.HOURS.toMillis(1));
		when(typeRestrictionBuilder.lessThan("alarmState", operationTime.getTime())).thenReturn(restriction);
		when(typeQuery.getRestrictionBuilder().equalTo("recordType",AlarmActionConstants.ERROR)).thenReturn(restriction);
		when( typeQuery.getRestrictionBuilder().equalTo("recordType", AlarmActionConstants.REPEATED_ERROR)).thenReturn(restriction);
		when( restrictionBuilder.anyOf(restriction,restriction)).thenReturn(restriction);
		when( restrictionBuilder.allOf(restriction,restriction)).thenReturn(restriction);
		when(dpsUtil.getLiveBucket()).thenReturn(liveBucket);
		when(dpsUtil.getLiveBucket().getQueryExecutor()).thenReturn(queryExecutor);
		when(queryExecutor.execute(typeQuery)).thenReturn(poListIterator);
		when(poListIterator.hasNext()).thenReturn(true,true,false);
		when(poListIterator.next()).thenReturn(persistenceObject);
		when(persistenceObject.getPoId()).thenReturn((long)1234);
		dpsUtil.getPoIds(2, qualifier);
		
		String qualifier1 = "ALARM";
		dpsUtil.getPoIds(2, qualifier1);
		assertNotNull(dpsUtil.getQueryBuilder());
	}
}
