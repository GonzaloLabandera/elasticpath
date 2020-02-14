/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.persistence.openjpa.support;

import org.apache.openjpa.enhance.PersistenceCapable;

import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.support.PersistablePostLoadStrategy;

/**
 * The abstract strategy used for post-loading eager fields. The implementing classes must provide the name of the field to be loaded, as well as
 * the loading implementation.
 *
 * @param <P> the persistable type
 */
public abstract class AbstractEagerFieldPostLoadStrategy<P extends Persistable> implements PersistablePostLoadStrategy<P> {

	/**
	 * Return the name of the field to be loaded.
	 *
	 * @return the field name
	 */
	public abstract String getFieldName();

	/**
	 * Return the object for the field to be loaded. It's typically implemented by making a cache-backed service call and setting the
	 * result to the target field.
	 *
	 * @param persistable the entity class, owner of the eager field
	 * @return the object to be set
	 */
	public abstract Object fetchObjectToLoad(P persistable);

	@Override
	@SuppressWarnings("unchecked")
	public void process(final Persistable persistable) {
		PersistenceCapable persistenceCapableEntity = (PersistenceCapable) persistable;
		if (!JPAUtil.isFieldLoaded(persistenceCapableEntity, getFieldName())) {
			Object objectToLoad = fetchObjectToLoad((P) persistable);
			JPAUtil.loadField(persistenceCapableEntity, getFieldName(), objectToLoad);
		}
	}
}
