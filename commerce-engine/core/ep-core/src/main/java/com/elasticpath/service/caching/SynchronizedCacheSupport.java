/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.caching;

import java.util.function.Function;

/**
 * Support for caching a function with synchronized cache miss support.
 *
 * @param <K> the cache key type
 * @param <T> the result type
 */
public interface SynchronizedCacheSupport<K, T> {
	/**
	 * Cache the result of the cache population function, ensuring that the cache population function is only invoked once per
	 * cache miss for a particular cache key.
	 *
	 * @param cachePopulationFunction the function for populating the cache
	 * @param cacheKey the cache key object
	 * @return the result
	 */
	T cacheResult(Function<Void, T> cachePopulationFunction, K cacheKey);
}
