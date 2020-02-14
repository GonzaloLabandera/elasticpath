/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.executors;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.MAX_ALLOWED_LIST_PARAMETERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
	@Mock private Persistable persistable;
	private List<Persistable> persistables;

	@Before
	public void init() {
		persistables = Lists.newArrayList(persistable);

		namedQueryWithListExecutor
			.withQueryName(NAMED_QUERY);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

	}

	/**
	 * Test whether a query is executed with a map with parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMapParameters() {

		when(query.getResultList()).thenReturn(persistables);

		List<String> paramList1 = Lists.newArrayList("val1", "val2");

		String listParam = "param1";

		Map<String, Collection<String>> mapParams = Maps.newHashMap();
		mapParams.put(listParam, paramList1);

		List result = namedQueryWithListExecutor
			.withParameters(mapParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameter(listParam, paramList1);

		verify(query, never()).setFirstResult(anyInt());
		verify(query, never()).setMaxResults(anyInt());
		verify(query, never()).setParameters((Object[]) null);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a first and max results as boundaries.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFirstAndMaxResults() {

		when(query.getResultList()).thenReturn(persistables);

		int fistResult = 1;
		int maxResults = 1;
		String listParam = "param1";
		Object[] arrayParams = {"param1value"};

		List<String> listValues = Lists.newArrayList("1", "2");

		List result = namedQueryWithListExecutor
			.withFirstResult(fistResult)
			.withMaxResults(maxResults)
			.withParameters(arrayParams)
			.withParameterValues(listValues)
			.withListParameterName(listParam)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameter(listParam, listValues);
		verify(query).setFirstResult(fistResult);
		verify(query).setMaxResults(maxResults);
		verify(query).setParameters(arrayParams);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed in batches.
	 */
	@Test
	public void shouldExecuteMultiResultQueryInBatches() {

		String listParam = "listParam";
		Object[] arrayParams = {"param1val"};

		List<Integer> listValues = new ArrayList<>();

		//we need at list 2 chunks - to create the first one, MAX_ALLOWED_LIST_PARAMETERS elements are required
		for (int i = 1; i <= MAX_ALLOWED_LIST_PARAMETERS + 1; i++) {
			listValues.add(i);
		}

		Persistable customEntity1 = new CustomEntityImpl();
		Persistable customEntity2 = new CustomEntityImpl();

		List resultList1 = Lists.newArrayList(customEntity1);
		List resultList2 = Lists.newArrayList(customEntity2);

		when(query.getResultList()).thenReturn(resultList1, resultList2);

		List result = namedQueryWithListExecutor
			.withParameters(arrayParams)
			.withParameterValues(listValues)
			.withListParameterName(listParam)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(customEntity1, customEntity2);

		verify(query, times(2)).getResultList();
		verify(query, times(2)).setParameters(arrayParams);
		verify(query, times(2)).setParameter(eq(listParam), anyCollection());
		verify(query, never()).setFirstResult(anyInt());
		verify(query, never()).setMaxResults(anyInt());

		verifyNoMoreInteractions(query);
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
