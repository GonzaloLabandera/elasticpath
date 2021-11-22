/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.elasticpath.domain.store.Store;

/**
 * Store Domain Context.
 *
 * @param <T> the domain
 */
public class StoreDomainContext<T> {
	private final T domain;
	private final Optional<Store> store;

	/**
	 * Constructor.
	 *
	 * @param domain the domain
	 * @param store  the store
	 */
	public StoreDomainContext(final T domain, final Store store) {
		this.domain = domain;
		this.store = Optional.ofNullable(store);
	}

	/**
	 * Constructor.
	 *
	 * @param domain the domain
	 * @param store  the store optional
	 */
	public StoreDomainContext(final T domain, final Optional<Store> store) {
		this.domain = domain;
		this.store = store;
	}

	/**
	 * Get the store.
	 *
	 * @return the store
	 */
	public Optional<Store> getStore() {
		return store;
	}

	/**
	 * Get the domain.
	 *
	 * @return the domain
	 */
	public T getDomain() {
		return domain;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		StoreDomainContext<?> that = (StoreDomainContext<?>) obj;

		return new EqualsBuilder()
				.append(getDomain(), that.getDomain())
				.append(getStore(), that.getStore())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getDomain())
				.append(getStore())
				.toHashCode();
	}
}
