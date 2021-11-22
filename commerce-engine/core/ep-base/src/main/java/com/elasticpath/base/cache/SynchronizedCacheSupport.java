/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.base.cache;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Support for caching a function with synchronized cache miss support.
 *
 * @param <K> the cache key type
 * @param <V> the result type
 */
public interface SynchronizedCacheSupport<K, V> {
	/**
	 * Get the requested key from the cache, or use the fallback loader function if the key is not present. In the case of a miss,
	 * this method will cache the result of the fallback loader execution, ensuring that the cache population function is only invoked once per
	 * cache miss for a particular cache key.
	 *
	 * @param cacheKey the cache key object
	 * @param fallbackLoaderFunction the function for populating the cache
	 * @param checkCacheFunction the function for retrieving the passed key from the cache
	 * @param populateCacheFunction the function for populating the cache with the passed key and value
	 * @return the result
	 */
	V get(K cacheKey, Function<K, V> fallbackLoaderFunction, Function<K, CacheResult<V>> checkCacheFunction,
		  BiFunction<K, V, V> populateCacheFunction);
}
