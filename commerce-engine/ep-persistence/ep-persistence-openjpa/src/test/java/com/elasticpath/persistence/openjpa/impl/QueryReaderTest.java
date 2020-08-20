/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.impl;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.toFlushModeType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.routing.QueryRouter;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;


/**
 * Unit test for the {@code QueryReader} class.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class QueryReaderTest {

	private static final String DYNAMIC_QUERY_STRING = "SELECT c FROM CustomerImpl c";
	private static final String DYNAMIC_QUERY_WITH_LIST_PARAM_STRING = "SELECT c FROM CustomerImpl c WHERE c.uidPk IN (:list)";
	private static final String NAMED_QUERY = "NAMED_QUERY";
	private static final String LIST_PARAM_NAME = "list";

	@InjectMocks private QueryReader queryReader;

	@Mock private EntityManager entityManager;
	@Mock private QueryRouter queryRouter;

	@SuppressWarnings("PMD.UnusedPrivateField")
	@Mock
	private FetchPlanHelper fetchPlanHelper;

	@Mock private OpenJPAQuery query;

	@Before
	public void init() {
		when(queryRouter.getEntityManagerForQuery(any())).thenReturn(entityManager);
	}

	/**
	 * Test loading entity for given class and uidpk.
	 */
	@Test
	public void shouldEntityForGivenClassAndUidPk() {
		Class<Persistable> persistableToFind = Persistable.class;
		long uidPk = 1L;

		when(entityManager.find(persistableToFind, uidPk)).thenReturn(new TestEntity());

		Persistable expected = queryReader.load(persistableToFind, uidPk);

		assertThat(expected).isNotNull();
	}

	/**
	 * Test executing a dynamic query for a given query string and first and max results boundaries.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringAndFirstAndMaxResultsLimits() {
		int firstResult = 1;
		int maxResults = 2;

		when(entityManager.createQuery(DYNAMIC_QUERY_STRING)).thenReturn(query);

		queryReader.retrieve(DYNAMIC_QUERY_STRING, firstResult, maxResults);

		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).setHint("openjpa.hint.OracleSelectHint", "/*+ first_rows(" + maxResults + ") */");
		verify(query).getResultList();
	}

	/**
	 * Test executing a dynamic query for given query string and array of parameters.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringAndParameterArray() {
		Object[] params = {"1", "2"};
		when(entityManager.createQuery(DYNAMIC_QUERY_STRING)).thenReturn(query);

		queryReader.retrieve(DYNAMIC_QUERY_STRING, params);

		verify(query).setParameters(params);
		verify(query).getResultList();
	}

	/**
	 * Test executing a dynamic query for a given query string and parameter map.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringAndParameterMap() {
		Map<String, Long> params = new HashMap<>();
		params.put("param1", 1L);
		params.put("param2", 2L);

		when(entityManager.createQuery(DYNAMIC_QUERY_STRING)).thenReturn(query);

		queryReader.retrieveWithNamedParameters(DYNAMIC_QUERY_STRING, params);

		verify(query).setParameters(params);
		verify(query).getResultList();
	}

	/**
	 * Test executing a dynamic query for a given query string, list parameter, params array and boundaries.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringListParameterAndBoundaries() {
		int firstResult = 1;
		int maxResults = 2;

		Object[] params = {"1", "2"};
		List<Long> listParamValues = Lists.newArrayList(1L, 2L);

		when(entityManager.createQuery(DYNAMIC_QUERY_WITH_LIST_PARAM_STRING)).thenReturn(query);

		queryReader.retrieveWithList(DYNAMIC_QUERY_WITH_LIST_PARAM_STRING, LIST_PARAM_NAME, listParamValues, params, firstResult, maxResults);

		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).setParameters(params);
		verify(query).setParameter(LIST_PARAM_NAME, listParamValues);
		verify(query).getResultList();
	}

	/**
	 * Test executing a dynamic query for a given query string, params array and boundaries.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringParameterArrayAndBoundaries() {
		int firstResult = 1;
		int maxResults = 2;

		Object[] params = {"1", "2"};

		when(entityManager.createQuery(DYNAMIC_QUERY_STRING)).thenReturn(query);

		queryReader.retrieve(DYNAMIC_QUERY_STRING, params, 1, 2);

		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).setParameters(params);
		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with an array of parameters.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParameterArray() {
		Object[] params = {"1", "2"};

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, params);

		verify(query).setParameters(params);
		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with an array of parameters and a flush mode.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParameterArrayAndFlushMode() {
		Object[] params = {"1", "2"};
		FlushMode flushMode = FlushMode.AUTO;

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, flushMode, params);

		verify(query).setParameters(params);
		verify(query).setFlushMode(toFlushModeType(flushMode));
		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with parameter map.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParameterMap() {
		Map<String, String> params = new HashMap<>();
		params.put("param1", "value1");

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, MapUtils.fixedSizeMap(params));

		verify(query).setParameters(params);
		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with first and max results boundaries.
	 */
	@Test
	public void shouldExecuteNamedQueryWithBoundaries() {
		int firstResult = 1;
		int maxResults = 2;

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, firstResult, maxResults);

		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with an array of params and first and max results boundaries.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParamsArrayAndBoundaries() {
		int firstResult = 1;
		int maxResults = 2;
		Object[] params = {"1", "2"};

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, params, firstResult, maxResults);

		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).setParameters(params);
		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with a given map of list parameters and corresponding collection of values.
	 */
	@Test
	public void shouldExecuteNamedQueryWithListParametersAndValueCollectionMap() {
		Map<String, Collection<Long>> params = new HashMap<>();
		params.put("param1", Lists.newArrayList(1L));
		params.put("param2", Lists.newArrayList(2L));

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQueryWithList(NAMED_QUERY, params);

		params.forEach((key, value) -> verify(query).setParameter(key, value));

		verify(query).getResultList();
	}

	/**
	 * Test executing a named query with a single list parameter, corresponding collection of values and additional parameters.
	 */
	@Test
	public void shouldExecuteNamedQueryWithSingleListParameterValuesCollectionAndAdditionalParameters() {
		Object[] params = {"1", "2"};
		Collection<Long> listParamValues = Lists.newArrayList(1L, 2L);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		queryReader.retrieveByNamedQueryWithList(NAMED_QUERY, LIST_PARAM_NAME, listParamValues, params);

		verify(query).setParameters(params);
		verify(query).setParameter(LIST_PARAM_NAME, listParamValues);
		verify(query).getResultList();
	}

	private static final class TestEntity implements Persistable {
		private static final long serialVersionUID = 9000000001L;

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

