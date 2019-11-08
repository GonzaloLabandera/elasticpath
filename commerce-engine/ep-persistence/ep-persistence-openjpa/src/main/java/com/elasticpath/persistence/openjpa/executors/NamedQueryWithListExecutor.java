/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;

import com.elasticpath.persistence.api.Persistable;

/**
 * A specialized version of the {@link NamedQueryExecutor} executor that executes named queries with list of values.
 * E..g SELECT x From XImpl where x.uidPk IN (:list)
 * 
 * @param <V> value type
 * @param <T> entity type
 */
@SuppressWarnings("rawtypes")
public class NamedQueryWithListExecutor<V, T extends Persistable> extends AbstractQueryExecutor {

	private String queryName;
	private Map<String, Collection<V>> mapParameters;
	private String listParameterName;
	private Collection<?> values;
	private Object[] arrayParameters;
	private Integer firstResult;
	private Integer maxResults;

	/**
	 * Set query name.
	 *
	 * @param queryName the query name.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withQueryName(final String queryName) {
		this.queryName = queryName;

		return this;
	}

	/**
	 * Set a map with named parameters.
	 *
	 * @param parameters named parameters.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withParameters(final Map<String, Collection<V>> parameters) {
		this.mapParameters = parameters;

		return this;
	}

	/**
	 * Set an array of parameters.
	 *
	 * @param parameters the array of parameters.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withParameters(final Object... parameters) {
		this.arrayParameters = parameters;

		return this;
	}

	/**
	 * Set list parameter name.
	 *
	 * @param listParameterName the parameter name.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withListParameterName(final String listParameterName) {
		this.listParameterName = listParameterName;

		return this;
	}

	/**
	 * Set a collection of parameter values.
	 *
	 * @param values the collection of values to set.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withParameterValues(final Collection<V> values) {
		this.values = values;

		return this;
	}

	/**
	 * Set the index of the first result.
	 *
	 * @param firstResult the index.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withFirstResult(final Integer firstResult) {
		this.firstResult = firstResult;

		return this;
	}

	/**
	 * Set max number of results to be returned.
	 *
	 * @param maxResults the max number of results to return.
	 * @return the current instance of {@link NamedQueryWithListExecutor}
	 */
	public NamedQueryWithListExecutor withMaxResults(final Integer maxResults) {
		this.maxResults = maxResults;

		return this;
	}

	@Override
	protected String getQuery() {
		return queryName;
	}

	@Override
	protected List<T> executeMultiResultQuery(final EntityManager entityManager) {

		OpenJPAQuery namedQuery =  OpenJPAPersistence.cast(getQueryUtil().createNamedQuery(entityManager, queryName));

		if (this.mapParameters != null) {
			return executeWithMapParameters(namedQuery);
		}

		if (firstResult != null && maxResults != null) {
			return executeWithParametersAndLimit(namedQuery);
		}

		return executeWithParametersInBatches(namedQuery);
	}

	@SuppressWarnings("unchecked")
	private List<T> executeWithMapParameters(final OpenJPAQuery namedQuery) {

		OpenJPAQuery configuredQuery = namedQuery;

		for (final Map.Entry<String, Collection<V>> entry : mapParameters.entrySet()) {
			final Collection<V> mapParamValues = entry.getValue();

			if (mapParamValues == null || mapParamValues.isEmpty()) {
				return Collections.emptyList();
			}

			final String parameterValue = getQueryUtil().getInParameterValues(mapParamValues);
			configuredQuery = OpenJPAPersistence.cast(getQueryUtil().insertListIntoQuery(configuredQuery, entry.getKey(), parameterValue));
		}

		return getQueryUtil().getResults(configuredQuery);
	}

	@SuppressWarnings("unchecked")
	private List<T> executeWithParametersInBatches(final OpenJPAQuery namedQuery) {

		int parameterCount = arrayParameters == null
			? 0
			: arrayParameters.length;

		List<String> listParameters = getQueryUtil().splitCollection(values, parameterCount);

		final List<T> result = new ArrayList<>();

		for (String listParameter : listParameters) {
			Query newQuery = getQueryUtil().insertListIntoQuery(namedQuery, listParameterName, listParameter);

			getQueryUtil().setQueryParameters(newQuery, arrayParameters);

			result.addAll(getQueryUtil().getResults(newQuery));
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private List<T> executeWithParametersAndLimit(final OpenJPAQuery namedQuery) {

		assert ArrayUtils.isNotEmpty(arrayParameters);

		String valuesList = getQueryUtil().getInParameterValues(values);
		if (StringUtils.isEmpty(valuesList)) {
			valuesList = "''";
		}

		Query newQuery = getQueryUtil().insertListIntoQuery(namedQuery, listParameterName, valuesList);
		newQuery.setFirstResult(firstResult);
		newQuery.setMaxResults(maxResults);

		getQueryUtil().setQueryParameters(newQuery, arrayParameters);

		return getQueryUtil().getResults(newQuery);
	}
}
