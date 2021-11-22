/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.base.cache.impl;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.antkorwin.xsync.XSync;

import com.elasticpath.base.cache.CacheResult;
import com.elasticpath.base.cache.SynchronizedCacheSupport;

/**
 * A support class for managing interactions with a SimpleTimeoutCache backed by EhCache. This implementation provides the following guarantees:
 *
 * 1. Threads will only be blocked if there is a cache miss and there is already an evaluation running for an equivalent cache key. Lookups for
 * different cache keys will not be blocked.
 *
 * 2. Only one evaluation will be executed for a cache miss of a given cache key.
 *
 * 3. While a cache miss evaluation is in progress, requests from separate threads for the same cache key will block until evaluation is complete
 * and they will get the cached value after the evaluation completes.
 *
 * tldr: This class virtually eliminates unnecessary cache miss evaluations and blocks threads only when a cache miss occurs for an equivalent
 * cache key.
 *
 * @param <K> the cache key type
 * @param <V> the result type
 */
public class SynchronizedCacheSupportImpl<K, V> implements SynchronizedCacheSupport<K, V> {
	private final XSync<K> xsync = new XSync<>();

	@Override
	public V get(final K cacheKey,
				 final Function<K, V> fallbackLoaderFunction,
				 final Function<K, CacheResult<V>> checkCacheFunction,
				 final BiFunction<K, V, V> populateCacheFunction) {
		CacheResult<V> cachedResult = checkCacheFunction.apply(cacheKey);
		if (cachedResult.isPresent()) {
			return cachedResult.get();
		}

		// On cache miss, make sure that we don't have several threads starting evaluation of the exact same parameters
		// to prevent an unrecoverable cascade of threads running the same evaluation.
		return xsync.evaluate(cacheKey, () -> {
			CacheResult<V> result = checkCacheFunction.apply(cacheKey);
			if (result.isPresent()) {
				return result.get();
			}
			V fallbackResult = fallbackLoaderFunction.apply(cacheKey);
			return populateCacheFunction.apply(cacheKey, fallbackResult);
		});
	}
}
