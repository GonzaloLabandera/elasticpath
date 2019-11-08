/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * Unit test for the {@code NamedQueryExecutor} class.
 */
@SuppressWarnings({"unchecked", "rawtypes", "serial"})
@RunWith(MockitoJUnitRunner.class)
public class NamedQueryExecutorWithListTest {

	private static final String NAMED_QUERY = "NAMED_QUERY";

	@InjectMocks private NamedQueryWithListExecutor namedQueryWithListExecutor;

	@Mock private EntityManager entityManager;
	@Mock private OpenJPAQuery query;
	@Mock private QueryUtil queryUtil;

	@Mock private List persistables;

	@Before
	public void init() {
		namedQueryWithListExecutor
			.withQueryName(NAMED_QUERY);

		when(queryUtil.createNamedQuery(entityManager, NAMED_QUERY)).thenReturn(query);
	}

	/**
	 * Test whether a query is executed with a map with parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithParameters() {

		List<String> paramList1 = Lists.newArrayList("val1", "val2");

		String listParam = "param1";
		String csvParamList = "'val1','val2'";

		OpenJPAQuery configuredQuery = mock(OpenJPAQuery.class);

		Map<String, Collection<String>> mapParams = Maps.newHashMap();
		mapParams.put(listParam, paramList1);

		when(queryUtil.getResults(configuredQuery)).thenReturn(persistables);
		when(queryUtil.getInParameterValues(paramList1)).thenReturn(csvParamList);
		when(queryUtil.insertListIntoQuery(query, listParam, csvParamList)).thenReturn(configuredQuery);

		List result = namedQueryWithListExecutor
			.withParameters(mapParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(queryUtil).getInParameterValues(paramList1);
		verify(queryUtil).insertListIntoQuery(query, listParam, csvParamList);
		verify(queryUtil).getResults(configuredQuery);
		verifyNoMoreInteractions(query);
		verifyNoMoreInteractions(configuredQuery);
	}

	/**
	 * Test whether a query is executed with a first and max results as boundaries.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFirstAndMaxResults() {

		int fistResult = 1;
		int maxResults = 1;
		String listParam = "param1";
		Object[] arrayParams = {listParam};

		List<String> listValues = Lists.newArrayList("1", "2");
		String csvParamList = "'1','2'";

		OpenJPAQuery configuredQuery = mock(OpenJPAQuery.class);

		when(queryUtil.insertListIntoQuery(query, listParam, csvParamList)).thenReturn(configuredQuery);
		when(queryUtil.getInParameterValues(listValues)).thenReturn(csvParamList);
		when(queryUtil.getResults(configuredQuery)).thenReturn(persistables);

		List result = namedQueryWithListExecutor
			.withFirstResult(fistResult)
			.withMaxResults(maxResults)
			.withParameters(arrayParams)
			.withParameterValues(listValues)
			.withListParameterName(listParam)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.isSameAs(persistables);

		verify(queryUtil).getInParameterValues(listValues);
		verify(queryUtil).insertListIntoQuery(query, listParam, csvParamList);
		verify(configuredQuery).setFirstResult(fistResult);
		verify(configuredQuery).setMaxResults(maxResults);
		verify(queryUtil).setQueryParameters(configuredQuery, arrayParams);

		verify(queryUtil).getResults(configuredQuery);
		verifyNoMoreInteractions(query);
		verifyNoMoreInteractions(configuredQuery);
	}

	/**
	 * Test whether a query is executed in batches.
	 */
	@Test
	public void shouldExecuteMultiResultQueryInBatches() {

		String listParam = "listParam";
		Object[] arrayParams = {listParam};

		List<String> listValues = Lists.newArrayList("1", "2");

		Persistable customEntity1 = new CustomEntityImpl();
		Persistable customEntity2 = new CustomEntityImpl();

		List resultList1 = Lists.newArrayList(customEntity1);
		List resultList2 = Lists.newArrayList(customEntity2);

		OpenJPAQuery configuredQuery = mock(OpenJPAQuery.class);

		when(queryUtil.insertListIntoQuery(eq(query), eq(listParam), any())).thenReturn(configuredQuery);
		when(queryUtil.splitCollection(listValues, arrayParams.length)).thenReturn(listValues);
		when(queryUtil.getResults(configuredQuery)).thenReturn(resultList1, resultList2);

		List result = namedQueryWithListExecutor
			.withParameters(arrayParams)
			.withParameterValues(listValues)
			.withListParameterName(listParam)
			.executeMultiResultQuery(entityManager);

		assertThat(result).contains(customEntity1, customEntity2);

		verify(queryUtil).insertListIntoQuery(query, listParam, "1");
		verify(queryUtil).insertListIntoQuery(query, listParam, "2");
		verify(queryUtil, times(2)).setQueryParameters(configuredQuery, arrayParams);
		verify(queryUtil, times(2)).getResults(configuredQuery);
		verifyNoMoreInteractions(query);
		verifyNoMoreInteractions(configuredQuery);
	}

	private class CustomEntityImpl implements Persistable {
		@Override
		public long getUidPk() {
			return 0;
		}

		@Override
		public void setUidPk(final long uidPk) {
			//do nothing
		}

		@Override
		public boolean isPersisted() {
			return false;
		}
	}
}
