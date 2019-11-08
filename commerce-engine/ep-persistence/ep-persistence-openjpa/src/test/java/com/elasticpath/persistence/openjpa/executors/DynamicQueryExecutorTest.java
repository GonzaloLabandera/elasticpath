/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * Unit test for the {@code DynamicQueryExecutor} class.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class DynamicQueryExecutorTest {

	private static final String QUERY = "SELECT c FROM CustomEntityImpl c WHERE UIDPK IN (:list)";

	@InjectMocks private DynamicQueryExecutor dynamicQueryExecutor;

	@Mock private EntityManager entityManager;
	@Mock private OpenJPAQuery query;
	@Mock private QueryUtil queryUtil;
	@Mock private List persistables;


	@Before
	public void init() {
		dynamicQueryExecutor
			.withQueryString(QUERY);

		when(entityManager.createQuery(QUERY)).thenReturn(query);
	}

	/**
	 * Test whether a query is executed with a given list parameter and the values.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithListParameter() {

		List<String> listValues = Lists.newArrayList("1", "2");
		String csvLstOfValues = "'1','2'";
		String listParameterName = "list";

		Query queryWithParams = mock(Query.class);

		when(queryUtil.getInParameterValues(listValues)).thenReturn(csvLstOfValues);
		when(queryUtil.insertListIntoQuery(query, listParameterName, csvLstOfValues)).thenReturn(queryWithParams);
		when(queryUtil.getResults(queryWithParams)).thenReturn(persistables);

		List result = dynamicQueryExecutor
			.withListParameterName(listParameterName)
			.withParameterValues(listValues)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(queryUtil).getInParameterValues(listValues);
		verify(queryUtil).insertListIntoQuery(query, listParameterName, csvLstOfValues);
		verify(queryUtil).getResults(queryWithParams);
		verify(queryUtil).setQueryParameters(queryWithParams, (Object[]) null);
		verify(queryUtil).setQueryParameters(queryWithParams, (Map) null);
		verifyNoMoreInteractions(query);
		verifyNoMoreInteractions(queryWithParams);
	}

	/**
	 * Test whether a query is executed with an array of parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithArrayParameters() {

		Object[] arrayParams = {"1", "2"};

		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = dynamicQueryExecutor
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
	 * Test whether a query is executed with a map with parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMapParameters() {

		Map<String, String> mapParams = Maps.newHashMap();
		mapParams.put("param1", "val1");
		mapParams.put("param2", "val2");

		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = dynamicQueryExecutor
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
	 * Test whether a query is executed with first result boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFirstResult() {

		int fistResult = 1;
		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = dynamicQueryExecutor
			.withFirstResult(fistResult)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(query).setFirstResult(fistResult);
		verify(queryUtil).getResults(query);
		verify(queryUtil).setQueryParameters(query, (Object[]) null);
		verify(queryUtil).setQueryParameters(query, (Map) null);
		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with max results boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMaxResults() {

		int maxResults = 1;
		when(queryUtil.getResults(query)).thenReturn(persistables);

		List result = dynamicQueryExecutor
			.withMaxResults(maxResults)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(query).setMaxResults(maxResults);
		verify(queryUtil).getResults(query);
		verify(queryUtil).setQueryParameters(query, (Object[]) null);
		verify(queryUtil).setQueryParameters(query, (Map) null);
		verify(query).setHint("openjpa.hint.OracleSelectHint", "/*+ first_rows(" + maxResults + ") */");
		verifyNoMoreInteractions(query);
	}
}
