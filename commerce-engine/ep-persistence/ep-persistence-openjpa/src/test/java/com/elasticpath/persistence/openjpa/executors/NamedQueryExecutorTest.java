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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.Persistable;

/**
 * Unit test for the {@code NamedQueryExecutor} class.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class NamedQueryExecutorTest {

	private static final String NAMED_QUERY = "NAMED_QUERY";

	@InjectMocks private NamedQueryExecutor namedQueryExecutor;

	@Mock private EntityManager entityManager;
	@Mock private OpenJPAQuery query;
	@Mock private Persistable persistable;
	private List<Persistable> persistables;

	@Before
	public void init() {
		persistables = Lists.newArrayList(persistable);

		namedQueryExecutor
			.withQueryName(NAMED_QUERY);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);
	}

	/**
	 * Test whether a query is executed with an array of parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithArrayParameters() {

		when(query.getResultList()).thenReturn(persistables);

		Object[] arrayParams = {"1", "2"};

		List result = namedQueryExecutor
			.withParameters(arrayParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameters(arrayParams);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a map of parameters.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMapParameters() {

		when(query.getResultList()).thenReturn(persistables);

		Map<String, String> mapParams = Maps.newHashMap();
		mapParams.put("param1", "val1");
		mapParams.put("param2", "val2");

		List result = namedQueryExecutor
			.withParameters(mapParams)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setParameters(mapParams);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a flush mode.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFlushMode() {

		when(query.getResultList()).thenReturn(persistables);

		FlushMode flushMode = FlushMode.AUTO;
		FlushModeType flushModeType = FlushModeType.AUTO;

		List result = namedQueryExecutor
			.withFlushMode(flushMode)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setFlushMode(flushModeType);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with first result boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFirstResult() {

		when(query.getResultList()).thenReturn(persistables);

		int fistResult = 1;

		List result = namedQueryExecutor
			.withFirstResult(fistResult)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setFirstResult(fistResult);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with max results boundary.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithMaxResults() {

		when(query.getResultList()).thenReturn(persistables);

		int maxResults = 1;

		List result = namedQueryExecutor
			.withMaxResults(maxResults)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setMaxResults(maxResults);

		verifyNoMoreInteractions(query);
	}

	/**
	 * Test whether a query is executed with a flush mode.
	 */
	@Test
	public void shouldExecuteMultiResultQueryWithFlushModeAndIgnoreChanges() {

		boolean ignoreChanges = true;

		when(query.getResultList()).thenReturn(persistables);

		List result = namedQueryExecutor
			.withFlushMode(FlushMode.COMMIT)
			.withIgnoreChanges(ignoreChanges)
			.executeMultiResultQuery(entityManager);

		assertThat(result)
			.contains(persistable);

		verify(query).getResultList();
		verify(query).setIgnoreChanges(ignoreChanges);
		verify(query).setFlushMode(FlushModeType.COMMIT);

		verifyNoMoreInteractions(query);
	}
}
