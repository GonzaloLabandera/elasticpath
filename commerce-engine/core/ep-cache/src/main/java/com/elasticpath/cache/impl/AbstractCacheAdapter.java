/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.elasticpath.base.cache.CacheResult;
import com.elasticpath.base.cache.SynchronizedCacheSupport;
import com.elasticpath.base.cache.impl.SynchronizedCacheSupportImpl;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;

/**
 * Abstract class implementing generic methods of the Cache interface.
 *
 * @param <K> the cache key type
 * @param <V> the result type
 */
public abstract class AbstractCacheAdapter<K, V> implements Cache<K, V> {
	private final SynchronizedCacheSupport<K, V> synchronizedCacheSupport = new SynchronizedCacheSupportImpl<>();

	@Override
	public V get(final K key, final Function<K, V> fallbackLoader) {
		return synchronizedCacheSupport.get(key, fallbackLoader, this::get, this::put);
	}

	@Override
	public V get(final K key, final Function<K, V> fallbackLoader, final BiFunction<K, V, V> populateCacheFunction) {
		return synchronizedCacheSupport.get(key, fallbackLoader, this::get, populateCacheFunction);
	}

	@Override
	public Map<K, V> getAll(final Collection<? extends K> keyValues) {
		Map<K, V> result = new LinkedHashMap<>();
		for (K keyVal : keyValues) {
			final CacheResult<V> value = get(keyVal);
			if (value.isPresent()) {
				result.put(keyVal, value.get());
			}
		}

		return result;
	}

	@Override
	public Map<K, V> getAll(final Collection<K> keyValues, final Function<Collection<K>, Map<K, V>> fallbackLoader) {
		Map<K, V> cachedValuesMap = getAll(keyValues);

		int cachedValuesSize = cachedValuesMap.size();
		int keyValuesSize = keyValues.size();

		if (cachedValuesSize == keyValuesSize) {
			return cachedValuesMap;
		}

		if (keyValuesSize > cachedValuesSize) {
			//more keys than cached values are provided.. need to update the cache
			final List<K> uncachedKeys = new ArrayList<>(keyValues);

			boolean isCollectionModified = uncachedKeys.removeAll(cachedValuesMap.keySet());

			if (isCollectionModified || cachedValuesMap.isEmpty()) {
				Map<K, V> unCachedValueMap = fallbackLoader.apply(uncachedKeys);
				for (Map.Entry<K, V> unCachedEntry : unCachedValueMap.entrySet()) {
					put(unCachedEntry.getKey(), unCachedEntry.getValue());
				}

				if (cachedValuesMap.isEmpty()) {
					return unCachedValueMap;
				}

				return CacheUtil.mergeResults(keyValues, cachedValuesMap, unCachedValueMap);

			} else {
				throw new EpServiceException("Couldn't remove elements from keyValues collection");
			}
		}

		return cachedValuesMap;
	}

}
