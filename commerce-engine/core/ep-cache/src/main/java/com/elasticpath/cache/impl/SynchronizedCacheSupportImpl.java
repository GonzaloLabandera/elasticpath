/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.elasticpath.cache.CacheResult;
import com.elasticpath.cache.SynchronizedCacheSupport;

/**
 * A support class for managing interactions with a SimpleTimeoutCache backed by EhCache. This implementation provides the following guarantees:
 *
 * 1. Threads will only be blocked if there is a cache miss and there is already an evaluation running for an equivalent cache key. Lookups for
 * different cache keys will not be blocked.
 *
 * 2. Only one evaluation will be executed for a cache miss of a given cache key. (There is actually a small chance that this could occur if the
 * evaluation locks map rolls over, but it is highly unlikely.)
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
	private final Map<K, ReentrantLock> evaluationLocks = new HashMap<>();

	@Override
	public V get(final K cacheKey,
				 final Function<K, V> fallbackLoaderFunction,
				 final Function<K, CacheResult<V>> checkCacheFunction,
				 final Consumer<Pair<K, V>> populateCacheFunction) {
		CacheResult<V> result = checkCacheFunction.apply(cacheKey);
		if (result.isPresent()) {
			return result.get();
		}

		// On cache miss, make sure that we don't have several threads starting evaluation of the exact same parameters
		// to prevent an unrecoverable cascade of threads running the same evaluation.
		ReentrantLock reentrantLock = acquireReentrantLock(cacheKey);
		reentrantLock.lock();
		try {
			result = checkCacheFunction.apply(cacheKey);
			if (result.isPresent()) {
				return result.get();
			}
			V fallbackResult = fallbackLoaderFunction.apply(cacheKey);
			populateCacheFunction.accept(new ImmutablePair<>(cacheKey, fallbackResult));
			return fallbackResult;
		} finally {
			releaseReentrantLock(reentrantLock);
		}
	}

	private ReentrantLock acquireReentrantLock(final K cacheKey) {
		ReentrantLock reentrantLock;
		synchronized (this) {
			reentrantLock = evaluationLocks.computeIfAbsent(cacheKey, key -> new ReentrantLock());
		}
		return reentrantLock;
	}

	private void releaseReentrantLock(final ReentrantLock reentrantLock) {
		synchronized (this) {
			reentrantLock.unlock();
			evaluationLocks.remove(reentrantLock);
		}
	}
}
