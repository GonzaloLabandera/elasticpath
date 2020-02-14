/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.executors;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.getEntityClassName;

import javax.persistence.EntityManager;

import com.elasticpath.persistence.api.Persistable;

/**
 * The executor for queries like <strong>find(EntityImpl, UIDPK)</strong>.
 * E.g. em.find(CustomerImpl, 123);
 *
 * @param <T> any {@link Persistable} type
 */
@SuppressWarnings("rawtypes")
public class IdentityQueryExecutor<T extends Persistable> extends AbstractQueryExecutor {

	private Class<T> clazz;
	private Long uidPk;

	/**
	 * Set the entity class to load.
	 *
	 * @param clazz the class.
	 * @return the current instance of {@link IdentityQueryExecutor}
	 */
	public IdentityQueryExecutor withClass(final Class<T> clazz) {
		this.clazz = clazz;

		return  this;
	}

	/**
	 * Set the ID to load entity class for.
	 *
	 * @param uidPk the entity's UidPk field value.
	 * @return the current instance of {@link IdentityQueryExecutor}
	 */
	public IdentityQueryExecutor withUidPk(final Long uidPk) {
		this.uidPk = uidPk;

		return  this;
	}

	@Override
	public T executeSingleResultQuery(final EntityManager entityManager) {
		return entityManager.find(clazz, uidPk);
	}

	/**
	 * The query is a textual representation of what {@link EntityManager#find(Class, Object)} does and it's <strong>not</strong> executed as such.
	 * It's used solely for the routing purposes, because routing is query-based.
	 *
	 * @return the query like e.g. SELECT c FROM ProductImpl c WHERE c.uidPk=? for given ProductImpl class
	 */
	@Override
	public String getQuery() {
		return "SELECT c FROM " + getEntityClassName(clazz) + " c WHERE c.uidPk = ?1";
	}


}
