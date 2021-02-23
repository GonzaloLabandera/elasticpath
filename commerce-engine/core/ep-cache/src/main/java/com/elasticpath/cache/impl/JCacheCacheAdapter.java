/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cache.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.cache.Cache;

import com.elasticpath.cache.CacheResult;

/**
 * A wrapper which provides a {@link com.elasticpath.cache.Cache} interface for a JCache (JSR-107) compliant cache.
 *
 * @param <K> The class implemented by the cache keys
 * @param <V> The class implemented by the cache values
 */
public class JCacheCacheAdapter<K, V> extends AbstractCacheAdapter<K, V> implements com.elasticpath.cache.Cache<K, V> {
	private final Cache<K, V> cache;

	/**
	 * Constructs the adapter.
	 *
	 * @param cache the underlying JCache
	 */
	public JCacheCacheAdapter(final Cache<K, V> cache) {
		this.cache = cache;
	}

	@Override
	public CacheResult<V> get(final K key) {
		return CacheResult.create(cache.get(key));
	}

	@Override
	public void put(final K key, final V value) {
		cache.put(key, value);
	}

	@Override
	public boolean remove(final K key) {
		return cache.remove(key);
	}

	@Override
	public void removeAll() {
		cache.removeAll();
	}

	@Override
	public String getName() {
		return cache.getName();
	}

	@Override
	public <T> T unwrap(final Class<T> clazz) {
		return cache.unwrap(clazz);
	}

	@Override
	public boolean containsKey(final K key) {
		return cache.containsKey(key);
	}

	@Override
	public CacheResult<V> getByPartialKey(final K partialKey) {
		for (Iterator<Cache.Entry<K, V>> iterator = cache.iterator(); iterator.hasNext();) {
			Cache.Entry<K, V> entry = iterator.next();
			if (entry.getKey().equals(partialKey)) {
				return CacheResult.create(entry.getValue());
			}
		}

		return CacheResult.notPresent();
	}

	@Override
	public List<V> getAllByPartialKey(final K partialKey) {
		List<V> result = new ArrayList<>();

		for (Iterator<Cache.Entry<K, V>> iterator = cache.iterator(); iterator.hasNext();) {
			Cache.Entry<K, V> entry = iterator.next();
			if (entry.getKey().equals(partialKey)) {
				result.add(entry.getValue());
			}
		}

		return result.isEmpty()
			? null
			: result;
	}
}
