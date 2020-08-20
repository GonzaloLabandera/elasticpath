/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl;

import com.elasticpath.cache.Cache;
import com.elasticpath.cache.factory.CacheFactory;

/**
 * Implementation of {@link CacheFactory} that wraps the underlying cache in a {@link CacheMissCachingAdapter} instance.
 */
public class CacheMissCachingFactoryImpl implements CacheFactory {

	private CacheFactory decorated;

	@Override
	public <K, V> Cache<K, V> createCache(final String cacheName) {
		final Cache<K, V> cache = decorated.createCache(cacheName);
		return new CacheMissCachingAdapter<>(cache);
	}

	public void setDecorated(final CacheFactory decorated) {
		this.decorated = decorated;
	}
}
