/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.persistence.openjpa.support;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAPersistence;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngine;


/**
 * Provides various JPA utility methods.
 */
public final class JPAUtil {

	/**
	 * Postgresql database type string as returned by JPA.
	 */
	public static final String POSTGRESQL_DB_TYPE = "postgresql";

	/**
	 * MySql database type string as returned by JPA.
	 */
	public static final String MYSQL_DB_TYPE = "mysql";

	/**
	 * Oracle database type string as returned by JPA.
	 */
	public static final String ORACLE_DB_TYPE = "oracle";

	private JPAUtil() {
		//default constructor
	}

	/**
	 * Check if entity field is loaded by JPA framework.
	 * It does that by obtaining a BitSet of loaded fields from the {@link org.apache.openjpa.kernel.StateManagerImpl}
	 * and checking whether the field's bit is set or not.
	 *
	 * @param entity the entity
	 * @param fieldName the field to check whether is loaded
	 * @return true, if field is loaded
	 */
	public static boolean isFieldLoaded(final PersistenceCapable entity, final String fieldName) {
		OpenJPAStateManager stateManager = getStateManager(entity);

		int fieldIndex = stateManager.getMetaData().getField(fieldName).getIndex();

		return stateManager.getLoaded().get(fieldIndex);
	}

	/**
	 * This method allows loading of lazy fields with values obtained using
	 * {@link AbstractEagerFieldPostLoadStrategy#fetchObjectToLoad(com.elasticpath.persistence.api.Persistable)} method.
	 *
	 * The method internally generally does 3 things:
	 *
	 * 1. removes {@link org.apache.openjpa.enhance.StateManager}
	 * 2. sets the value to the lazy field via reflection
	 * 3. restores the {@link org.apache.openjpa.enhance.StateManager}
	 *
	 * The reason for removing the state manager is because any change made under active session (and state manager)
	 * triggers change events that are processed by various lifecycle events (e.g. ChangeSetEventListener) leading
	 * to false positive changes.
	 *
	 * However, because entities are enhanced and state manager removed, regular setter methods do not work so the last
	 * resort is to use reflection to set the field.
	 *
	 * The final step of restoring the state manager is preceded with setting a "loaded" flag in the state manager's
	 * BitSet of loaded fields. Failure to do that will cause {@link IllegalAccessException} when field is managed by the
	 * {@link org.apache.openjpa.kernel.DetachedStateManager}.
	 *
	 * @param entity the entity to load lazy field for
	 * @param fieldName the lazy field name
	 * @param objectToLoad the object to set to the lazy field
	 */
	public static void loadField(final PersistenceCapable entity, final String fieldName, final Object objectToLoad) {
		/* preserve state manager since it has to be restored later on.
		   the reason for this is that we don't want to trigger any lifecycle event
		   (e.g. field changed) that may be (wrongfully) processed by a listener (e.g. ChangeSetEventListener).
		 */
		OpenJPAStateManager stateManager = getStateManager(entity);
		int fieldIndex = stateManager.getMetaData().getField(fieldName).getIndex();

		//disable state manager
		entity.pcReplaceStateManager(null);

		//using setter will not work when state manger is disabled; using reflection is the only way to set the field
		try {
			Field field = entity.getClass()
				.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(entity, objectToLoad);

			//inform the state manager that field is loaded
			stateManager.getLoaded().set(fieldIndex);
			//also that entity is dirty
			stateManager.getDirty().set(fieldIndex);
		} catch (Exception e) {
			throw new EpPersistenceException("Error occured while setting field: " + fieldName, e);
		}

		//restore the state manager
		entity.pcReplaceStateManager(stateManager);
	}

	/**
	 * This method only sets the "loaded" flag for the provided entity and it's field name.
	 * It works only with {@link org.apache.openjpa.kernel.StateManagerImpl} because the meta-data,
	 * required for the resolution of the field's index, is not available in {@link org.apache.openjpa.kernel.DetachedStateManager}.
	 *
	 * The lazy fields can't be accessed from detached entities and this method enables the access.
	 * Without this method, the handling of lazy fields would be significantly difficult.
	 *
	 * @param entity the owner of the field that needs to be marked as loaded
	 * @param fieldName the field to be marked as loaded
	 */
	public static void markFieldAsLoaded(final PersistenceCapable entity, final String fieldName) {
		OpenJPAStateManager stateManager = getStateManager(entity);
		int fieldIndex = stateManager.getMetaData().getField(fieldName).getIndex();

		if (stateManager != null) {
			stateManager.getLoaded().set(fieldIndex);
		}
	}

	private static OpenJPAStateManager getStateManager(final PersistenceCapable entity) {
		return (OpenJPAStateManager) entity.pcGetStateManager();
	}

