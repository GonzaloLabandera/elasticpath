/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.persistence.openjpa.support;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAPersistence;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngine;


/**
 * Provides various JPA utility methods.
 */
public final class JPAUtil {

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

}
