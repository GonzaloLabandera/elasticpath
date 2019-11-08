/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.executors.DynamicQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.IdentityQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.NamedQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.NamedQueryWithListExecutor;


/**
 * Unit test for the {@code QueryReader} class.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class QueryReaderTest {

	private static final String DYNAMIC_QUERY_STRING = "SELECT c FROM CustomerImpl c";
	private static final String NAMED_QUERY = "NAMED_QUERY";
	private static final String LIST_PARAM_NAME = "list";

	@InjectMocks private QueryReader queryReader;

	@Mock private IdentityQueryExecutor identityQueryExecutor;
	@Mock private DynamicQueryExecutor dynamicQueryExecutor;
	@Mock private NamedQueryExecutor namedQueryExecutor;
	@Mock private NamedQueryWithListExecutor namedQueryWithListExecutor;
	@Mock private ApplicationContext applicationContext;

	@Before
	public void init() {
		when(applicationContext.getBean("identityQueryExecutor")).thenReturn(identityQueryExecutor);
		when(applicationContext.getBean("dynamicQueryExecutor")).thenReturn(dynamicQueryExecutor);
		when(applicationContext.getBean("namedQueryExecutor")).thenReturn(namedQueryExecutor);
		when(applicationContext.getBean("namedQueryWithListExecutor")).thenReturn(namedQueryWithListExecutor);

		when(dynamicQueryExecutor.withQueryString(any())).thenReturn(dynamicQueryExecutor);
		when(namedQueryExecutor.withQueryName(any())).thenReturn(namedQueryExecutor);
		when(namedQueryWithListExecutor.withQueryName(any())).thenReturn(namedQueryWithListExecutor);

	}
	/**
	 * Test loading entity for given class and uidpk.
	 */
	@Test
	public void shouldEntityForGivenClassAndUidPk() {
		when(identityQueryExecutor.withClass(any())).thenReturn(identityQueryExecutor);
		when(identityQueryExecutor.withUidPk(any())).thenReturn(identityQueryExecutor);

		queryReader.load(Persistable.class, 1L);

		verify(identityQueryExecutor).withClass(Persistable.class);
		verify(identityQueryExecutor).withUidPk(1L);
		verify(identityQueryExecutor).executeAndReturnSingleResult();
	}

	/**
	 * Test executing a dynamic query for a given query string and first and max results boundaries.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringAndFirstAndMaxResultsLimits() {
		when(dynamicQueryExecutor.withFirstResult(any())).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withMaxResults(any())).thenReturn(dynamicQueryExecutor);

		queryReader.retrieve(DYNAMIC_QUERY_STRING, 1, 2);

		verify(dynamicQueryExecutor).withQueryString(DYNAMIC_QUERY_STRING);
		verify(dynamicQueryExecutor).withFirstResult(1);
		verify(dynamicQueryExecutor).withMaxResults(2);
		verify(dynamicQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a dynamic query for given query string and array of parameters.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringAndParameterArray() {
		Object[] params = {"1", "2"};

		when(dynamicQueryExecutor.withParameters(params)).thenReturn(dynamicQueryExecutor);

		queryReader.retrieve(DYNAMIC_QUERY_STRING, params);

		verify(dynamicQueryExecutor).withQueryString(DYNAMIC_QUERY_STRING);
		verify(dynamicQueryExecutor).withParameters(params);
		verify(dynamicQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a dynamic query for a given query string and parameter map.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringAndParameterMap() {
		Map<String, Long> params = new HashMap<>();
		params.put("param1", 1L);
		params.put("param2", 2L);

		when(dynamicQueryExecutor.withParameters(params)).thenReturn(dynamicQueryExecutor);

		queryReader.retrieveWithNamedParameters(DYNAMIC_QUERY_STRING, params);

		verify(dynamicQueryExecutor).withQueryString(DYNAMIC_QUERY_STRING);
		verify(dynamicQueryExecutor).withParameters(params);
		verify(dynamicQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a dynamic query for a given query string, list parameter, params array and boundaries.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringListParameterAndBoundaries() {
		Object[] params = {"1", "2"};
		List<Long> listParamValues = Lists.newArrayList(1L, 2L);

		when(dynamicQueryExecutor.withParameters(params)).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withListParameterName(any())).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withParameterValues(any())).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withFirstResult(any())).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withMaxResults(any())).thenReturn(dynamicQueryExecutor);

		queryReader.retrieveWithList(DYNAMIC_QUERY_STRING, LIST_PARAM_NAME, listParamValues, params, 1, 2);

		verify(dynamicQueryExecutor).withQueryString(DYNAMIC_QUERY_STRING);
		verify(dynamicQueryExecutor).withParameters(params);
		verify(dynamicQueryExecutor).withListParameterName(LIST_PARAM_NAME);
		verify(dynamicQueryExecutor).withParameterValues(listParamValues);
		verify(dynamicQueryExecutor).withFirstResult(1);
		verify(dynamicQueryExecutor).withMaxResults(2);
		verify(dynamicQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a dynamic query for a given query string, params array and boundaries.
	 */
	@Test
	public void shouldExecuteDynamicQueryWithQueryStringParameterArrayAndBoundaries() {
		Object[] params = {"1", "2"};

		when(dynamicQueryExecutor.withParameters(params)).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withFirstResult(any())).thenReturn(dynamicQueryExecutor);
		when(dynamicQueryExecutor.withMaxResults(any())).thenReturn(dynamicQueryExecutor);

		queryReader.retrieve(DYNAMIC_QUERY_STRING, params, 1, 2);

		verify(dynamicQueryExecutor).withQueryString(DYNAMIC_QUERY_STRING);
		verify(dynamicQueryExecutor).withParameters(params);
		verify(dynamicQueryExecutor).withFirstResult(1);
		verify(dynamicQueryExecutor).withMaxResults(2);
		verify(dynamicQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with an array of parameters.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParameterArray() {
		Object[] params = {"1", "2"};

		when(namedQueryExecutor.withParameters(params)).thenReturn(namedQueryExecutor);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, params);

		verify(namedQueryExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryExecutor).withParameters(params);
		verify(namedQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with an array of parameters and a flush mode.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParameterArrayAndFlushMode() {
		Object[] params = {"1", "2"};
		FlushMode flushMode = FlushMode.AUTO;

		when(namedQueryExecutor.withParameters(params)).thenReturn(namedQueryExecutor);
		when(namedQueryExecutor.withFlushMode(any())).thenReturn(namedQueryExecutor);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, flushMode, params);

		verify(namedQueryExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryExecutor).withParameters(params);
		verify(namedQueryExecutor).withFlushMode(flushMode);
		verify(namedQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with parameter map.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParameterMap() {
		when(namedQueryExecutor.withParameters(anyMap())).thenReturn(namedQueryExecutor);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, new HashMap<>());

		verify(namedQueryExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryExecutor).withParameters(anyMap());
		verify(namedQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with first and max results boundaries.
	 */
	@Test
	public void shouldExecuteNamedQueryWithBoundaries() {
		when(namedQueryExecutor.withFirstResult(any())).thenReturn(namedQueryExecutor);
		when(namedQueryExecutor.withMaxResults(any())).thenReturn(namedQueryExecutor);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, 1, 2);

		verify(namedQueryExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryExecutor).withFirstResult(1);
		verify(namedQueryExecutor).withMaxResults(2);
		verify(namedQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with an array of params and first and max results boundaries.
	 */
	@Test
	public void shouldExecuteNamedQueryWithParamsArrayAndBoundaries() {
		Object[] params = {"1", "2"};

		when(namedQueryExecutor.withParameters(params)).thenReturn(namedQueryExecutor);
		when(namedQueryExecutor.withFirstResult(any())).thenReturn(namedQueryExecutor);
		when(namedQueryExecutor.withMaxResults(any())).thenReturn(namedQueryExecutor);

		queryReader.retrieveByNamedQuery(NAMED_QUERY, params, 1, 2);

		verify(namedQueryExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryExecutor).withParameters(params);
		verify(namedQueryExecutor).withFirstResult(1);
		verify(namedQueryExecutor).withMaxResults(2);
		verify(namedQueryExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with a given map of list parameters and corresponding collection of values.
	 */
	@Test
	public void shouldExecuteNamedQueryWithListParametersAndValueCollectionMap() {
		Map<String, Collection<Long>> params = new HashMap<>();
		params.put("param1", Lists.newArrayList(1L));
		params.put("param2", Lists.newArrayList(2L));

		when(namedQueryWithListExecutor.withParameters(params)).thenReturn(namedQueryWithListExecutor);

		queryReader.retrieveByNamedQueryWithList(NAMED_QUERY, params);

		verify(namedQueryWithListExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryWithListExecutor).withParameters(params);
		verify(namedQueryWithListExecutor).executeAndReturnResultList();
	}

	/**
	 * Test executing a named query with a single list parameter, corresponding collection of values and additional parameters.
	 */
	@Test
	public void shouldExecuteNamedQueryWithSingleListParameterValuesCollectionAndAdditionalParameters() {
		Object[] params = {"1", "2"};
		Collection<Long> listParamValues = Lists.newArrayList(1L, 2L);

		when(namedQueryWithListExecutor.withParameters(params)).thenReturn(namedQueryWithListExecutor);
		when(namedQueryWithListExecutor.withParameterValues(any())).thenReturn(namedQueryWithListExecutor);
		when(namedQueryWithListExecutor.withListParameterName(any())).thenReturn(namedQueryWithListExecutor);

		queryReader.retrieveByNamedQueryWithList(NAMED_QUERY, LIST_PARAM_NAME, listParamValues, params);

		verify(namedQueryWithListExecutor).withQueryName(NAMED_QUERY);
		verify(namedQueryWithListExecutor).withListParameterName(LIST_PARAM_NAME);
		verify(namedQueryWithListExecutor).withParameterValues(listParamValues);
		verify(namedQueryWithListExecutor).withParameters(params);
		verify(namedQueryWithListExecutor).executeAndReturnResultList();
	}
}

