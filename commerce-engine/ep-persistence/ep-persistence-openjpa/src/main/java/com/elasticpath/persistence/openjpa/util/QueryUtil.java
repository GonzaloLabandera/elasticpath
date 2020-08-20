/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.FlushMode;

/**
 * A util class for query-related operations.
 */
public final class QueryUtil {

	/** Some dbs, like Oracle, allows limited number of parameters in the IN clause (1000 for Oracle).
	 *  This constant has a lower value that ensures safe setting of list parameters.
	 */
	public static final int MAX_ALLOWED_LIST_PARAMETERS = 900;

	private QueryUtil() {
		//empty constructor
	}
	/**
	 * Get query results.
	 *
	 * @param query the query.
	 * @param <T> the entity type.
	 * @return the results.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getResults(final Query query) {
		return new ArrayList<>((List<T>) query.getResultList());
	}

	/**
	 * Convert {@link FlushMode} to {@link FlushModeType}.
	 *
	 * @param flushMode the flush mode.
	 * @return the flush mode type.
	 */
	public static FlushModeType toFlushModeType(final FlushMode flushMode) {
		if (flushMode == FlushMode.AUTO) {
			return FlushModeType.AUTO;
		} else if (flushMode == FlushMode.COMMIT) {
			return FlushModeType.COMMIT;
		}

		throw new EpPersistenceException("Unknown flush mode: " + flushMode);
	}

	/**
	 * Split a collection of values into batches since some databases impose a restriction on number of elements that can be set in the IN clause.
	 * Also, it is advisable to split huge collections to avoid large result sets.
	 *
	 * @param values the collection of values to split.
	 * @param <V> value type.
	 * @return the list of strings containing a group of values
	 */
	public static <V> List<List<V>> splitCollection(final Collection<V> values) {
		List<List<V>> listOfSubLists = new ArrayList<>();

		if (values.isEmpty()) {
			return listOfSubLists;
		}

		//can't break the interface, but Lists.partition works only with List
		List<V> valuesCopy = ImmutableList.copyOf(values);

		if (values.size() > MAX_ALLOWED_LIST_PARAMETERS) {
			return Lists.partition(valuesCopy, MAX_ALLOWED_LIST_PARAMETERS);
		}

		listOfSubLists.add(valuesCopy);

		return listOfSubLists;
	}

	/**
	 * Return entity class name either from Entity#name() or Class#getSimpleName().
	 *
	 * @param entityClass the class to obtain the name from
	 * @return String representing a class name
	 */
	public static String getEntityClassName(final Class<?> entityClass) {
		String entityClassName = "";

		Annotation entityAnnotation = entityClass.getDeclaredAnnotation(Entity.class);

		if (entityAnnotation != null) { //sanity check - entity classes must be annotated with @Entity
			try {
				Method nameMethod = entityAnnotation.getClass().getDeclaredMethod("name");
				entityClassName = nameMethod.invoke(entityAnnotation).toString();

			} catch (Exception e) {
				//do nothing
			}
		}

		if (StringUtils.isBlank(entityClassName)) {
			entityClassName = entityClass.getSimpleName();
		}

		return entityClassName;
	}

	/**
	 * Create a new instance of {@link OpenJPAQuery} from a given JPQL string.
	 *
	 * @param entityManager the {@link EntityManager}
	 * @param dynamicJPQLQuery the dynamic JPQL query string
	 * @return a new {@link OpenJPAQuery}
	 */
	@SuppressWarnings("rawtypes")
	public static OpenJPAQuery createDynamicJPQLQuery(final EntityManager entityManager, final String dynamicJPQLQuery) {
		return OpenJPAPersistence.cast(entityManager.createQuery(dynamicJPQLQuery));
	}

	/**
	 * Create a new instance of {@link OpenJPAQuery} from a given native SQL string.
	 *
	 * @param entityManager the {@link EntityManager}
	 * @param dynamicSQLQuery the dynamic SQL query string
	 * @param <T> the entity type
	 * @return a new {@link OpenJPAQuery}
	 */
	@SuppressWarnings("unchecked")
	public static <T> OpenJPAQuery<T> createDynamicSQLQuery(final EntityManager entityManager, final String dynamicSQLQuery) {
		return OpenJPAPersistence.cast(entityManager.createNativeQuery(dynamicSQLQuery));
	}
}
