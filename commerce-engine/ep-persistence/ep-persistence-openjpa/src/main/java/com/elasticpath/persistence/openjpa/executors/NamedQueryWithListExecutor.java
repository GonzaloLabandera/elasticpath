/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.executors;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.getResults;
import static com.elasticpath.persistence.openjpa.util.QueryUtil.splitCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
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
	private Collection<V> values;
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

		OpenJPAQuery namedQuery = OpenJPAPersistence.cast(entityManager.createNamedQuery(queryName));

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

		for (final Map.Entry<String, Collection<V>> entry : mapParameters.entrySet()) {
			final Collection<V> mapParamValues = entry.getValue();

			if (CollectionUtils.isEmpty(mapParamValues)) {
				return Collections.emptyList();
			}

			namedQuery.setParameter(entry.getKey(), mapParamValues);
		}

		return getResults(namedQuery);
	}

	@SuppressWarnings("unchecked")
	private List<T> executeWithParametersInBatches(final OpenJPAQuery namedQuery) {

		List<List<V>> listOfSubListsOfParameters = splitCollection(values);

		final List<T> result = new ArrayList<>();

		for (List<V> subListOfParameters : listOfSubListsOfParameters) {

			namedQuery.setParameters(arrayParameters);
			namedQuery.setParameter(listParameterName, subListOfParameters);

			result.addAll(getResults(namedQuery));
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private List<T> executeWithParametersAndLimit(final OpenJPAQuery namedQuery) {

		namedQuery.setParameters(arrayParameters);

		if (CollectionUtils.isEmpty(values)) {
			namedQuery.setParameter(listParameterName, null);
		} else {
			namedQuery.setParameter(listParameterName, values);
		}

		namedQuery.setFirstResult(firstResult);
		namedQuery.setMaxResults(maxResults);

		return getResults(namedQuery);
	}
}
