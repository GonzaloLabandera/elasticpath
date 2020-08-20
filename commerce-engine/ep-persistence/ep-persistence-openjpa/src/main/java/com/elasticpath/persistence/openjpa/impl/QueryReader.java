/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.openjpa.executors.AbstractQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.DynamicQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.IdentityQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.NamedQueryExecutor;
import com.elasticpath.persistence.openjpa.executors.NamedQueryWithListExecutor;
import com.elasticpath.persistence.openjpa.routing.QueryRouter;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;

/**
 * This is a main interface used for all types of queries. All read methods mirror those in com.elasticpath.persistence.api.PersistenceEngine
 * using specialized query executors.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class QueryReader {

	private QueryRouter queryRouter;
	private FetchPlanHelper fetchPlanHelper;

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#load(Class, long)
	 *
	 * @param <T> the type of the object
	 * @param persistenceClass the persistent class of the given id.
	 * @param uidPk the persistent instance id.
	 * @return the persistent instance
	 */
	public <T> T load(final Class<T> persistenceClass, final long uidPk) {

		return (T) getIdentityQueryExecutor()
			.withUidPk(uidPk)
			.withClass(persistenceClass)
			.executeAndReturnSingleResult();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieve(String, int, int)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieve(final String queryStr, final int firstResult, final int maxResults) {

		return getDynamicQueryExecutor()
			.withQueryString(queryStr)
			.withFirstResult(firstResult)
			.withMaxResults(maxResults)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieve(String, Object...)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieve(final String queryStr, final Object... parameters) {

		return getDynamicQueryExecutor()
			.withQueryString(queryStr)
			.withParameters(parameters)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveWithNamedParameters(String, Map)
	 *
	 * @param queryStr the query
	 * @param parameters the parameter names and values to be used with the given query
	 * @param <T> the entity type returned by the query
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieveWithNamedParameters(final String queryStr, final Map<String, ?> parameters) {

		return getDynamicQueryExecutor()
			.withQueryString(queryStr)
			.withParameters(parameters)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveWithList(String, String, Collection, Object[], int, int)
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryStr the JPQL query
	 * @param values the collection of values
	 * @param listParameterName the name of the parameter for the list values
	 * @param parameters the parameters to be used with the criteria
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances.
	 */
	public <T, E> List<T> retrieveWithList(final String queryStr, final String listParameterName, final Collection<E> values,
		final Object[] parameters, final int firstResult, final int maxResults) {

		return getDynamicQueryExecutor()
			.withQueryString(queryStr)
			.withListParameterName(listParameterName)
			.withParameterValues(values)
			.withParameters(parameters)
			.withFirstResult(firstResult)
			.withMaxResults(maxResults)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieve(String, Object[], int, int)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the HQL query string to be executed
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @param parameters the parameters to be used with the criteria
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieve(final String queryStr, final Object[] parameters, final int firstResult, final int maxResults) {

		return getDynamicQueryExecutor()
			.withQueryString(queryStr)
			.withParameters(parameters)
			.withFirstResult(firstResult)
			.withMaxResults(maxResults)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQuery(String, Object...)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieveByNamedQuery(final String queryName, final Object... parameters) {

		return getNamedQueryExecutor()
			.withQueryName(queryName)
			.withParameters(parameters)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQuery(String, FlushMode, Object...)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param flushMode the flush mode to use when executing the query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieveByNamedQuery(final String queryName, final FlushMode flushMode, final Object... parameters) {

		return getNamedQueryExecutor()
			.withQueryName(queryName)
			.withParameters(parameters)
			.withFlushMode(flushMode)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQuery(String, FlushMode, boolean, Object...)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param flushMode the flush mode to use when executing the query
	 * @param ignoreChanges whether to ignore changes in local cache
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieveByNamedQuery(final String queryName, final FlushMode flushMode, final boolean ignoreChanges,
		final Object... parameters) {

		return getNamedQueryExecutor()
			.withQueryName(queryName)
			.withParameters(parameters)
			.withFlushMode(flushMode)
			.withIgnoreChanges(ignoreChanges)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQuery(String, Map)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the named list of parameters to be used with the given query
	 * @return a collection of persistable instances
	 */
	public <T> List<T> retrieveByNamedQuery(final String queryName, final Map<String, Object> parameters) {

		return getNamedQueryExecutor()
			.withQueryName(queryName)
			.withParameters(parameters)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQuery(String, int, int)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances.
	 */
	public <T> List<T> retrieveByNamedQuery(final String queryName, final int firstResult, final int maxResults) {

		return getNamedQueryExecutor()
			.withQueryName(queryName)
			.withFirstResult(firstResult)
			.withMaxResults(maxResults)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQuery(String, Object[], int, int)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieveByNamedQuery(final String queryName, final Object[] parameters, final int firstResult, final int maxResults) {
		return getNamedQueryExecutor()
			.withQueryName(queryName)
			.withParameters(parameters)
			.withFirstResult(firstResult)
			.withMaxResults(maxResults)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQueryWithList(String, Map)
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryName the named query
	 * @param parameterValuesMap the map of name - list values
	 * @return a list of persistent instances
	 */
	public <T, E> List<T> retrieveByNamedQueryWithList(final String queryName, final Map<String, Collection<E>> parameterValuesMap) {

		return getNamedQueryWithListExecutor()
			.withQueryName(queryName)
			.withParameters(parameterValuesMap)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQueryWithList(String, String, Collection, Object...)
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryName the named query
	 * @param listParameterName the name of the parameter for the list values
	 * @param values the collection of values
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	public <T, E> List<T> retrieveByNamedQueryWithList(final String queryName, final String listParameterName, final Collection<E> values,
		final Object... parameters) {

		return getNamedQueryWithListExecutor()
			.withQueryName(queryName)
			.withListParameterName(listParameterName)
			.withParameters(parameters)
			.withParameterValues(values)
			.executeAndReturnResultList();
	}

	/**
	 * @see com.elasticpath.persistence.api.PersistenceEngine#retrieveByNamedQueryWithList(String, String, Collection, Object[], int, int)
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param listParameterName name of the list parameter
	 * @param values collection of values for the list parameter
	 * @param parameters the parameters to be used with the given query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	public <T> List<T> retrieveByNamedQueryWithList(final String queryName, final String listParameterName, final Collection<?> values,
		final Object[] parameters, final int firstResult, final int maxResults) {

		return getNamedQueryWithListExecutor()
			.withQueryName(queryName)
			.withListParameterName(listParameterName)
			.withParameters(parameters)
			.withParameterValues(values)
			.withFirstResult(firstResult)
			.withMaxResults(maxResults)
			.executeAndReturnResultList();
	}


	public IdentityQueryExecutor getIdentityQueryExecutor() {
		return  getQueryExecutor(IdentityQueryExecutor.class);
	}

	public DynamicQueryExecutor getDynamicQueryExecutor() {
		return  getQueryExecutor(DynamicQueryExecutor.class);
	}

	public NamedQueryExecutor getNamedQueryExecutor() {
		return  getQueryExecutor(NamedQueryExecutor.class);
	}

	public NamedQueryWithListExecutor getNamedQueryWithListExecutor() {
		return  getQueryExecutor(NamedQueryWithListExecutor.class);
	}

	private <T extends AbstractQueryExecutor> T getQueryExecutor(final Class<T> executorClass) {
		try {
			T queryExecutor = executorClass.newInstance();
			queryExecutor.setQueryRouter(queryRouter);
			queryExecutor.setFetchPlanHelper(fetchPlanHelper);

			return queryExecutor;
		} catch (Exception e) {
			throw new EpSystemException("Error occurred while creating a query executor", e);
		}
	}

	public void setQueryRouter(final QueryRouter queryRouter) {
		this.queryRouter = queryRouter;
	}

	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}
}
