/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.caching.core.faceting;

import com.elasticpath.cache.Cache;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfigurationLoader;

/**
 * Caching version of the Facet configuration loader.
 */
public class CachingFacetConfigurationLoaderImpl  implements FilteredNavigationConfigurationLoader {

	private Cache<String, FilteredNavigationConfiguration> loadFilteredNavigationConfigurationCache;
	private FilteredNavigationConfigurationLoader fallback;

	@Override
	public FilteredNavigationConfiguration loadFilteredNavigationConfiguration(final String storeCode) {
		return loadFilteredNavigationConfigurationCache.get(storeCode, cacheKey -> fallback.loadFilteredNavigationConfiguration(storeCode));
	}

	@Override
	public String getSeparatorInToken() {
		return fallback.getSeparatorInToken();
	}

	/**
	 * Set the cache.
	 * @param loadFilteredNavigationConfigurationCache the cache.
	 */
	public void setloadFilteredNavigationConfigurationCache(
			final Cache<String, FilteredNavigationConfiguration> loadFilteredNavigationConfigurationCache) {
		this.loadFilteredNavigationConfigurationCache = loadFilteredNavigationConfigurationCache;
	}

	/**
	 * Set the fallback service.
	 * @param fallback the fallback service.
	 */
	public void setFallback(final FilteredNavigationConfigurationLoader fallback) {
		this.fallback = fallback;
	}
}
