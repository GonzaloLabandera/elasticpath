/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.cache.impl.CacheUtil;

/**
 * A decorator that can wrap another cache adapter (such as {@link com.elasticpath.cache.impl.EhcacheCacheAdapter}) so that
 * cache misses are cached. Normally cache misses always result in a call to the fallback loader.
 *
 * @param <K> The class implemented by the cache keys
 * @param <V> The class implemented by the cache values
 */
public class CacheMissCachingAdapter<K, V> implements Cache<K, V> {

	private final Cache<K, V> decorated;

	/**
	 * Constructor.
	 *
	 * @param decorated the decorated cache
	 */
	public CacheMissCachingAdapter(final Cache<K, V> decorated) {
		this.decorated = decorated;
	}

	@Override
	public V get(final K key) {
		return decorated.get(key);
	}

	@Override
	public V get(final K key, final CacheLoader<K, V> fallbackLoader) {
		final V cached = get(key);
		if (cached != null) {
			return cached;
		}
		if (containsKey(key)) {
			return null;
		}
		final V found = fallbackLoader.load(key);
		put(key, found);
		return found;
	}

	@Override
	public Map<K, V> getAll(final Collection<? extends K> keyValues) {
		final Map<K, V> result = new LinkedHashMap<>(keyValues.size() * 2);
		for (K keyVal : keyValues) {
			final V value = get(keyVal);
			if (value != null || containsKey(keyVal)) {
				result.put(keyVal, value);
			}
		}
		return result;
	}

	@Override
	public Map<K, V> getAll(final Collection<? extends K> keyValues, final CacheLoader<K, V> fallbackLoader) {
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
				// caches null results in case the fallback loader won't return the related records either
				for (K unCachedKey : uncachedKeys) {
					put(unCachedKey, null);
				}
				Map<K, V> unCachedValueMap = fallbackLoader.loadAll(uncachedKeys);
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

	@Override
	public void put(final K key, final V value) {
		decorated.put(key, value);
	}

	@Override
	public boolean remove(final K key) {
		return decorated.remove(key);
	}

	@Override
	public void removeAll() {
		decorated.removeAll();
	}

	@Override
	public String getName() {
		return decorated.getName();
	}

	@Override
	public <T> T unwrap(final Class<T> clazz) {
		return decorated.unwrap(clazz);
	}

	@Override
	public boolean containsKey(final K key) {
		return decorated.containsKey(key);
	}
}
