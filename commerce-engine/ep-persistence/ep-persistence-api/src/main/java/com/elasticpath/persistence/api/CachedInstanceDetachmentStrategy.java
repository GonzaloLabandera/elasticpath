/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.persistence.api;

/**
 * The strategy interface for detaching domain instances before caching.
 *
 * Under active transaction, instances are preset with OpenJPA StateManager while detached ones with DetachedStateManagers.
 * Both managers are responsible for managing instance fields i.e. the overall instance state.
 */
public interface CachedInstanceDetachmentStrategy {
	/**
	 * Detach instance.
	 *
	 * @param object The object to detach.
	 * @param <T> the type
	 * @return detached object.
	 */
	<T> T detach(T object);
}
