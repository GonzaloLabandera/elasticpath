/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.impl;

import java.util.Map;
import java.util.Optional;

import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.persistence.api.Persistable;

/**
 * Implementation of {@link MutableCachingService} that delegates to entity specific caching services.
 */
public class DelegatingMutableCachingServiceImpl implements MutableCachingService<Persistable> {
	private Map<Class<? extends Persistable>, MutableCachingService<? extends Persistable>> cachingServices;

	@Override
	public void cache(final Persistable entity) {
		getCachingService(entity.getClass())
				.ifPresent(cachingService -> cachingService.cache(entity));
	}

	@Override
	public void invalidate(final Persistable entity) {
		getCachingService(entity.getClass())
				.ifPresent(cachingService -> cachingService.invalidate(entity));
	}

	@Override
	public void invalidateAll() {
		cachingServices.values().forEach(MutableCachingService::invalidateAll);
	}

	/**
	 * Get the optional caching service for the entity class.
	 *
	 * @param entityClass the entity class
	 * @param <E>         the entity type
	 * @return the optional entity caching service
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Persistable> Optional<MutableCachingService<E>> getCachingService(final Class<? extends Persistable> entityClass) {
		return Optional.ofNullable((MutableCachingService<E>) cachingServices.get(entityClass));
	}

	public void setCachingServices(final Map<Class<? extends Persistable>, MutableCachingService<? extends Persistable>> cachingServices) {
		this.cachingServices = cachingServices;
	}
}
