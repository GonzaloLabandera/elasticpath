/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.persistence.openjpa.impl;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.createDynamicJPQLQuery;
import static com.elasticpath.persistence.openjpa.util.QueryUtil.getResults;
import static com.elasticpath.persistence.openjpa.util.QueryUtil.splitCollection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.QueryResultCache;
import org.apache.openjpa.persistence.StoreCache;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import com.elasticpath.persistence.api.ChangeType;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.api.PersistenceEngineOperationListener;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.PersistenceSessionFactory;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngineInternal;
import com.elasticpath.persistence.openjpa.JpaPersistenceSession;
import com.elasticpath.persistence.openjpa.PersistenceInterceptor;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;

/**
 * The JPA implementation of <code>PersistenceEngine</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveClassLength", "PMD.GodClass" })
public class JpaPersistenceEngineImpl implements JpaPersistenceEngineInternal {
	private static final Logger LOG = LoggerFactory.getLogger(JpaPersistenceEngineImpl.class);

	private static final String CAUGHT_AN_EXCEPTION = "Caught an exception";

	private EntityManager entityManager;
	private PersistenceSessionFactory sessionFactory;

	private PlatformTransactionManager txManager;

	private FetchPlanHelper fetchPlanHelper;
	private QueryReaderFactory queryReaderFactory;
	private QueryReader queryReader;

	private List<PersistenceEngineOperationListener> operationListeners = Collections.emptyList();

	/**
	 * Create a new instance of {@link QueryReader} and, subsequently {@link com.elasticpath.persistence.openjpa.routing.QueryRouter} per engine
	 * instance with specific read-write entity manager.
	 */
	public void init() {
		queryReader = queryReaderFactory.createQueryReader(entityManager);
	}

	// @@@@@@@@@@@@ Read operations @@@@@@@@@@@@@@@@@@@@@
	/**
	 * Get a persistent instance with the given id. Return null if no matching record exists.
	 *
	 * @param <T> the type of the object
	 * @param persistenceClass the persistent class of the given id.
	 * @param uidPk the persistent instance id.
	 * @return the persistent instance
	 *
	 * TODO deprecate, replace all get calls with "load" and remove this method eventually because they both call entityManager.find(class, long)
	 */
	@Override
	public <T extends Persistable> T get(final Class<T> persistenceClass, final long uidPk) {
		return load(persistenceClass, uidPk);
	}

	/**
	 * Load a persistent instance with the given id. Throw an unrecoverable exception if there is no matching database row.
	 *
	 * @param <T> the type of the object
	 * @param persistenceClass the persistent class of the given id.
	 * @param uidPk The persistent instance id.
	 * @return the persistent instance
	 *
	 */
	@Override
	public <T extends Persistable> T load(final Class<T> persistenceClass, final long uidPk) {
		return queryReader.load(persistenceClass, uidPk);
	}

	/**
	 * Load a persistent instance with the given id. Throw an unrecoverable exception if there is
	 * no matching database row. This method will create a new session (EntityManager) to execute
	 * the query, and close the new session when completed.
	 *
	 * @param <T> the type of the object
	 * @param persistenceClass the persistent class of the given id.
	 * @param uidPk the persistent instance id.
	 * @return the persistent instance
	 */
	@Override
	public <T extends Persistable> T loadWithNewSession(final Class<T> persistenceClass, final long uidPk) {
		EntityManager newEntityManager = null;
		try {
			newEntityManager = createNewEntityManager();
			return newEntityManager.find(persistenceClass, uidPk);
		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			if (newEntityManager != null) {
				newEntityManager.close();
			}
		}
	}

	/**
	 * Retrieve a list of persistent instances with the given list of queries.
	 * <p>
	 * Note: all queries must return the same type of data. Their results will be merged into the list returned by this method.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queries the list of queries
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances.
	 */
	@Override
	public <T> List<T> retrieve(final List<String> queries, final Object... parameters) {
		final List<T> result = new ArrayList<>();
		for (String query : queries) {
			final List<T> objects = retrieve(query, parameters);
			result.addAll(objects);
		}

		return result;
	}

	/**
	 * Retrieve a list of persistent instances with the specified query and bounds, namely, the maximum number of rows to retrieve and / or the first
	 * row to retrieve. This help to support pagination.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the HQL query string to be executed
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	@Override
	public <T> List<T> retrieve(final String queryStr, final int firstResult, final int maxResults) {
		return queryReader.retrieve(queryStr, firstResult, maxResults);
	}

	/**
	 * Retrieve a list of persistent instances with the specified query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the HQL query
	 * @param parameters the parameters to be used with the criteria
	 * @return a list of persistent instances
	 */
	@Override
	public <T> List<T> retrieve(final String queryStr, final Object... parameters) {
		return queryReader.retrieve(queryStr, parameters);
	}

	@Override
	public <T> List<T> retrieveWithNamedParameters(final String queryStr, final Map<String, ?> parameters) {
		return queryReader.retrieveWithNamedParameters(queryStr, parameters);
	}

	/**
	 * Retrieve a list of persistent instances with the specified query.
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
	@Override
	public <T, E> List<T> retrieveWithList(final String queryStr, final String listParameterName,
		final Collection<E> values, final Object[] parameters, final int firstResult, final int maxResults) {

		return queryReader.retrieveWithList(queryStr, listParameterName, values, parameters, firstResult, maxResults);
	}

	/**
	 * Retrieve a list of persistent instances with the specified query. This method will create a new
	 * session (EntityManager) to excute the query, and close the new session when completed.
	 *
	 * @param <T> the object's type to retrieve
	 * @param query the HQL query
	 * @param parameters the prameters to be used with the criteria
	 * @return a list of persistent instances.
	 */
	@Override
	public <T> List<T> retrieveWithNewSession(final String query, final Object... parameters) {

		EntityManager newEntityManager = null;
		try {
			newEntityManager = createNewEntityManager();

			OpenJPAQuery<?> newQuery = createDynamicJPQLQuery(newEntityManager, query);

			newQuery.setParameters(parameters);

			return getResults(newQuery);
		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			if (newEntityManager != null) {
				newEntityManager.close();
			}
		}
	}

	/**
	 * Retrieve a list of persistent instances with the specified query. This method will create a new
	 * session (EntityManager) to excute the query, and close the new session when completed.
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryString the HQL query
	 * @param values the collection of values
	 * @param listParameterName the name of the parameter for the list values
	 * @param parameters the parameters to be used with the criteria
	 * @return a list of persistent instances.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public <T, E> List<T> retrieveWithListWithNewSession(final String queryString, final String listParameterName, final Collection<E> values,
														 final Object... parameters) {
		EntityManager newEntityManager = null;
		try {
			newEntityManager = createNewEntityManager();
			OpenJPAQuery query = createDynamicJPQLQuery(newEntityManager, queryString);

			List<List<E>> listOfSubListsOfValues = splitCollection(values);

			final List<T> result = new ArrayList<>();
			for (List<E> subListOfValues : listOfSubListsOfValues) {
				query.setParameters(parameters);
				query.setParameter(listParameterName, subListOfValues);

				result.addAll(getResults(query));
			}
			return result;
		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			if (newEntityManager != null) {
				newEntityManager.close();
			}
		}
	}

	/**
	 * Retrieve a list of persistent instances with the specified query and bounds, namely, the maximum number of rows to retrieve and / or the first
	 * row to retrieve. This help to support pagination.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the HQL query string to be executed
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @param parameters the parameters to be used with the criteria
	 * @return a list of persistent instances
	 */
	@Override
	public <T> List<T> retrieve(final String queryStr, final Object[] parameters, final int firstResult, final int maxResults) {
		return queryReader.retrieve(queryStr, parameters, firstResult, maxResults);
	}

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	@Override
	public <T> List<T> retrieveByNamedQuery(final String queryName, final Object... parameters) {
		return queryReader.retrieveByNamedQuery(queryName, parameters);
	}

	@Override
	public <T> List<T> retrieveByNamedQuery(final String queryName, final FlushMode flushMode, final Object... parameters) {
		return queryReader.retrieveByNamedQuery(queryName, flushMode, parameters);
	}

	@Override
	public <T> List<T> retrieveByNamedQuery(final String queryName, final FlushMode flushMode, final boolean ignoreChanges,
		final Object[] parameters) {

		return queryReader.retrieveByNamedQuery(queryName, flushMode, ignoreChanges, parameters);
	}

	/**
	 * Retrieves a collection of persistent instances with the specified named query. Parameter
	 * names should <i>not</i> be numerical.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the named list of parameters to be used with the given query
	 * @return a collection of persistable instances
	 */
	@Override
	public <T> List<T> retrieveByNamedQuery(final String queryName, final Map<String, Object> parameters) {
		return queryReader.retrieveByNamedQuery(queryName, parameters);
	}

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	@Override
	public <T> List<T> retrieveByNamedQuery(final String queryName, final int firstResult, final int maxResults) {
		return queryReader.retrieveByNamedQuery(queryName, firstResult, maxResults);
	}

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve.
	 * @return a list of persistent instances
	 */
	@Override
	public <T> List<T> retrieveByNamedQuery(final String queryName, final Object[] parameters, final int firstResult, final int maxResults) {
		return queryReader.retrieveByNamedQuery(queryName, parameters, firstResult, maxResults);
	}

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryName the named query
	 * @param parameterValuesMap the map of name - list values
	 * @return a list of persistent instances
	 */
	@Override
	public <T, E> List<T> retrieveByNamedQueryWithList(final String queryName, final Map<String, Collection<E>> parameterValuesMap) {
		return queryReader.retrieveByNamedQueryWithList(queryName, parameterValuesMap);
	}

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryName the named query
	 * @param listParameterName the name of the parameter for the list values
	 * @param values the collection of values
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	@Override
	public <T, E> List<T> retrieveByNamedQueryWithList(final String queryName, final String listParameterName,
		final Collection<E> values, final Object... parameters) {

		return queryReader.retrieveByNamedQueryWithList(queryName, listParameterName, values, parameters);
	}

	/**
	 * Retrieve a list of persistent instances with the specified named query.
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
	@Override
	public <T> List<T> retrieveByNamedQueryWithList(final String queryName, final String listParameterName, final Collection<?> values,
		final Object[] parameters, final int firstResult, final int maxResults) {

		return queryReader.retrieveByNamedQueryWithList(queryName, listParameterName, values, parameters, firstResult, maxResults);
	}

	// @@@@@@@@@@@@@@@@@ Modification operations @@@@@@@@@

	/**
	 * Update/delete all objects according to the given query.
	 *
	 * @param sql the HQL query
	 * @param parameters the prameters to be used with the criteria
	 * @return the number of instances updated/deleted
	 */
	@Override
	public int bulkUpdate(final String sql, final Object... parameters) {
		OpenJPAQuery<?> query = getQueryForDynamicQuery(sql);
		ChangeType changeType = getChangeTypeFor(query);
		fireBeginBulkOperationEvent(query, parameters, changeType);

		try {
			query.setParameters(parameters);

			return query.executeUpdate();

		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndBulkOperationEvent(changeType);
		}
	}

	/**
	 * Delete the given persistent instance.
	 *
	 * @param object the instance to delete
	 */
	@Override
	public void delete(final Persistable object) {
		try {

			fireBeginSingleOperationEvent(object, ChangeType.DELETE);

			Object persistObject = entityManager.getReference(object.getClass(), object.getUidPk());
			entityManager.remove(persistObject);

		} catch (final Exception e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndSingleOperationEvent(object, ChangeType.DELETE);
		}
	}

	/**
	 * Merge the given persistent instance.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to merge
	 * @return the merged object
	 */
	@Override
	public <T extends Persistable> T merge(final T object) {
		try {
			fireBeginSingleOperationEvent(object, ChangeType.UPDATE);

			if (object instanceof PersistenceInterceptor) {
				((PersistenceInterceptor) object).executeBeforePersistAction();
			}
			return entityManager.merge(object);

		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndSingleOperationEvent(object, ChangeType.UPDATE);
		}
	}

	/**
	 * Persist the given instance.
	 *
	 * @param object the instance to save.
	 */
	@Override
	public void save(final Persistable object) {
		try {
			fireBeginSingleOperationEvent(object, ChangeType.CREATE);

			if (object instanceof PersistenceInterceptor) {
				((PersistenceInterceptor) object).executeBeforePersistAction();
			}
			entityManager.persist(object);

		} catch (final Exception e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndSingleOperationEvent(object, ChangeType.CREATE);
		}
	}

	/**
	 * Save the persistable instance if it's new or merge the persistent instance if it exists.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to save or merge
	 * @return the merged object if it is merged, or the persisted object for save action
	 */
	@Override
	public <T extends Persistable> T saveOrMerge(final T object) {
		if (object.isPersisted()) {
			return merge(object);
		}

		save(object);
		return object;
	}

	/**
	 * Save the persistable instance if it's new or update the persistent instance if it exists.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to save or update
	 * @return the merged object if it is merged, or the persisted object for save action
	 */
	@Override
	public <T extends Persistable> T saveOrUpdate(final T object) {
		return saveOrMerge(object);
	}

	/**
	 * Update the given persistent instance.  If you update an object, you must use the returned object from the update operation.
	 * @param <T> the type of the object
	 * @param object the instance to update
	 * @return the instance that the state was merged to
	 */
	@Override
	public <T extends Persistable> T update(final T object) {
		try {
			fireBeginSingleOperationEvent(object, ChangeType.UPDATE);

			if (object instanceof PersistenceInterceptor) {
				((PersistenceInterceptor) object).executeBeforePersistAction();
			}
			return entityManager.merge(object);

		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndSingleOperationEvent(object, ChangeType.UPDATE);
		}
	}

	/**
	 * Execute an update or delete named query.
	 *
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @return the number of entities updated or deleted
	 */
	@Override
	public int executeNamedQuery(final String queryName, final Object... parameters) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing named query {}", queryName);
		}

		OpenJPAQuery<?> query = getPersistingQueryForNamedQuery(queryName);
		ChangeType changeType = getChangeTypeFor(query);
		fireBeginBulkOperationEvent(queryName, query, parameters, changeType);

		try {
			query.setParameters(parameters);

			return query.executeUpdate();
		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndBulkOperationEvent(changeType);
		}

	}

	@Override
	public int executeQuery(final String dynamicQuery, final Object... parameters) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing dynamic query {}", dynamicQuery);
		}

		OpenJPAQuery<?> query = getQueryForDynamicQuery(dynamicQuery);
		ChangeType changeType = getChangeTypeFor(query);
		fireBeginBulkOperationEvent("Dynamic Query", query, parameters, changeType);

		try {
			query.setParameters(parameters);

			return query.executeUpdate();
		} catch (final DataAccessException e) {
			throw new EpPersistenceException(CAUGHT_AN_EXCEPTION, e);
		} finally {
			fireEndBulkOperationEvent(changeType);
		}
	}

	/**
	 * Execute dynamic update/delete query with list parameter and optional other parameters.
	 *
	 * @param dynamicQuery he query to be executed.
	 * @param listParameterName the name of the parameter for the list values
	 * @param values the collection of values
	 * @param parameters the parameters to be used with the given query
	 * @param <E> the type of values used in the query
	 * @return the number of entities updated or deleted
	 *///DML
	public <E> int executeQueryWithList(final String dynamicQuery, final String listParameterName,
		final Collection<E> values, final Object... parameters) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("executing dynamic query {} with list {}", dynamicQuery, listParameterName);
		}

		OpenJPAQuery<?> query = getQueryForDynamicQuery(dynamicQuery);
		List<List<E>> listOfSubListsOfValues = splitCollection(values);

		int result = 0;
		for (List<E> subListOfValues : listOfSubListsOfValues) {
			query.setParameters(parameters);
			query.setParameter(listParameterName, subListOfValues);

			ChangeType changeType = getChangeTypeFor(query);
			fireBeginBulkOperationEvent("Dynamic Query", query, parameters, changeType);

			try {
				result += query.executeUpdate();
			} finally {
				fireEndBulkOperationEvent(changeType);
			}
		}
		return result;
	}

	/**
	 * Execute an update or delete named query.
	 *
	 * @param <E> the type of values used in the query
	 * @param queryName the named query
	 * @param listParameterName the name of the parameter for the list values
	 * @param values the collection of values
	 * @param parameters the parameters to be used with the given query
	 * @return the number of entities updated or deleted
	 */
	@Override//DML
	public <E> int executeNamedQueryWithList(final String queryName, final String listParameterName, final Collection<E> values,
		final Object... parameters) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("executing named query {}", queryName);
		}

		OpenJPAQuery<Integer> query = getPersistingQueryForNamedQuery(queryName);
		List<List<E>> listOfSubListsOfValues  = splitCollection(values);

		int result = 0;

		for (List<E> subListOfValues : listOfSubListsOfValues) {
			query.setParameters(parameters);
			query.setParameter(listParameterName, subListOfValues);

			ChangeType changeType = getChangeTypeFor(query);
			fireBeginBulkOperationEvent(queryName, query, parameters, changeType);
			try {
				result += query.executeUpdate();
			} finally {
				fireEndBulkOperationEvent(changeType);
			}
		}
		return result;
	}

	//@@@@@@@@@@@@ Custom methods @@@@@@@@@@@@@@@@@@@@@@@@@@@

	/**
	 * Returns JPA Entity Manager.
	 *
	 * @return JpaSession wrapper of JPA EntityManager
	 */
	@Override
	public PersistenceSession getPersistenceSession() {
		return getSessionFactory().createPersistenceSession();
	}

	/**
	 * Returns a shared session.
	 *
	 * @return a session
	 */
	@Override
	public PersistenceSession getSharedPersistenceSession() {
		return new ListeningPersistenceSessionImpl(entityManager, txManager, true, this);
	}

	/**
	 * Initialize the given object.
	 *
	 * @param object the object to initialize.
	 */
	@Override
	public void initialize(final Object object) {
		entityManager.refresh(object);
	}

	/**
	 * Begin a single persistence operation. This will add a listener to the broker
	 * if one has not already been added.
	 *
	 * @param object the subject of the operation
	 * @param changeType the type of operation being performed
	 */
	@Override
	public void fireBeginSingleOperationEvent(final Persistable object, final ChangeType changeType) {
		for (PersistenceEngineOperationListener listener : getPersistenceEngineOperationListeners()) {
			listener.beginSingleOperation(object, changeType);
		}
	}

	/**
	 * End a single persistence operation. This will remove any listeners added to
	 * the broker by fireBeginSingleOperationEvent().
	 *
	 * @param object the object that was the subject of the operation
	 * @param changeType the type of operation being performed
	 */
	@Override
	public void fireEndSingleOperationEvent(final Persistable object, final ChangeType changeType) {
		for (PersistenceEngineOperationListener listener : getPersistenceEngineOperationListeners()) {
			listener.endSingleOperation(object, changeType);
		}
	}

	/**
	 * Begin a bulk persistence operation. This will add a listener to the broker
	 * if one has not already been added.
	 *
	 * @param query the query being executed
	 * @param parameters parameters to the query
	 * @param changeType the type of operation being performed
	 */
	void fireBeginBulkOperationEvent(final OpenJPAQuery<?> query, final Object[] parameters, final ChangeType changeType) {
		fireBeginBulkOperationEvent(null, query, parameters, changeType);
	}

	/**
	 * Begin a bulk persistence operation. This will add a listener to the broker
	 * if one has not already been added.
	 *
	 * @param queryName the name of the query or null;
	 * @param query the query being executed
	 * @param parameters parameters to the query
	 * @param changeType the type of operation being performed
	 */
	void fireBeginBulkOperationEvent(final String queryName, final OpenJPAQuery<?> query, final Object[] parameters, final ChangeType changeType) {
		for (PersistenceEngineOperationListener listener : getPersistenceEngineOperationListeners()) {
			listener.beginBulkOperation(queryName, query.getQueryString(), Arrays.toString(parameters), changeType);
		}
	}

	/**
	 * End a bulk persistence operation. This will remove any listeners added to
	 * the broker by beginBulkOperation().
	 *
	 * @param changeType the type of operation being performed
	 */
	@SuppressWarnings("PMD.UnusedFormalParameter")
	void fireEndBulkOperationEvent(final ChangeType changeType) {
		for (PersistenceEngineOperationListener listener : getPersistenceEngineOperationListeners()) {
			listener.endBulkOperation();
		}
	}

	/**
	 * Get a direct connection.
	 * @return the connection
	 */
	@Override
	public Connection getConnection() {
		EntityManager newEntityManager = ((JpaPersistenceSession) sessionFactory.createPersistenceSession()).getEntityManager();
		return (Connection) OpenJPAPersistence.cast(newEntityManager).getConnection();
	}

	/**
	 * Indicate that the current transaction is a large transaction. This will
	 * tell JPA to more aggressively invalidate the cache.
	 *
	 * @param largeTransaction true to indicate a large transaction
	 */
	@Override
	public void setLargeTransaction(final boolean largeTransaction) {
		OpenJPAEntityManager oem = OpenJPAPersistence.cast(entityManager);
		oem.setTrackChangesByType(largeTransaction);
	}

	/**
	 * Get the OpenJPA Broker.
	 * @return the broker
	 */
	@Override
	public Broker getBroker() {
		OpenJPAEntityManager openjpaEM = getOpenJPAEntityManager();
		return JPAFacadeHelper.toBroker(openjpaEM);
	}

	/**
	 * Output some debug information on a persistable object.
	 * @param name - the name of the object
	 * @param object - the persistable object to debug
	 */
	@Override
	public void debugObject(final String name, final Persistable object) {
		StringBuilder debugMessage = new StringBuilder();
		OpenJPAEntityManager openjpaEM = OpenJPAPersistence.cast(entityManager);
		debugMessage.append("Status for ");
		debugMessage.append(name);
		debugMessage.append(": dirty=");
		debugMessage.append(openjpaEM.isDirty(object));
		debugMessage.append(", detached=");
		debugMessage.append(openjpaEM.isDetached(object));
		debugMessage.append(", persistent=");
		debugMessage.append(openjpaEM.isPersistent(object));
		debugMessage.append(", transactional=");
		debugMessage.append(openjpaEM.isTransactional(object));
		debugMessage.append(", lockMode=");
		debugMessage.append(openjpaEM.getLockMode(object));
		LOG.debug(debugMessage.toString());
	}


	// @@@@@@@@@@@@ Cache related operations @@@@@@@@@@@@@@@@@@@@@

	/**
	 * Flush the cache.
	 */
	@Override
	public void flush() {
		entityManager.flush();
	}

	/**
	 * Clear the cache.
	 */
	@Override
	public void clear() {
		entityManager.clear();
	}

	@Override
	public void evictObjectFromCache(final Persistable object) {
		getDataCache()
			.evict(object.getClass(), object.getUidPk());
	}

	/**
	 * Clear the JPA data cache and query results cache completely.
	 */
	@Override
	public void clearCache() {
		clearDataCache();
		clearQueryResultCache();
	}

	/**
	 * Indicate whether data caching is enabled or not.
	 *
	 * @return true if the data cache is enabled
	 */
	@Override
	public boolean isCacheEnabled() {
		StoreCache cache = getDataCache();
		if (cache == null) {
			return false;
		}
		if (cache instanceof StoreCacheImpl) {
			DataCache dataCache = ((StoreCacheImpl) cache).getDelegate();
			return dataCache != null;
		}
		return true;
	}

	/**
	 * Clear the Query Result Cache.
	 */
	protected void clearQueryResultCache() {
		getQueryResultCache().evictAll();
	}

	/**
	 * Evict all items from the OpenJPA Data Cache.
	 */
	protected void clearDataCache() {
		getDataCache().evictAll();
	}

	private OpenJPAEntityManagerFactory getOpenJPAEntityManagerFactory() {
		EntityManagerFactory emf = ((JpaSessionFactoryImpl) sessionFactory).getEntityManagerFactory();
		return OpenJPAPersistence.cast(emf);
	}

	/**
	 * Get the OpenJPA Entity Manager.
	 *
	 * @return an <code>OpenJPAEntityManager</code> instance
	 */
	protected OpenJPAEntityManager getOpenJPAEntityManager() {
		return OpenJPAPersistence.cast(entityManager);
	}

	private ChangeType getChangeTypeFor(final OpenJPAQuery<?> query) {
		ChangeType changeType = null;
		switch (query.getOperation()) {
		case DELETE :
			changeType = ChangeType.DELETE;
			break;
		case UPDATE :
			changeType = ChangeType.UPDATE;
			break;
		default:
			//Ignore
		}
		return changeType;
	}

	@Override
	public <T> T detach(final T object) {
		final OpenJPAEntityManager openJPAEntityManager = OpenJPAPersistence.cast(entityManager);
		if (openJPAEntityManager.isDetached(object)) {
			return object;
		}
		return openJPAEntityManager.detachCopy(object);
	}

	protected List<PersistenceEngineOperationListener> getPersistenceEngineOperationListeners() {
		return Collections.unmodifiableList(operationListeners);
	}

	@Override
	public void setPersistenceEngineOperationListeners(final List<PersistenceEngineOperationListener> persistenceEngineListeners) {
		this.operationListeners = persistenceEngineListeners;
	}

	@Override
	public void addPersistenceEngineOperationListener(final PersistenceEngineOperationListener listener) {
		List<PersistenceEngineOperationListener> listenerCopy =
			new ArrayList<>(getPersistenceEngineOperationListeners());
		listenerCopy.add(listener);

		setPersistenceEngineOperationListeners(listenerCopy);
	}

	@Override
	public void removePersistenceEngineOperationListener(final PersistenceEngineOperationListener listener) {
		List<PersistenceEngineOperationListener> listenerCopy =
			new ArrayList<>(getPersistenceEngineOperationListeners());
		listenerCopy.remove(listener);

		setPersistenceEngineOperationListeners(listenerCopy);
	}

	@Override
	public PersistenceEngine withLoadTuners(final LoadTuner... loadTuners) {
		fetchPlanHelper.setLoadTuners(loadTuners);
		return this;
	}

	@Override
	public PersistenceEngine withCollectionOfLazyFields(final Map<Class<?>, Collection<String>> lazyFields) {
		fetchPlanHelper.setCollectionOfLazyFields(lazyFields);
		return this;
	}

	@Override
	public PersistenceEngine withLazyFields(final Map<Class<?>, String> lazyFields) {
		fetchPlanHelper.setLazyFields(lazyFields);
		return this;
	}

	@Override
	public PersistenceEngine withLazyFields(final Class<?> clazz, final Collection<String> lazyFields) {
		fetchPlanHelper.setLazyFields(clazz, lazyFields);
		return this;
	}

	/**
	 * Get the Entity Manager.
	 *
	 * @return the EntityManager
	 */
	@Override
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Set the Entity Magager.
	 *
	 * @param entityManager the EntityManager to use
	 */
	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Get the persistence session factory.
	 * @return an instance of <code>PersistenceSessionFactory</code>
	 */
	@Override
	public PersistenceSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set the persistence session factory.
	 * @param sessionFactory the factory
	 */
	public void setSessionFactory(final PersistenceSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Sets the transaction manager.
	 *
	 * @param txManager the transaction manager
	 */
	public void setTransactionManager(final PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public PlatformTransactionManager getTxManager() {
		return txManager;
	}

	private StoreCache getDataCache() {
		return getOpenJPAEntityManagerFactory().getStoreCache();
	}

	private QueryResultCache getQueryResultCache() {
		return getOpenJPAEntityManagerFactory().getQueryResultCache();
	}

	private EntityManager createNewEntityManager() {
		PersistenceSession newSession = sessionFactory.createPersistenceSession();
		return ((JpaPersistenceSession) newSession).getEntityManager();
	}

	/*
	 Returned query will participate in a persisting operation (INSERT, UPDATE, DELETE)
	 */
	@SuppressWarnings("unchecked")
	private <T> OpenJPAQuery<T> getPersistingQueryForNamedQuery(final String queryName) {
		try {
			return OpenJPAPersistence.cast(entityManager.createNamedQuery(queryName));
		} catch (final DataAccessException e) {
			throw new EpPersistenceException("Exception was thrown when create named query.", e);
		}
	}

	/*
		DML query is built dynamically
	 */
	@SuppressWarnings("unchecked")
	private <T> OpenJPAQuery<T> getQueryForDynamicQuery(final String dynamicQuery) {
		try {
			return createDynamicJPQLQuery(entityManager, dynamicQuery);
		} catch (final DataAccessException e) {
			throw new EpPersistenceException("Exception was thrown when create named query.", e);
		}
	}

	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}

	public FetchPlanHelper getFetchPlanHelper() {
		return fetchPlanHelper;
	}

	public QueryReaderFactory getQueryReaderFactory() {
		return queryReaderFactory;
	}

	public void setQueryReaderFactory(final QueryReaderFactory queryReaderFactory) {
		this.queryReaderFactory = queryReaderFactory;
	}
}
