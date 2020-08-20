/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import java.util.List;
import java.util.Map;

import com.elasticpath.common.dto.Dto;
import com.elasticpath.importexport.common.caching.CachePopulator;

/**
 * Delegating cache populator.
 */
public class DelegatingCachePopulatorImpl implements CachePopulator<Dto> {

	private Map<Class<? extends Dto>, CachePopulator<? extends Dto>> cachePopulators;

	@Override
	public void populate(final List<Dto> dtos) {
		if (dtos.isEmpty()) {
			return;
		}
		final CachePopulator<Dto> populator = getCachePopulator(dtos.get(0).getClass());
		if (populator == null) {
			return;
		}
		populator.populate(dtos);
	}

	/**
	 * Get the cache populator for the given dto class.
	 *
	 * @param dtoClass the dto class
	 * @return the cache populator
	 */
	@SuppressWarnings("unchecked")
	protected CachePopulator<Dto> getCachePopulator(final Class<? extends Dto> dtoClass) {
		return (CachePopulator<Dto>) cachePopulators.get(dtoClass);
	}

	public void setCachePopulators(final Map<Class<? extends Dto>, CachePopulator<? extends Dto>> cachePopulators) {
		this.cachePopulators = cachePopulators;
	}
}
