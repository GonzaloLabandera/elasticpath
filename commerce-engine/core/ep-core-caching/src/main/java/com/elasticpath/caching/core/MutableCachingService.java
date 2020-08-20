/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core;

import com.elasticpath.persistence.api.Persistable;

/**
 * Caching service that supports updating and invalidating the cached entities.
 *
 * @param <E> the entity class
 */
public interface MutableCachingService<E extends Persistable> {

	/**
	 * Updates the entity in any of this service's caches.
	 *
	 * @param entity the entity
	 */
	void cache(E entity);

	/**
	 * Removes the entity from any of this service's caches.
	 *
	 * @param entity the entity
	 */
	void invalidate(E entity);

	/**
	 * Removes all entities from any of this service's caches.
	 */
	void invalidateAll();
}
