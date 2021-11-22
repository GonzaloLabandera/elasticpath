/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.cache.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.elasticpath.base.cache.CacheResult;

/**
 * An implementation of {@link com.elasticpath.cache.Cache} that always misses.
 * @param <K> The class implemented by the cache keys
 * @param <V> The class implemented by the cache values
 */
public class AlwaysMissCache<K, V> implements com.elasticpath.cache.Cache<K, V> {
	@Override
	public CacheResult<V> get(final K key) {
		return CacheResult.notPresent();
	}

	@Override
	public V get(final K key, final Function<K, V> fallbackLoader) {
		return fallbackLoader.apply(key);
	}

	@Override
	public V get(final K key, final Function<K, V> fallbackLoader, final BiFunction<K, V, V> populateCacheFunction) {
		return fallbackLoader.apply(key);
	}

	@Override
	public Map<K, V> getAll(final Collection<? extends K> keyValues) {
		return Collections.emptyMap();
	}

	@Override
	public Map<K, V> getAll(final Collection<K> keyValues, final Function<Collection<K>, Map<K, V>> fallbackLoader) {
		return Collections.emptyMap();
	}

	@Override
	public V put(final K key, final V value) {
		return value;
	}

	@Override
	public boolean remove(final K key) {
		return false;
	}

	@Override
	public void removeAll() {
		// Do nothing
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public <T> T unwrap(final Class<T> clazz) {
		return null;
	}

	@Override
	public boolean containsKey(final K key) {
		return false;
	}
}
