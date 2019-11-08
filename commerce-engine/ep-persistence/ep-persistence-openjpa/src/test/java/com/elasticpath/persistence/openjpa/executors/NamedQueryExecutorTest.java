/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Maps;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * Unit test for the {@code NamedQueryExecutor} class.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class NamedQueryExecutorTest {

	private static final String NAMED_QUERY = "NAMED_QUERY";

	@InjectMocks private NamedQueryExecutor namedQueryExecutor;

	@Mock private EntityManager entityManager;
	@Mock private Query query;
	@Mock private QueryUtil queryUtil;
	@Mock private List persistables;


	@Before
	public void init() {
		namedQueryExecutor
			.withQueryName(NAMED_QUERY);

		when(queryUtil.createNamedQuery(entityManager, NAMED_QUERY)).thenReturn(query);
	}

	/**
	 * Test whether a query is executed with an array of parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithArrayParameters() {

		Object[] arrayParams = {"1", "2"};

		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = namedQueryExecutor
			.withParameters(arrayParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(queryUtil).setQueryParameters(query, arrayParams);
		verify(queryUtil).setQueryParameters(query, (Map) null);
		verify(queryUtil).getResults(query);
		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a map of parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMapParameters() {

		Map<String, String> mapParams = Maps.newHashMap();
		mapParams.put("param1", "val1");
		mapParams.put("param2", "val2");

		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = namedQueryExecutor
			.withParameters(mapParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(queryUtil).setQueryParameters(query, (Object[]) null);
		verify(queryUtil).setQueryParameters(query, mapParams);
		verify(queryUtil).getResults(query);
		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a flush mode.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFlushMode() {

		FlushMode flushMode = FlushMode.AUTO;
		FlushModeType flushModeType = FlushModeType.AUTO;

		when(queryUtil.getResults(query)).thenReturn(persistables);
		when(queryUtil.toFlushModeType(flushMode)).thenReturn(flushModeType);

		List result = namedQueryExecutor
			.withFlushMode(flushMode)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(queryUtil).setQueryParameters(query, (Object[]) null);
		verify(queryUtil).setQueryParameters(query, (Map) null);
		verify(queryUtil).toFlushModeType(flushMode);
		verify(query).setFlushMode(flushModeType);
		verify(queryUtil).getResults(query);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with first result boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFirstResult() {

		int fistResult = 1;

		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = namedQueryExecutor
			.withFirstResult(fistResult)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(query).setFirstResult(fistResult);
		verify(queryUtil).setQueryParameters(query, (Object[]) null);
		verify(queryUtil).setQueryParameters(query, (Map) null);
		verify(queryUtil).getResults(query);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with max results boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMaxResults() {

		int maxResults = 1;

		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = namedQueryExecutor
			.withMaxResults(maxResults)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(query).setMaxResults(maxResults);
		verify(queryUtil).setQueryParameters(query, (Object[]) null);
		verify(queryUtil).setQueryParameters(query, (Map) null);
		verify(queryUtil).getResults(query);

		verifyNoMoreInteractions(query);
	}
}
