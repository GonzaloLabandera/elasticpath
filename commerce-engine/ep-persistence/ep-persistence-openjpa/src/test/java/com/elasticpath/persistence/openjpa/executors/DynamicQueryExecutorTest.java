/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
	@Mock private Persistable persistable;
	private List<Persistable> persistables;

	@Before
	public void init() {
		persistables = Lists.newArrayList(persistable);

		dynamicQueryExecutor
			.withQueryString(QUERY);

		when(entityManager.createQuery(QUERY)).thenReturn(query);
	}

	/**
	 * Test whether a query is executed with a given list parameter and the values.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithListParameter() {

		when(query.getResultList()).thenReturn(persistables);

		List<String> listValues = Lists.newArrayList("1", "2");
		String listParameterName = "list";

		List result = dynamicQueryExecutor
			.withListParameterName(listParameterName)
			.withParameterValues(listValues)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameter(listParameterName, listValues);

		verify(query, never()).setFirstResult(anyInt());
		verify(query, never()).setMaxResults(anyInt());
		verify(query, never()).setHint(anyString(), any());
		verify(query, never()).setParameters((Map) null);
		verify(query, never()).setParameters((Object[]) null);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with an array of parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithArrayParameters() {

		when(query.getResultList()).thenReturn(persistables);

		Object[] arrayParams = {"1", "2"};

		List result = dynamicQueryExecutor
			.withParameters(arrayParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameters(arrayParams);

		verify(query, never()).setParameter(anyString(), anyCollection());
		verify(query, never()).setFirstResult(anyInt());
		verify(query, never()).setMaxResults(anyInt());
		verify(query, never()).setHint(anyString(), any());
		verify(query, never()).setParameters((Map) null);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a map with parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMapParameters() {

		when(query.getResultList()).thenReturn(persistables);

		Map<String, String> mapParams = Maps.newHashMap();
		mapParams.put("param1", "val1");
		mapParams.put("param2", "val2");

		List result = dynamicQueryExecutor
			.withParameters(mapParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameters(mapParams);

		verify(query, never()).setParameter(anyString(), anyCollection());
		verify(query, never()).setFirstResult(anyInt());
		verify(query, never()).setMaxResults(anyInt());
		verify(query, never()).setHint(anyString(), any());
		verify(query, never()).setParameters((Object[]) null);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with first result boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFirstResult() {

		when(query.getResultList()).thenReturn(persistables);

		int fistResult = 1;

		List result = dynamicQueryExecutor
			.withFirstResult(fistResult)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setFirstResult(fistResult);

		verify(query, never()).setParameter(anyString(), anyCollection());
		verify(query, never()).setMaxResults(anyInt());
		verify(query, never()).setHint(anyString(), any());
		verify(query, never()).setParameters((Map) null);
		verify(query, never()).setParameters((Object[]) null);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with max results boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMaxResults() {

		when(query.getResultList()).thenReturn(persistables);

		int maxResults = 1;

		List result = dynamicQueryExecutor
			.withMaxResults(maxResults)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setMaxResults(maxResults);
		verify(query).setHint("openjpa.hint.OracleSelectHint", "/*+ first_rows(" + maxResults + ") */");

		verify(query, never()).setParameter(anyString(), anyCollection());
		verify(query, never()).setFirstResult(anyInt());
		verify(query, never()).setParameters((Map) null);
		verify(query, never()).setParameters((Object[]) null);

		verifyNoMoreInteractions(query);
	}
}
