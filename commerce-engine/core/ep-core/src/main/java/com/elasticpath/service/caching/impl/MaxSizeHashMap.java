/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.caching.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A hash map that automatically removes it's oldest entry if it exceeds a pre-defined size.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
	/** Serial version id. */
	private static final long serialVersionUID = 7000000001L;

	private final int maxSize;

	/**
	 * Constructor.
	 * @param maxSize maximum permitted number of entries in the map
	 */
	public MaxSizeHashMap(final int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		MaxSizeHashMap<?, ?> that = (MaxSizeHashMap<?, ?>) other;
		return maxSize == that.maxSize;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), maxSize);
	}
}