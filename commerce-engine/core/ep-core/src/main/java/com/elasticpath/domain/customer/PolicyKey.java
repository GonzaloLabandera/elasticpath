/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer;

import java.util.Collection;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * Policy key extensible enum.
 */
public class PolicyKey extends AbstractExtensibleEnum<PolicyKey> {

	private static final long serialVersionUID = 1L;

	/**
	 * Ordinal constant for DEFAULT.
	 */
	public static final int DEFAULT_ORDINAL = 0;

	/**
	 * Default policy key.
	 */
	public static final PolicyKey DEFAULT = new PolicyKey(DEFAULT_ORDINAL, "DEFAULT");

	/**
	 * Ordinal constant for HIDDEN.
	 */
	public static final int HIDDEN_ORDINAL = 1;

	/**
	 * Hidden policy key.
	 */
	public static final PolicyKey HIDDEN = new PolicyKey(HIDDEN_ORDINAL, "HIDDEN");

	/**
	 * Ordinal constant for READ_ONLY.
	 */
	public static final int READ_ONLY_ORDINAL = 2;

	/**
	 * Visible policy key.
	 */
	public static final PolicyKey READ_ONLY = new PolicyKey(READ_ONLY_ORDINAL, "READ_ONLY");

	/**
	 * Constructor.
	 *
	 * @param ordinal the ordinal
	 * @param name    the name
	 */
	protected PolicyKey(final int ordinal, final String name) {
		super(ordinal, name, PolicyKey.class);
	}

	/**
	 * Find the enum value with the specified ordinal value.
	 *
	 * @param ordinal the ordinal value
	 * @return the enum value
	 */
	public static PolicyKey valueOf(final int ordinal) {
		return valueOf(ordinal, PolicyKey.class);
	}

	/**
	 * Find the enum value with the specified name value.
	 *
	 * @param name the name
	 * @return the enum value
	 */
	public static PolicyKey valueOf(final String name) {
		return valueOf(name, PolicyKey.class);
	}

	/**
	 * Get all available values.
	 *
	 * @return all available values.
	 */
	public static Collection<PolicyKey> values() {
		return values(PolicyKey.class);
	}

	@Override
	protected Class<PolicyKey> getEnumType() {
		return PolicyKey.class;
	}
}