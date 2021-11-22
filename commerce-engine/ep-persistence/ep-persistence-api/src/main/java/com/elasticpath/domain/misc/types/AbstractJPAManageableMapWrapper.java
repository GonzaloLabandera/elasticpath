/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *
 */

package com.elasticpath.domain.misc.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.util.ChangeTracker;
import org.apache.openjpa.util.Proxies;
import org.apache.openjpa.util.Proxy;

/**
 * This class is a general-purpose map wrapper, managed by OpenJPA state managers.
 * Extensions can be used as return types in combination with {@link org.apache.openjpa.persistence.Externalizer} and
 * {@link org.apache.openjpa.persistence.Factory} annotations.
 *
 * The fact that this class is managed by OpenJPA state managers simplifies the implementation of notifying OpenJPA structures
 * when map is modified.
 *
 * This class does not provide all {@link Map} methods, only a subset.
 * Due to OpenJPA limitations, it wasn't possible to extend {@link HashMap} or a similar map structure, because querying
 * works only for primitive, primitive wrapper, string, or date types, due to constraints on query syntax.
 *
 * See http://openjpa.apache.org/builds/2.4.1/apache-openjpa/docs/manual.html#ref_guide_pc_extern
 * <code>
 *     Note
 * Currently, queries are limited to fields that externalize to a primitive, primitive wrapper, string, or date types, due to constraints on query
 * syntax.
 * </code>
 *
 * With regards to thread-safety, this class/extensions do not differ from enhanced entities, thus usual precautions should be taken when
 * dealing with multiple threads.
 *
 * @param <KEY> Map key type
 * @param <VALUE> Map value type
 */
public abstract class AbstractJPAManageableMapWrapper<KEY, VALUE> implements Proxy {
	private OpenJPAStateManager stateManager;
	private int fieldIndex;

	private Map<KEY, VALUE> map = new HashMap<>();

	/**
	 * Default constructor.
	 */
	protected AbstractJPAManageableMapWrapper() {
		//empty
	}

	/**
	 * Custom constructor.
	 *
	 * @param newMap the map to construct a new instance with.
	 */
	protected AbstractJPAManageableMapWrapper(final Map<KEY, VALUE> newMap) {
		this.map = newMap;
	}

	/**
	 * Removes a given list of keys.
	 *
	 * @param keysToRemove the list of keys to remove
	 */
	public void removeAll(final List<KEY> keysToRemove) {
		if (CollectionUtils.isNotEmpty(keysToRemove)) {
			map.keySet().removeAll(keysToRemove);
			dirty();
		}
	}

	/**
	 * Clears the map.
	 * @see Map#clear().
	 */
	public void clear() {
		map.clear();
		dirty();
	}

	/**
	 * Create a new entry.
	 * @see Map#put(Object, Object).
	 * 
	 * @param key the key
	 * @param value the value
	 * @return the value
	 */
	public VALUE put(final KEY key, final VALUE value) {
		VALUE val =  map.put(key, value);
		dirty();
		return val;
	}

	/**
	 * Merge provided map with the current one.
	 * @see Map#putAll(Map).
	 * @param mapToMerge the map to merge
	 */
	public void putAll(final Map<? extends KEY, ? extends VALUE> mapToMerge) {
		if (MapUtils.isNotEmpty(mapToMerge)) {
			map.putAll(mapToMerge);
			dirty();
		}
	}

	/**
	 * Remove an entry, identified with its key.
	 *
	 * @see Map#remove(Object).
	 * 
	 * @param key the key
	 * @return the value
	 */
	public VALUE remove(final Object key) {
		VALUE val =  map.remove(key);
		dirty();
		return val;
	}

	/**
	 * Create a new entry, if absent.
	 * @see Map#putIfAbsent(Object, Object).
	 *
	 * @param key the key
	 * @param value the value
	 * @return the value
	 */
	public VALUE putIfAbsent(final KEY key, final VALUE value) {
		VALUE val =  map.putIfAbsent(key, value);
		dirty();
		return val;
	}

	/**
	 * Remove an entry identified with key and valye.
	 *
	 * @see Map#remove(Object, Object).
	 * @param key the key
	 * @param value the value
	 * @return true if key and value are removed
	 */
	public boolean remove(final Object key, final Object value) {
		boolean isRemoved =  map.remove(key, value);
		dirty();
		return isRemoved;
	}

	/**
	 * Replace old value with the new one.
	 *
	 * @see Map#replace(Object, Object).
	 *
	 * @param key the key
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @return true if old value is replaced
	 */
	public boolean replace(final KEY key, final VALUE oldValue, final VALUE newValue) {
		boolean isReplaced =  map.replace(key, oldValue, newValue);
		dirty();
		return isReplaced;
	}

	/**
	 * Replace value.
	 *
	 * @see Map#replace(Object, Object).
	 *
	 * @param key the key
	 * @param value the value
	 * @return the old value
	 */
	public VALUE replace(final KEY key, final VALUE value) {
		VALUE val =  map.replace(key, value);
		dirty();
		return val;
	}

	/**
	 * Merge value using re-mapping function.
	 *
	 * @see Map#merge(Object, Object, BiFunction).
	 *
	 * @param key the key
	 * @param value the value
	 * @param remappingFunction the remapping function
	 * @return the value
	 */
	public VALUE merge(final KEY key, final VALUE value, final BiFunction<? super VALUE, ? super VALUE, ? extends VALUE> remappingFunction) {
		VALUE val =  map.merge(key, value, remappingFunction);
		dirty();
		return val;
	}

	/**
	 * Replace all entries using provided function.
	 *
	 * @see Map#replaceAll(BiFunction).
	 *
	 * @param function the function
	 */
	public void replaceAll(final BiFunction<? super KEY, ? super VALUE, ? extends VALUE> function) {
		map.replaceAll(function);
		dirty();
	}

	/**
	 * Get the value.
	 *
	 * @see Map#get(Object).
	 *
	 * @param key the key
	 * @return the value
	 */
	public VALUE get(final KEY key) {
		return map.get(key);
	}

	/**
	 * Returns unmodifiable map to avoid accidental, non-OpenJPA-aware, modifications.
	 *
	 * @return unmodifiable map
	 */
	public Map<KEY, VALUE> getMap() {
		return Collections.unmodifiableMap(this.map);
	}

	/**
	 * Return map's entry set.
	 *
	 * @see Map#entrySet().
	 *
	 * @return the entry set
	 */
	public Set<Map.Entry<KEY, VALUE>> entrySet() {
		return getMap().entrySet();
	}

	@Override
	public void setOwner(final OpenJPAStateManager openJPAStateManager, final int fieldIndex) {
		this.stateManager = openJPAStateManager;
		this.fieldIndex = fieldIndex;
	}

	@Override
	public OpenJPAStateManager getOwner() {
		return stateManager;
	}

	@Override
	public int getOwnerField() {
		return fieldIndex;
	}

	@Override
	public ChangeTracker getChangeTracker() {
		return null;
	}

	private void dirty() {
		Proxies.dirty(this, false);
		if (stateManager != null && stateManager.getClass().isAssignableFrom(DetachedStateManager.class)) {
			stateManager = null;
		}
	}
}
