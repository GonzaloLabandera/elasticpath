/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.base.cache;

import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A container object for a cache result which can distinguish between a cache
 * value that is present or not present. A present value can contain null.
 * If a value is present, {@code isPresent()} will return {@code true} and
 * {@code get()} will return the value.
 *
 * @param <T> the class implemented by the cache values
 */
public final class CacheResult<T> {
	private T value;
	private final boolean present;

	private CacheResult(final T value) {
		this.present = true;
		this.value = value;
	}

	private CacheResult() {
		present = false;
	}

	/**
	 * Creates wrapper for given value.
	 *
	 * @param value given value to create wrapper
	 * @param <T>   the class implemented by the cache values
	 * @return created wrapper
	 */
	public static <T> CacheResult<T> create(final T value) {
		return new CacheResult<>(value);
	}

	/**
	 * Creates empty wrapper for absent result in the cache.
	 *
	 * @param <T> the class implemented by the cache values
	 * @return empty wrapper
	 */
	public static <T> CacheResult<T> notPresent() {
		return new CacheResult<>();
	}

	/**
	 * If a value is present in this {@code CacheResult}, returns the value,
	 * otherwise throws {@code NoSuchElementException}.
	 *
	 * @return the value held by this {@code CacheResult}
	 * @throws NoSuchElementException if there is no value present
	 */
	public T get() {
		if (!present) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * Return {@code true} if there is a value present, otherwise {@code false}.
	 *
	 * @return {@code true} if there is a value present, otherwise {@code false}
	 */
	public boolean isPresent() {
		return present;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		CacheResult<?> that = (CacheResult<?>) obj;

		return new EqualsBuilder()
				.append(present, that.present)
				.append(value, that.value)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(value)
				.append(present)
				.toHashCode();
	}
}