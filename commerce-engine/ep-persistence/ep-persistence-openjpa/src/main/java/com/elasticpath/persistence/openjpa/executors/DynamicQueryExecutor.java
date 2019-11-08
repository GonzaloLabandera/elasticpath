/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.executors;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.OpenJPAPersistence;

import com.elasticpath.persistence.api.Persistable;

/**
 * The executor for dynamic queries.
 *
 * @param <T> any {@link Persistable} type
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DynamicQueryExecutor<T extends Persistable> extends AbstractQueryExecutor {

	private String queryString;
	private String listParameterName;
	private Collection<?> values;
	private Object[] arrayParameters;
	private Map<String, ?> mapParameters;
	private Integer firstResult;
	private Integer maxResults;

	/**
	 * Set dynamically built query string.
	 *
	 * @param queryString dyanmic query string.
	 *
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public DynamicQueryExecutor withQueryString(final String queryString) {
		this.queryString = queryString;

		return this;
	}

	/**
	 * Set a collection of parameter values.
	 *
	 * @param values the collection of values to set.
	 * @param <V> value type
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public <V> DynamicQueryExecutor withParameterValues(final Collection<V> values) {
		this.values = values;

		return this;
	}

	/**
	 * Set list parameter name.
	 * @param listParameterName the parameter name.
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public DynamicQueryExecutor withListParameterName(final String listParameterName) {
		this.listParameterName = listParameterName;

		return this;
	}

	/**
	 * Set a map with named parameters.
	 *
	 * @param parameters named parameters.
	 * @param <V> value type
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public <V> DynamicQueryExecutor withParameters(final Map<String, V> parameters) {
		this.mapParameters = parameters;

		return this;
	}

	/**
	 * Set an array of parameters.
	 *
	 * @param parameters the array of parameters.
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public DynamicQueryExecutor withParameters(final Object... parameters) {
		this.arrayParameters = parameters;

		return this;
	}

	/**
	 * Set the index of the first result.
	 *
	 * @param firstResult the index.
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public DynamicQueryExecutor withFirstResult(final Integer firstResult) {
		this.firstResult = firstResult;

		return this;
	}

	/**
	 * Set max number of results to be returned.
	 *
	 * @param maxResults the max number of results to return.
	 * @return the current instance of {@link DynamicQueryExecutor}
	 */
	public DynamicQueryExecutor withMaxResults(final Integer maxResults) {
		this.maxResults = maxResults;

		return this;
	}

	@Override
	public String getQuery() {
		return queryString;
	}

	@Override
	public List<T> executeMultiResultQuery(final EntityManager entityManager) {

		Query query = entityManager.createQuery(queryString);

		if (StringUtils.isNotBlank(listParameterName)
			&& CollectionUtils.isNotEmpty(values)) {

			String listValues = getQueryUtil().getInParameterValues(values);
			if (StringUtils.isEmpty(listValues)) {
				listValues = "''";
			}

			query = getQueryUtil().insertListIntoQuery(OpenJPAPersistence.cast(query), listParameterName, listValues);
		}

		getQueryUtil().setQueryParameters(query, arrayParameters);
		getQueryUtil().setQueryParameters(query, mapParameters);

		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}

		if (maxResults != null) {
			query.setMaxResults(maxResults);
			query.setHint("openjpa.hint.OracleSelectHint", "/*+ first_rows(" + maxResults + ") */");
		}

		return getQueryUtil().getResults(query);
	}
}
