/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.executors;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.getResults;
import static com.elasticpath.persistence.openjpa.util.QueryUtil.toFlushModeType;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;

import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.Persistable;

/**
 * The executor for named queries.
 *
 * @param <T> any {@link Persistable} type
 */
@SuppressWarnings("rawtypes")
public class NamedQueryExecutor<T extends Persistable> extends AbstractQueryExecutor {

	private String queryName;
	private Object[] arrayParameters;
	private Map<String, ?> mapParameters;
	private FlushMode flushMode;
	private Integer firstResult;
	private Integer maxResults;
	private Boolean ignoreChanges;
	private Class<?> resultClass;

	/**
	 * Set query name.
	 *
	 * @param queryName the query name.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withQueryName(final String queryName) {
		this.queryName = queryName;

		return this;
	}

	/**
	 * Set an array of parameters.
	 *
	 * @param parameters the array of parameters.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withParameters(final Object... parameters) {
		this.arrayParameters = parameters;

		return  this;
	}

	/**
	 * Set a map with named parameters.
	 *
	 * @param parameters named parameters.
	 * @param <V> value type
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public <V> NamedQueryExecutor withParameters(final Map<String, V> parameters) {
		this.mapParameters = parameters;

		return  this;
	}

	/**
	 * Set query flush mode.
	 *
	 * @param flushMode the flush mode.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withFlushMode(final FlushMode flushMode) {
		this.flushMode = flushMode;

		return this;
	}

	/**
	 * Set the index of the first result.
	 *
	 * @param firstResult the index.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withFirstResult(final Integer firstResult) {
		this.firstResult = firstResult;

		return this;
	}

	/**
	 * Set max number of results to be returned.
	 *
	 * @param maxResults the max number of results to return.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withMaxResults(final Integer maxResults) {
		this.maxResults = maxResults;

		return this;
	}

	/**
	 * Ignore changes, if required.
	 *
	 * @param ignoreChanges the flag.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withIgnoreChanges(final Boolean ignoreChanges) {
		this.ignoreChanges = ignoreChanges;

		return this;
	}

	/**
	 * Use provided class to wrap raw data.
	 *
	 * @param resultClass the class to wrap raw data with.
	 * @return the current instance of {@link NamedQueryExecutor}
	 */
	public NamedQueryExecutor withResultClass(final Class<?> resultClass) {
		this.resultClass = resultClass;

		return this;
	}

	@Override
	public String getQuery() {
		return queryName;
	}

	@Override
	@SuppressWarnings({"unchecked", "PMD.NPathComplexity"})
	public List<T> executeMultiResultQuery(final EntityManager entityManager) {
		OpenJPAQuery namedQuery = resultClass == null
				? OpenJPAPersistence.cast(entityManager.createNamedQuery(queryName))
				: OpenJPAPersistence.cast(entityManager.createNamedQuery(queryName, resultClass));

		if (ArrayUtils.isNotEmpty(arrayParameters)) {
			namedQuery.setParameters(arrayParameters);

		}

		if (MapUtils.isNotEmpty(mapParameters)) {
			namedQuery.setParameters(mapParameters);
		}

		if (this.flushMode != null) {
			namedQuery.setFlushMode(toFlushModeType(flushMode));
		}

		if (this.firstResult != null) {
			namedQuery.setFirstResult(firstResult);
		}

		if (this.maxResults != null) {
			namedQuery.setMaxResults(maxResults);
		}

		if (this.ignoreChanges != null) {
			OpenJPAPersistence.cast(namedQuery).setIgnoreChanges(ignoreChanges);
		}

		return getResults(namedQuery);
	}
}
