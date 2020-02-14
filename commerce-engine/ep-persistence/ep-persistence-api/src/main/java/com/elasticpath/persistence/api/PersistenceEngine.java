/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.persistence.api;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The persistence engine interface.
 * Data-modification methods, like save, update, delete, dynamic updates etc
 * may throw unchecked {@link EpPersistenceException}.
 *
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface PersistenceEngine {

	/**
	 * Persist the given instance.
	 *
	 * @param object the instance to save.
	 */
	void save(Persistable object);

	/**
	 * Retrieve a list of persistent instances with the specified query and bounds, namely,
	 * the maximum number of rows to retrieve and / or the first row to retrieve.
	 * This help to support pagination.
	 *
	 * @param <T> the object's type to retrieve
	 * @param query the query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieve(String query, int firstResult, int maxResults);

	/**
	 * Retrieve a list of persistent instances with the specified query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param query the JPQL query
	 * @param values the collection of values
	 * @param listParameterName the name of the parameter for the list values
	 * @param parameters the parameters to be used with the criteria
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances.
	 */
	<T, E> List<T> retrieveWithList(String query, String listParameterName, Collection<E> values, Object[] parameters, int firstResult,
		int maxResults);

	/**
	 * Retrieve a list of persistent instances with the specified query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param query the query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieve(String query, Object... parameters);

	/**
	 * Retrieve a list of persistent instances with the specified query.  Parameter names and values are
	 * specified in pairs: name1, val1, name2, val2, etc.  For example:
	 *
	 * <pre>
	 *    Map<String, Object> params = new HashMap<String, Object>();
	 *    params.put("color", Color.RED);
	 *    params.put("size", 5);
	 *    params.put("shapes", Arrays.asList(Shape.CIRCLE, Shape.SQUARE));
	 *
	 *    List<Entity> entities = getPersistenceEngine().retrieveWithNamedParameters(
	 *       "SELECT e FROM Entity e WHERE e.color = :color AND e.size = :size AND e.shape in (:shapes)",
	 *       params);
	 * </pre>
	 *
	 * @param query the query
	 * @param parameters the parameter names and values to be used with the given query
	 * @param <T> the entity type returned by the query
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieveWithNamedParameters(String query, Map<String, ?> parameters);

	/**
	 * Retrieve a list of persistent instances with the specified query. This method will create a new
	 * session (EntityManager) to excute the query, and close the new session when completed.
	 *
	 * @param <T> the object's type to retrieve
	 * @param query the HQL query
	 * @param parameters the prameters to be used with the criteria
	 * @return a list of persistent instances.
	 */
	<T> List<T> retrieveWithNewSession(String query, Object... parameters);

	/**
	 * Retrieve a list of persistent instances with the specified query. This method will create a new
	 * session (EntityManager) to excute the query, and close the new session when completed.
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param query the HQL query
	 * @param values the collection of values
	 * @param listParameterName the name of the parameter for the list values
	 * @param parameters the parameters to be used with the criteria
	 * @return a list of persistent instances.
	 */
	<T, E> List<T> retrieveWithListWithNewSession(String query, String listParameterName, Collection<E> values, Object... parameters);

	/**
	 * Retrieve a list of persistent instances with the specified query and bounds, namely,
	 * the maximum number of rows to retrieve and / or the first row to retrieve.
	 * This help to support pagination.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryStr the HQL query string to be executed
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @param parameters the parameters to be used with the criteria
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieve(String queryStr, Object[] parameters, int firstResult, int maxResults);

	/**
	 * Update the given persistent instance.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to update
	 * @return the instance that the state was merged to
	 */
	<T extends Persistable> T update(T object);

	/**
	 * Save the persistable instance if it's new or update the persistent instance if it exists.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to save or update
	 * @return the merged object if it is merged, or the persisted object for save action
	 */
	<T extends Persistable> T saveOrUpdate(T object);

	/**
	 * Merge the given persistent instance.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to merge
	 * @return the merged object
	 */
	<T extends Persistable> T merge(T object);

	/**
	 * Save the persistable instance if it's new or merge the persistent instance if it exists.
	 *
	 * @param <T> the type of the object
	 * @param object the instance to save or merge
	 * @return the merged object if it is merged, or the persisted object for save action
	 */
	<T extends Persistable> T saveOrMerge(T object);

	/**
	 * Delete the given persistent instance.
	 *
	 * @param object The instance to delete
	 */
	void delete(Persistable object);

	/**
	 * Load a persistent instance with the given id. Throw an unrecoverable exception if there is no matching database row.
	 *
	 * @param <T> the type of the object
	 * @param persistenceClass the persistent class of the given id.
	 * @param uidPk the persistent instance id.
	 * @return the persistent instance
	 */
	<T extends Persistable> T load(Class<T> persistenceClass, long uidPk);

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
	<T extends Persistable> T loadWithNewSession(Class<T> persistenceClass, long uidPk);

	/**
	 * Get a persistent instance with the given id. Return null if no matching record exists.
	 *
	 * @param <T> the type of the object
	 * @param persistenceClass the persistent class of the given id.
	 * @param uidPk the persistent instance id.
	 * @return the persistent instance
	 */
	<T extends Persistable> T get(Class<T> persistenceClass, long uidPk);

	/**
	 * Flush the cache.
	 */
	void flush();

	/**
	 * Clear the cache.
	 */
	void clear();

	/**
	 * Initialize the given object.
	 *
	 * @param object the object to initialize.
	 */
	void initialize(Object object);

	/**
	 * Returns a session.
	 *
	 * @return a session
	 */
	PersistenceSession getPersistenceSession();

	/**
	 * Returns a shared session.
	 *
	 * @return a session
	 */
	PersistenceSession getSharedPersistenceSession();

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
	<T> List<T> retrieve(List<String> queries, Object... parameters);


	/**
	 * Update/delete all objects according to the given query.
	 *
	 * @param query the HQL query
	 * @param parameters the parameters to be used with the criteria
	 * @return the number of instances updated/deleted
	 */
	int bulkUpdate(String query, Object... parameters);

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieveByNamedQuery(String queryName, Object... parameters);

	/**
	 * Retrieve a list of persistent instances with the specified named query, using the given FlushMode.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param flushMode the flush mode to use when executing the query
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieveByNamedQuery(String queryName, FlushMode flushMode, Object... parameters);

	/**
	 * Retrieve a list of persistent instances with the specified named query and parameters, as well as the combination of
	 * flush mode and ignore-changes flag.
	 *
	 * Using the right combination of flush mode and ignore-changes flag, the query results may be obtained from the local cache or the db.
	 *
	 * @see @link{http://openjpa.apache.org/builds/2.4.0/apache-openjpa/docs/manual.html#ref_guide_dbsetup_retain}
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param flushMode the flush mode to use when executing the query
	 * @param ignoreChanges whether to ignore changes in local cache
	 * @param parameters the parameters to be used with the given query
	 * @return a list of persistent instances
	 * @throws EpPersistenceException in case of persistence errors
	 */
	<T> List<T> retrieveByNamedQuery(String queryName, FlushMode flushMode, boolean ignoreChanges, Object[] parameters);

	/**
	 * Retrieves a collection of persistent instances with the specified named query. Parameter
	 * names should <i>not</i> be numerical.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the named list of parameters to be used with the given query
	 * @return a collection of persistable instances
	 */
	<T> List<T> retrieveByNamedQuery(String queryName, Map<String, Object> parameters);

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances.
	 */
	<T> List<T> retrieveByNamedQuery(String queryName, int firstResult, int maxResults);

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @param firstResult the first row to retrieve
	 * @param maxResults the maximum number of rows to retrieve
	 * @return a list of persistent instances
	 */
	<T> List<T> retrieveByNamedQuery(String queryName, Object[] parameters, int firstResult, int maxResults);

	/**
	 * Retrieve a list of persistent instances with the specified named query.
	 *
	 * @param <T> the object's type to retrieve
	 * @param <E> the type of values used in the query
	 * @param queryName the named query
	 * @param parameterValuesMap the map of name - list values
	 * @return a list of persistent instances
	 */
	<T, E> List<T> retrieveByNamedQueryWithList(String queryName, Map<String, Collection<E>> parameterValuesMap);

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
	<T, E> List<T> retrieveByNamedQueryWithList(String queryName, String listParameterName, Collection<E> values, Object... parameters);

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
	<T> List<T> retrieveByNamedQueryWithList(String queryName, String listParameterName, Collection<?> values, Object[] parameters,
			int firstResult, int maxResults);

	/**
	 * Execute an update or delete named query.
	 *
	 * @param queryName the named query
	 * @param parameters the parameters to be used with the given query
	 * @return the number of entities updated or deleted
	 */
	int executeNamedQuery(String queryName, Object... parameters);

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
	<E> int executeNamedQueryWithList(String queryName, String listParameterName, Collection<E> values, Object... parameters);

	/**
	 * Execute dynamic update/delete query.
	 *
	 * @param query the query to be executed.
	 * @param parameters the query parameters.
	 * @return the number of entities updated or deleted
	 */
	int executeQuery(String query, Object... parameters);

	/**
	 * Execute dynamic update/delete query with list parameter and optional other parameters.
	 *
	 * @param query he query to be executed.
	 * @param listParameterName the name of the parameter for the list values
	 * @param values the collection of values
	 * @param parameters the parameters to be used with the given query
	 * @param <E> the type of values used in the query
	 * @return the number of entities updated or deleted
	 */
	<E> int executeQueryWithList(String query, String listParameterName, Collection<E> values, Object... parameters);

	/**
	 * Get the persistence session factory.
	 * @return an instance of <code>PersistenceSessionFactory</code>
	 */
	PersistenceSessionFactory getSessionFactory();

	/**
	 * Get a direct connection.
	 * @return the connection
	 */
	Connection getConnection();

	/**
	 * Output some debug information on a persistable object.
	 * @param name - the name of the persistable object
	 * @param object - the persistable object to debug
	 */
	void debugObject(String name, Persistable object);

	/**
	 * Remove data for the given object from the cache.
	 *
	 * @param object the Persistence object to evict.
	 */
	void evictObjectFromCache(Persistable object);

	/**
	 * Clear the cache completely.
	 */
	void clearCache();

	/**
	 * Indicate whether caching is enabled or not.
	 *
	 * @return true if the data cache is enabled
	 */
	boolean isCacheEnabled();

	/**
	 * Indicate to the persistence engine that the current transaction
	 * is large so it will treat caching appropriately.
	 *
	 * @param largeTransaction set the large transaction mode
	 */
	void setLargeTransaction(boolean largeTransaction);

	/**
	 * Detach the specified object from the entity manager.
	 *
	 * @param <T> the type of the object
	 * @param object the object to detach
	 * @return the detached instance
	 */
	<T> T detach(T object);

	/**
	 * Removes the current set of PersistenceEngineOperationListeners are replaces them with the new set provided.
	 *
	 * @param persistenceEngineListeners the new list of PersistenceEngineOperationListeners to bombard with events
	 */
	void setPersistenceEngineOperationListeners(List<PersistenceEngineOperationListener> persistenceEngineListeners);

	/**
	 * Adds a PersistenceEngineOperationListener which will receive events when persistence operations are started and ended.
	 * @param listener the listener to add
	 */
	void addPersistenceEngineOperationListener(PersistenceEngineOperationListener listener);

	/**
	 * Removes a PersistenceEngineOperationListener which will no longer receive events when persistence operations are started and ended.
	 * @param listener the listener to remove
	 * TODO remove the method; not used
	 */
	void removePersistenceEngineOperationListener(PersistenceEngineOperationListener listener);

	/**
	 * Set one or more load tuners.
	 *
	 * @param loadTuners an array of load tuners.
	 * @return the current instance of {@link PersistenceEngine}
	 */
	PersistenceEngine withLoadTuners(LoadTuner... loadTuners);

	/**
	 * Set a map with pairs of Class and a collection of lazy fields to be loaded.
	 * E.g CustomerImpl.class, {"preferredShippingAddress", "customerProfile"}
	 *
	 * @param lazyFields a map with lazy fields to be loaded.
	 * @return the current instance of {@link PersistenceEngine}
	 */
	PersistenceEngine withCollectionOfLazyFields(Map<Class<?>, Collection<String>> lazyFields);

	/**
	 * Set a map with pairs of Class and a lazy field to be loaded.
	 * E.g CustomerImpl.class, "preferredShippingAddress"
	 *
	 * @param lazyFields a map with lazy fields to be loaded.
	 * @return the current instance of {@link PersistenceEngine}
	 */
	PersistenceEngine withLazyFields(Map<Class<?>, String> lazyFields);

	/**
	 * Set a class and a collection of lazy fields to be loaded.
	 *
	 * @param clazz the target class to load lazy fields for.
	 * @param lazyFields a collection of lazy fields to be loaded.
	 * @return the current instance of {@link PersistenceEngine}
	 */
	PersistenceEngine withLazyFields(Class<?> clazz, Collection<String> lazyFields);
}
