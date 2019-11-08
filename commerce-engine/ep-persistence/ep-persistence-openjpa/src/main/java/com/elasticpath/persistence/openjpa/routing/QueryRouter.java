/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.routing;

import static org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive;
import static org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.openjpa.util.QueryRouterMetaInfoHolder;

/**
 * The focal class used for routing query executions on master or replica db based on multiple conditions:
 *
 * 1. Is Horizontal db scaling (HDS) feature enabled
 * 2. Is Query is safe to execute on replica
 * 3. Is query, being executed, under active and non-read-only transaction
 * 4. Is result null/empty
 *
 * Based on the outcome of these checks read-write or read-only {@link EntityManager} will be used.
 *
 */
public class QueryRouter {

	private static final Logger LOG = LoggerFactory.getLogger(QueryRouter.class);

	private EntityManager readWriteEntityManager;
	private EntityManager readOnlyEntityManager;

	private HDSSupportBean hdsSupportBean;
	private QueryRouterMetaInfoHolder queryRouterMetaInfoHolder;

	/**
	 * If HDS feature is enabled, it will trigger the population of the {@link QueryRouterMetaInfoHolder} structure.
	 */
	public void init() {
		if (hdsSupportBean.isHdsSupportEnabled()) {
			queryRouterMetaInfoHolder.initFromRWEntityManager(readWriteEntityManager);
		}
	}

	/**
	 * Return correct {@link EntityManager} for given query name or a dynamic query string by checking
	 * aforementioned conditions.
	 *
	 * @param queryName the query name or a dynamic query string
	 * @return read-write or read-only {@link EntityManager}
	 */
	public EntityManager getEntityManagerForQuery(final String queryName) {
		EntityManager activeEntityManager = readWriteEntityManager;
		if (canReadFromReplica(queryName)) {

			LOG.debug("Using RO Entity Manager for query {}", queryName);

			activeEntityManager = readOnlyEntityManager;
		} else {
			LOG.debug("Using RW Entity Manager for query {}", queryName);
		}

		return activeEntityManager;
	}

	/**
	 * Check if query is retriable. The query is retriable if must return a non-empty result.
	 * E.g.
	 * 	Select c From CustomerImpl where c.uidPk=123 => must return "c"
	 * 	Select c From CustomerImpl 					 => may or may not return results
	 *
	 * @param queryName the query name or dynamic query string
	 * @return true, if query is retriable.
	 */
	public boolean isQueryRetriable(final String queryName) {
		return queryRouterMetaInfoHolder.isQueryRetriable(queryName);
	}

	/**
	 * This method should prevent reading stale data by retrying on master, if stale results are detected.
	 * The following conditions must be met in order to retry on master:
	 *
	 * 1. HDS feature must be enabled
	 * 2. The passed {@link EntityManager} must be the read-only one
	 * 3. The query is not safe for replica
	 * 4. The query is retriable
	 * 5. The result is null or empty (if it's an instance of {@link List}
	 *
	 * @param activeEntityManager the {@link EntityManager} obtained from {@link #getEntityManagerForQuery} method.
	 * @param queryName the query name or dynamic query string
	 * @param result the execution result
	 * @param <T> the type
	 * @return true, if all conditions are met
	 */
	public <T> boolean shouldRetry(final EntityManager activeEntityManager, final String queryName, final T result) {

		//check whether retry on master is required
		boolean shouldRetryOnMaster = isHDSEnabledAndActiveManagerIsReadOnly(activeEntityManager)
			&& isQueryNotSafeAndRetriable(queryName)
			&& isResultEmpty(result);

		if (shouldRetryOnMaster) {
				LOG.debug("Query {} will be retried on master", queryName);
				return true;
		}

		LOG.debug("No need to retry query {}", queryName);

		return false;
	}

	private boolean isHDSEnabledAndActiveManagerIsReadOnly(final EntityManager activeEntityManager) {
		return hdsSupportBean.isHdsSupportEnabled() && activeEntityManager.equals(readOnlyEntityManager);
	}

	private boolean isQueryNotSafeAndRetriable(final String queryName) {
		return !hdsSupportBean.isQuerySafeForReplica() && isQueryRetriable(queryName);
	}

	@SuppressWarnings("unchecked")
	private <T> boolean isResultEmpty(final T result) {
		return result == null || (result instanceof List && CollectionUtils.isEmpty((List<T>) result));
	}

	private boolean canReadFromReplica(final String queryName) {
		if (!hdsSupportBean.isHdsSupportEnabled()) {
			return false;
		}

		if (isActualTransactionActive() && !isCurrentTransactionReadOnly()) {
			LOG.debug("Query {} is under TX. Using master", queryName);
			return false;
		}

		return verifyQueryIsSafeForReplica(queryName);
	}

	private boolean verifyQueryIsSafeForReplica(final String queryName) {
		return queryRouterMetaInfoHolder.isQuerySafeForReadingFromReplica(queryName);
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}

	public void setQueryRouterMetaInfoHolder(final QueryRouterMetaInfoHolder queryRouterMetaInfoHolder) {
		this.queryRouterMetaInfoHolder = queryRouterMetaInfoHolder;
	}

	public void setReadWriteEntityManager(final EntityManager readWriteEntityManager) {
		this.readWriteEntityManager = readWriteEntityManager;
	}

	public EntityManager getReadWriteEntityManager() {
		return readWriteEntityManager;
	}

	public void setReadOnlyEntityManager(final EntityManager readOnlyEntityManager) {
		this.readOnlyEntityManager = readOnlyEntityManager;
	}

	public EntityManager getReadOnlyEntityManager() {
		return readOnlyEntityManager;
	}
}
