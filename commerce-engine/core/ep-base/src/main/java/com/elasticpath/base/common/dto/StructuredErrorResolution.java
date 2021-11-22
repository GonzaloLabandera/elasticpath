/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.base.common.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Holds information on how to resolve an error.
 */
public class StructuredErrorResolution {

	private final Class<?> domain;
	private final String guid;

	/**
	 * Creates a new Structured Error resolution.
	 * @param domain the domain class.
	 * @param guid the guid of the domain object.
	 */
	public StructuredErrorResolution(final Class<?> domain, final String guid) {
		this.domain = domain;
		this.guid = guid;
	}

	public Class<?> getDomain() {
		return domain;
	}

	public String getGuid() {
		return guid;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		StructuredErrorResolution that = (StructuredErrorResolution) other;

		return new EqualsBuilder()
				.append(domain, that.domain)
				.append(guid, that.guid)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(domain)
				.append(guid)
				.toHashCode();
	}
}
