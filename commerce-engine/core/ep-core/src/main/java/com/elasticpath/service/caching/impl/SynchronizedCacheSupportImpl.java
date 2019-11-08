/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.caching.impl;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.elasticpath.cache.SimpleTimeoutCache;
import com.elasticpath.service.caching.SynchronizedCacheSupport;

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
 * @param <T> the result type
 */
public class SynchronizedCacheSupportImpl<K, T> implements SynchronizedCacheSupport<K, T> {
	private static final Logger LOG = Logger.getLogger(SynchronizedCacheSupportImpl.class);
	private static final int EVALUATION_LOCKS_MAX_SIZE = 10000;

	private SimpleTimeoutCache<K, T> evaluationCache;

	private final Map<K, ReentrantLock> evaluationLocks = new MaxSizeHashMap<>(EVALUATION_LOCKS_MAX_SIZE);

	@Override
	public T cacheResult(final Function<Void, T> cachePopulationFunction, final K cacheKey) {
		T result = evaluationCache.get(cacheKey);
		if (result != null) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Cache hit on " + cacheKey);
			}
			return result;
		}

		// On cache miss, make sure that we don't have several threads starting evaluation of the exact same parameters
		// to prevent an unrecoverable cascade of threads running the same evaluation.
		if (LOG.isTraceEnabled()) {
			LOG.trace("Cache miss on " + cacheKey);
		}
		ReentrantLock reentrantLock = getReentrantLock(cacheKey);
		reentrantLock.lock();
		try {
			result = evaluationCache.get(cacheKey);
			if (result != null) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Avoided duplicate execution on " + cacheKey);
				}
				return result;
			}
			if (LOG.isTraceEnabled()) {
				LOG.trace("Started fallthrough execution for " + cacheKey);
			}
			result = cachePopulationFunction.apply(null);
			if (LOG.isTraceEnabled()) {
				LOG.trace("Completed fallthrough execution for " + cacheKey);
			}
			evaluationCache.put(cacheKey, result);
			return result;
		} finally {
			reentrantLock.unlock();
		}
	}

	private ReentrantLock getReentrantLock(final K cacheKey) {
		ReentrantLock reentrantLock;
		synchronized (this) {
			reentrantLock = evaluationLocks.computeIfAbsent(cacheKey, key -> new ReentrantLock());
		}
		return reentrantLock;
	}

	protected SimpleTimeoutCache<K, T> getEvaluationCache() {
		return evaluationCache;
	}

	public void setEvaluationCache(final SimpleTimeoutCache<K, T> evaluationCache) {
		this.evaluationCache = evaluationCache;
	}
}