	/**
	 * Returns the id for the current transaction from the managed runtime.
	 *
	 * @param jpaPersistenceEngine the JPA persistence engine
	 * @return the transaction id
	 * @throws Exception if there is an issue retrieving the transaction key
	 */
	public static String getTransactionId(final JpaPersistenceEngine jpaPersistenceEngine) throws Exception {
		ManagedRuntime runtime = jpaPersistenceEngine.getBroker().getManagedRuntime();
		Object key = runtime.getTransactionKey();
		int transactionId = key.hashCode();
		return String.valueOf(transactionId);
	}

	/**
	 * Determines if the passed entity is dirty.
	 *
	 * @param persistenceCapable the entity object
	 * @param jpaPersistenceEngine the JPA persistence engine
	 * @return true if the entity is dirty
	 */
	@SuppressWarnings("unchecked")
	public static boolean isDirty(final PersistenceCapable persistenceCapable, final JpaPersistenceEngine jpaPersistenceEngine) {
		if (persistenceCapable.pcIsDirty()) {
			return true;
		}

		EntityManager entityManager = jpaPersistenceEngine.getEntityManager();
		ClassMetaData metaData = JPAFacadeHelper.getMetaData(entityManager, persistenceCapable.getClass());

		Set<Class<?>> collect = (Set<Class<?>>) OpenJPAPersistence.cast(entityManager).getDirtyObjects().stream()
				.map(Object::getClass)
				.collect(Collectors.toSet());

		return Stream.of(metaData.getDeclaredFields())
				.map(FieldMetaData::getRelationType)
				.anyMatch(collect::contains);
	}

	/**
	 * Determines if the passed entity has actually been written to the database or not.
	 *
	 * @param persistenceCapable the entity object
	 * @return true if the entity has been flushed to the database, false if it is still only in memory.
	 */
	@SuppressWarnings("unchecked")
	public static boolean isNew(final PersistenceCapable persistenceCapable) {
		return persistenceCapable.pcIsNew();
	}

	/**
	 * Determines if the passed entity has an assigned state manager or not.
	 *
	 * @param persistenceCapable the entity object
	 * @return true if the entity has a StateManager, false otherwise.
	 */
	public static boolean hasStateManager(final PersistenceCapable persistenceCapable) {
		return persistenceCapable.pcGetStateManager() != null;
	}

	/**
	 * Return native query raw string using named native query name.
	 *
	 * @param entityManager the entity manager, used as a {@link Broker}
	 * @param nativeQueryName the native query name
	 * @return the native query string
	 */
	public static String getNativeQueryStringByQueryName(final EntityManager entityManager, final String nativeQueryName) {
		Broker broker = JPAFacadeHelper.toBroker(OpenJPAPersistence.cast(entityManager));

		QueryMetaData queryMetaData = broker
				.getConfiguration()
				.getMetaDataRepositoryInstance()
				.getQueryMetaData(null, nativeQueryName, broker.getClassLoader(), true);

		return queryMetaData.getQueryString();
	}

	/**
	 * Native queries with IN clause, must be handled explicitly, because current OpenJPA API doesn't support an array/list of parameter values.
	 * Out-of-the-box, JPQLs with IN clause are handled at runtime by replacing the placeholder with the required number of "?".
	 *
	 * E.g SELECT e FROM Entity e where e.uidPK IN (:list) and the list of values is 1,2,3
	 * the resulting SQL will be SELECT t.FIELD1, ... FROM TABLE t  where t.uidPk IN (?,?,?).
	 *
	 * There is no counterpart for native queries.
	 *
	 * @param rawNativeQuery raw native query string
	 * @param numOfListParamValues the number of list values
	 * @return modified native query string
	 */
	public static String expandListParameterForNativeQuery(final String rawNativeQuery, final int numOfListParamValues) {
		String csvQuestionMarks = StringUtils.repeat("?,", numOfListParamValues);
		csvQuestionMarks = StringUtils.removeEnd(csvQuestionMarks, ",");

		//handle surrounding spaces
		return  rawNativeQuery.replaceFirst("IN\\s+\\(\\s*\\?\\s*\\)",
		  "IN (" + csvQuestionMarks + ")");
	}

	/**
	 * Get database type from a connection.
	 *
	 * @param jpaPersistenceEngine the persistence engine to obtain a connection from
	 * @return lower-cased database type
	 */
	public static String getDatabaseType(final PersistenceEngine jpaPersistenceEngine) {
		// Get the database type from the database connection
		try (Connection connection = jpaPersistenceEngine.getConnection()) {
			return connection.getMetaData().getDatabaseProductName().toLowerCase();
		} catch (SQLException sqlException) {
			throw new EpServiceException("Error occurred while getting database connection", sqlException);
		}
	}
}
