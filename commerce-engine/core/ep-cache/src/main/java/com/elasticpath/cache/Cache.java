/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cache;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.elasticpath.base.cache.CacheResult;

/**
 * EP Specific Cache interface.  Note that this interface is a strict subset of {@link javax.cache.Cache}.
 * This vendor specific sub-interface exists only because not all of the Cache Providers that EP interfaces
 * with implement the JSR 107 interface.
 *
 * @param <K> The class implemented by the cache keys
 * @param <V> The class implemented by the cache values
 */
public interface Cache<K, V> {
	/**
	 * Gets an entry from the cache.
	 * <p>
	 * If the cache is configured to use read-through, and get would return null
	 * because the entry is missing from the cache, the Cache's CacheLoader
	 * is called in an attempt to load the entry.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the wrapped result.
	 * @throws NullPointerException  if the key is null
	 * @throws ClassCastException    if the implementation is configured to perform
	 *                               runtime-type-checking, and the key or value
	 *                               types are incompatible with those that have been
	 *                               configured for the {@link Cache}
	 */
	CacheResult<V> get(K key);

	/**
	 * Retrieves an object from cache using the given key, falling back on the fallbackLoader if it's not present.
	 *
	 * @param key            the name of the key to retrieve the object by
	 * @param fallbackLoader a cache loader to use to load the value if the value cannot be found in cache
	 * @return the value
	 * @throws NullPointerException if either argument is null
	 */
	V get(K key, Function<K, V> fallbackLoader);

	/**
	 * Retrieves an object from cache using the given key, falling back on the fallbackLoader if it's not present.
	 * The populateCacheFunction provides custom cache population when simple put is not enough.
	 *
	 * @param key            the name of the key to retrieve the object by
	 * @param fallbackLoader a cache loader to use to load the value if the value cannot be found in cache
	 * @param populateCacheFunction custom cache population function
	 * @return the value
	 * @throws NullPointerException if either argument is null
	 */
	V get(K key, Function<K, V> fallbackLoader, BiFunction<K, V, V> populateCacheFunction);

	/**
	 * Retrieves a map with cached values using given keys.
	 *
	 * @param keyValues the list of key values
	 * @return A map of entries that were found for the given keys. Keys not found
	 *         in the cache are not in the returned map.
	 *
	 * @throws NullPointerException if either argument is null
	 */
	Map<K, V> getAll(Collection<? extends K> keyValues);

	/**
	 * Retrieves an object from cache using the given keys.
	 *
	 * @param keyValues the list of key values
	 * @param fallbackLoader a cache loader to use to load the value if values cannot be found in cache
	 * @return A map of entries that were found for the given keys. Keys not found
	 *         in the cache are not in the returned map.
	 *
	 * @throws NullPointerException if either argument is null
	 */
	Map<K, V> getAll(Collection<K> keyValues, Function<Collection<K>, Map<K, V>> fallbackLoader);

	/**
	 * Associates the specified value with the specified key in the cache.
	 * <p>
	 * If the {@link Cache} previously contained a mapping for the key, the old
	 * value is replaced by the specified value.  (A cache <tt>c</tt> is said to
	 * contain a mapping for a key <tt>k</tt> if and only if containsKey(Object)
	 * c.containsKey(k)} would return <tt>true</tt>.)
	 *
	 * @param key   key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the value
	 * @throws NullPointerException  if key is null or if value is null
	 * @throws ClassCastException    if the implementation is configured to perform
	 *                               runtime-type-checking, and the key or value
	 *                               types are incompatible with those that have been
	 *                               configured for the {@link Cache}
	 * @see java.util.Map#put(Object, Object)
	 */
	V put(K key, V value);

	/**
	 * Removes the mapping for a key from this cache if it is present.
	 * <p>
	 * More formally, if this cache contains a mapping from key <tt>k</tt> to
	 * value <tt>v</tt> such that
	 * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is removed.
	 * (The cache can contain at most one such mapping.)
	 *
	 * <p>Returns <tt>true</tt> if this cache previously associated the key,
	 * or <tt>false</tt> if the cache contained no mapping for the key.
	 * <p>
	 * The cache will not contain a mapping for the specified key once the
	 * call returns.
	 *
	 * @param key key whose mapping is to be removed from the cache
	 * @return returns false if there was no matching key
	 * @throws NullPointerException  if key is null
	 * @throws ClassCastException    if the implementation is configured to perform
	 *                               runtime-type-checking, and the key or value
	 *                               types are incompatible with those that have been
	 *                               configured for the {@link Cache}
	 */
	boolean remove(K key);

	/**
	 * Removes all of the mappings from this cache.
	 * <p>
	 * The order that the individual entries are removed is undefined.
	 * <p>
	 * For every mapping that exists the following are called:
	 * <ul>
	 *   <li>any registered CacheEntryRemovedListeners</li>
	 *   <li>if the cache is a write-through cache, the CacheWriter</li>
	 * </ul>
	 * If the cache is empty, the CacheWriter is not called.
	 * <p>
	 * This is potentially an expensive operation as listeners are invoked.
	 * Use clear() to avoid this.
	 */
	void removeAll();

	/**
	 * Return the name of the cache.
	 *
	 * @return the name of the cache.
	 */
	String getName();

	/**
	 * Provides a standard way to access the underlying concrete caching
	 * implementation to provide access to further, proprietary features.
	 * <p>
	 * If the provider's implementation does not support the specified class,
	 * the {@link IllegalArgumentException} is thrown.
	 *
	 * @param clazz the proprietary class or interface of the underlying concrete
	 *              cache. It is this type that is returned.
	 * @param <T> The class implemented by the underlying concrete caching implementation
	 * @return an instance of the underlying concrete cache
	 * @throws IllegalArgumentException if the caching provider doesn't support
	 *                                  the specified class.
	 * @throws SecurityException        when the operation could not be performed
	 *                                  due to the current security settings
	 */
	<T> T unwrap(java.lang.Class<T> clazz);

	/**
	 * Checks if the cache contains given key.
	 *
	 * @param key the key
	 * @return true if cache contains provided key
	 */
	boolean containsKey(K key);
}
