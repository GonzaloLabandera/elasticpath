/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.util;

import java.util.HashMap;
import java.util.Objects;


/**
 * An extended HashMap that uses wild cards to match key.
 * For example all key that start with elasticpath-ipcollect will match elasticpath-ipcollect-* key.
 *
 * @param <V> the type of setting
 */
public class XPFHashMap<V> extends HashMap<String, V> {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	@Override
	public V get(final Object key) {
		V value = super.get(key);

		return Objects.nonNull(value)
				? value
				: findByWildCard(key);
	}

	private V findByWildCard(final Object key) {
		return keySet().stream()
				.filter(currentKey -> currentKey.endsWith("*"))
				.filter(str -> key.toString().startsWith(str.substring(0, str.length() - 1)))
				.findFirst()
				.map(this::get)
				.orElse(null);
	}
}
