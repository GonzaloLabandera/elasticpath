/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.impl.CacheUtil;

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
	 * @return the element, or null, if it does not exist.
	 * @throws NullPointerException  if the key is null
	 * @throws ClassCastException    if the implementation is configured to perform
	 *                               runtime-type-checking, and the key or value
	 *                               types are incompatible with those that have been
	 *                               configured for the {@link Cache}
	 */
	V get(K key);

	/**
	 * Gets an entry from the cache using a partial key.
	 *
	 * The key is considered partial if one or more fields do not participate in the <strong>hash-code</strong> calculation but they do participate
	 * in <strong>equals</strong>.
	 * Partial keys <strong>MUST</strong> be composite objects and implement <strong>equals</strong> method using <strong>||</strong> (vs &&) operator
	 * (unlike typical implementation of the <strong>equals</strong> method) between all participating fields that uniquely identify the key.
	 * The <strong>hashCode</strong> MUST include only 1 field, the one that will be the most searched by.
	 *
	 * This breaks the usual contract when implementing <strong>equals</strong> and <strong>hashCode</strong> methods but it allows
	 * re-purposing of the same cache.
	 *
	 * The concept is very similar to RDBMS, with primary and foreign keys.
	 *
	 * Partial keys perform very well in Ehcache caches with less than <strong>one million</strong> entries and they are by no means slower
	 * than <strong>hashCode</strong> search (O(N) vs O(1); it also depends on the position of the target value in the list of keys).
	 *
	 * While other map-based structures require full-table scan (and thus O(N) performance), {@link java.util.TreeMap} offers O(log N) retrieval
	 * and excellent performance.
	 *
	 * When designing a cache with partial keys, the first consideration is to determine which field(s) will be used the most.
	 * Such fields should be used in <strong>hashCode</strong> while others excluded and used only in <string>equals</string>.
	 *
	 * All key fields <strong>MUST</strong> uniquely identify the target object and guarantee that a single match will be returned.
	 *
	 * In the following example, CompoundKey can be used for searching entities by <strong>uidPk</strong> field and <strong>hashCode</strong>
	 * or by either <strong>guid</strong> or <strong>code</strong> fields, using <strong>equals</strong>.
	 *
	 * <code>
	 *     public class CompoundKey {
	 *     		private Long uidPkIdx;
	 *     		private String guidIdx;
	 *     		private String codeIdx;
	 *
	 *     		public void setUidPkIdx(Long uidPkIdx) {
	 *     			this.uidPkIdx = uidPkIdx;
	 *     		}
	 *
	 *     		public void setGuidIdx(String guidIdx) {
	 *     			this.guidIdx = guidIdx;
	 *     		}
	 *
	 *     		public void setCodeIdx(String codeIdx) {
	 *     			this.codeIdx = codeIdx;
	 *     		}
	 *			@Override
	 *     		public boolean equals(Object object) {
	 *				if (this == o) {
					 	return true;
	 				}
	 				if (o == null || getClass() != o.getClass()) {
					 	return false;
	 				}

	 				CompoundKey that = (CompoundKey) o;

					return Objects.equals(uidPkIdx, that.uidPkIdx)
					 || Objects.equals(guidIdx, that.guidIdx)
					 || Objects.equals(codeIdx, that.codeIdx);
	 *     		}
	 *
	 *     		@Override
				public int hashCode() {
					return Objects.hash(uidPkIdx);
				}
	 *     }
	 * </code>
	 * @param partialKey The key with parital data to search for.
	 * @return null if no value is found.
	 */
	default V getByPartialKey(K partialKey) {
		return null;
	}

	/**
	 * Return all values from the cache that correspond to the partial key.
	 *
	 * @param partialKey the partial key
	 * @return null if no values are found, otherwise a list of found values.
	 */
	default List<V> getAllByPartialKey(K partialKey) {
		return null;
	}

	/**
	 * Retrieves an object from cache using the given key.
	 *
	 * @param key the name of the key to retrieve the object by
	 * @param fallbackLoader a cache loader to use to load the value if the value cannot be found in cache
	 * @return the cached object
	 *
	 * @throws NullPointerException if either argument is null
	 */
	default V get(K key, CacheLoader<K, V> fallbackLoader) {
		V found = get(key);
		if (found == null) {
			found = fallbackLoader.load(key);
			if (found == null) {
				return null;
			} else {
				put(key, found);
			}
		}

		return found;
	}

	/**
	 * Retrieves a map with cached values using given keys.
	 *
	 * @param keyValues the list of key values
	 * @return A map of entries that were found for the given keys. Keys not found
	 *         in the cache are not in the returned map.
	 *
	 * @throws NullPointerException if either argument is null
	 */
	default Map<K, V> getAll(Collection<? extends K> keyValues) {
		Map<K, V> result = new LinkedHashMap<>();
		for (K keyVal : keyValues) {
			V value = get(keyVal);
			if (value != null) {
				result.put(keyVal, value);
			}
		}

		return result;
	}

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
	default Map<K, V> getAll(Collection<? extends K> keyValues, CacheLoader<K, V> fallbackLoader) {
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
	 * @throws NullPointerException  if key is null or if value is null
	 * @throws ClassCastException    if the implementation is configured to perform
	 *                               runtime-type-checking, and the key or value
	 *                               types are incompatible with those that have been
	 *                               configured for the {@link Cache}
	 * @see java.util.Map#put(Object, Object)
	 */
	void put(K key, V value);

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
