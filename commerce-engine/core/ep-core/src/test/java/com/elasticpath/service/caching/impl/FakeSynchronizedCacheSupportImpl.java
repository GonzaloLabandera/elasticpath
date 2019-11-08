package com.elasticpath.service.caching.impl;

import java.util.function.Function;

import com.elasticpath.service.caching.SynchronizedCacheSupport;


/**
 * A fake implementation of SynchronizedCacheSupport that simply executes the cache population function every time.
 *
 * @param <K> the cache key type
 * @param <T> the result type
 **/
public class FakeSynchronizedCacheSupportImpl<K, T> implements SynchronizedCacheSupport<K, T> {
	@Override
	public T cacheResult(final Function<Void, T> cachePopulationFunction, final K cacheKey) {
		return cachePopulationFunction.apply(null);
	}
}
