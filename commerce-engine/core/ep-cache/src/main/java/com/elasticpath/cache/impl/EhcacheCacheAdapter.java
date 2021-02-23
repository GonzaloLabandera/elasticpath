/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cache.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.ehcache.Element;

import com.elasticpath.cache.CacheResult;

/**
 * A wrapper which provides a {@link com.elasticpath.cache.Cache} interface for ehcache.
 *
 * @param <K> The class implemented by the cache keys
 * @param <V> The class implemented by the cache values
 */
public class EhcacheCacheAdapter<K, V> extends AbstractCacheAdapter<K, V> implements com.elasticpath.cache.Cache<K, V> {
	private final net.sf.ehcache.Cache cache;

	/**
	 * Constructs the adapter.
	 *
	 * @param cache the underlying EhCache
	 */
	public EhcacheCacheAdapter(final net.sf.ehcache.Cache cache) {
		this.cache = cache;
	}

	@Override
	@SuppressWarnings("unchecked")
	public CacheResult<V> get(final K key) {
		final Element element = cache.get(key);
		if (Objects.isNull(element)) {
			return CacheResult.notPresent();
		}
		return CacheResult.create((V) element.getObjectValue());
	}

	@Override
	public void put(final K key, final V value) {
		Element ele = new Element(key, value);
		cache.put(ele);
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
	@SuppressWarnings("unchecked")
	public <T> T unwrap(final Class<T> clazz) {
		return (T) cache;
	}

	@Override
	public boolean containsKey(final K key) {
		return cache.getKeys().contains(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public CacheResult<V> getByPartialKey(final K partialKey) {
		for (Object key : cache.getKeys()) {
			if (key.equals(partialKey)) {
				Element element = cache.get(key);
				if (Objects.nonNull(element)) {
					return CacheResult.create((V) element.getObjectValue());
				}
			}
		}

		return CacheResult.notPresent();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<V> getAllByPartialKey(final K partialKey) {
		List<V> result = new ArrayList<>();

		for (Object key : cache.getKeys()) {
			if (key.equals(partialKey)) {
				Element element = cache.get(key);
				if (element != null) {
					result.add((V) element.getObjectValue());
				}
			}
		}

		return result.isEmpty()
				? null
				: result;
	}
}
