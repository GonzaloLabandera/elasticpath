/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.executors;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.openjpa.persistence.FetchPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.openjpa.routing.QueryRouter;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * A main class for query execution with a support for retrying queries on master in case of
 * db replica failures or stale data.
 *
 * The extensions must implement {@link #getQuery} and either {@link #executeSingleResultQuery} or
 * {@link #executeMultiResultQuery} method.
 *
 *
 * @param <T> any {@link Persistable} type
 */
public abstract class AbstractQueryExecutor<T extends Persistable> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractQueryExecutor.class);

	private QueryRouter queryRouter;
	private QueryUtil queryUtil;
	private FetchPlanHelper fetchPlanHelper;

	/**
	 * Depending on the query type, the returned query can be a dynamic string or a query name.
	 * Must be implemented by extensions.
	 *
	 * @return a query string or a name
	 */
	protected abstract String getQuery();

	/**
	 * Execute query and return a single result. If query is about to be executed on db replica and returned result
	 * is stale (for those queries that MUST return a non-null result) or db replica is not available the query
	 * will be retried on master (without further retries if master is not available.
	 *
	 * @return the query result.
	 */
	public T executeAndReturnSingleResult() {
		EntityManager activeEntityManager = queryRouter.getEntityManagerForQuery(getQuery());

		FetchPlan activeFetchPlan = null;
		FetchPlan rwFetchPlan = null;
		T result;

		try {
			Pair<T, FetchPlan> pairResultFetchPlan = executeSingleResultQueryWithEntityManager(activeEntityManager);
			result = pairResultFetchPlan.getLeft();
			activeFetchPlan = pairResultFetchPlan.getRight();

			if (queryRouter.shouldRetry(activeEntityManager, getQuery(), result)) {
				LOG.debug("Retrying query {} on master because replica didn't return results", getQuery());

				pairResultFetchPlan = executeSingleResultQueryWithEntityManager(queryRouter.getReadWriteEntityManager());
				result = pairResultFetchPlan.getLeft();
				rwFetchPlan = pairResultFetchPlan.getRight();
			}

			return result;

		} catch (Exception e) {
			throw new EpPersistenceException("Error occurred while executing executeAndReturnSingleResult method", e);
		} finally {
			fetchPlanHelper.clearFetchPlan(rwFetchPlan);
			fetchPlanHelper.clearFetchPlan(activeFetchPlan);
		}
	}

	/**
	 * Execute query and return a list as a result. If query is about to be executed on db replica and returned result
	 * is stale (for those queries that MUST return a non-null/empty result) or db replica is not available the query
	 * will be retried on master (without further retries if master is not available.
	 *
	 * @return the query result.
	 */
	public List<T> executeAndReturnResultList() {
		EntityManager activeEntityManager = queryRouter.getEntityManagerForQuery(getQuery());

		FetchPlan activeFetchPlan = null;
		FetchPlan rwFetchPlan = null;
		List<T> result;

		try {
			Pair<List<T>, FetchPlan> pairResultFetchPlan = executeMultiResultQueryWithEntityManager(activeEntityManager);
			result = pairResultFetchPlan.getLeft();
			activeFetchPlan = pairResultFetchPlan.getRight();

			if (queryRouter.shouldRetry(activeEntityManager, getQuery(), result)) {

				pairResultFetchPlan = executeMultiResultQueryWithEntityManager(queryRouter.getReadWriteEntityManager());
				result = pairResultFetchPlan.getLeft();
				rwFetchPlan = pairResultFetchPlan.getRight();
			}

			return result;

		} catch (Exception e) {
			throw new EpPersistenceException("Error occurred while executing executeAndReturnResultList method", e);
		} finally {
			fetchPlanHelper.clearFetchPlan(rwFetchPlan);
			fetchPlanHelper.clearFetchPlan(activeFetchPlan);
		}
	}

	/**
	 * Execute a query, using provided {@link EntityManager} and return a single result.
	 *
	 * @param entityManager the entity manager.
	 * @return a single result.
	 */
	protected T executeSingleResultQuery(final EntityManager entityManager) {
		throw new EpPersistenceException("Not supported");
	}

	/**
	 * Execute a query, using provided {@link EntityManager} and return a list of result.
	 *
	 * @param entityManager the entity manager.
	 * @return a list of results.
	 */
	protected List<T> executeMultiResultQuery(final EntityManager entityManager) {
		throw new EpPersistenceException("Not supported");
	}

	private Pair<T, FetchPlan> executeSingleResultQueryWithEntityManager(final EntityManager entityManager) {
		FetchPlan fetchPlan = fetchPlanHelper.configureFetchPlan(entityManager);

		T result = executeSingleResultQuery(entityManager);

		return Pair.of(result, fetchPlan);
	}

	private Pair<List<T>, FetchPlan> executeMultiResultQueryWithEntityManager(final EntityManager entityManager) {
		FetchPlan fetchPlan = fetchPlanHelper.configureFetchPlan(entityManager);

		List<T> result = executeMultiResultQuery(entityManager);

		return Pair.of(result, fetchPlan);
	}

	public void setQueryRouter(final QueryRouter queryRouter) {
		this.queryRouter = queryRouter;
	}

	public void setQueryUtil(final QueryUtil queryUtil) {
		this.queryUtil = queryUtil;
	}

	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}

	public QueryUtil getQueryUtil() {
		return queryUtil;
	}
}
