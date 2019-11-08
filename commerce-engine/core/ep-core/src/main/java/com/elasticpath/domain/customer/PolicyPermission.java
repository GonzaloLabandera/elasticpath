/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer;

import java.util.Collection;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * Policy permission extensible enum.
 */
public class PolicyPermission extends AbstractExtensibleEnum<PolicyPermission> {

	private static final long serialVersionUID = 3339631345791187622L;

	/**
	 * Emit policy permission ordinal.
	 */
	public static final int EMIT_ORDINAL = 0;

	/**
	 * Emit Policy permission.
	 */
	public static final PolicyPermission EMIT = new PolicyPermission(EMIT_ORDINAL, "EMIT");

	/**
	 * Edit policy permission ordinal.
	 */
	public static final int EDIT_ORDINAL = 1;

	/**
	 * Edit Policy permission.
	 */
	public static final PolicyPermission EDIT = new PolicyPermission(EDIT_ORDINAL, "EDIT");

	/**
	 * None policy permission ordinal.
	 */
	public static final int NONE_ORDINAL = 2;

	/**
	 * None policy permission.
	 */
	public static final PolicyPermission NONE = new PolicyPermission(NONE_ORDINAL, "NONE");

	/**
	 * Instantiate new policy permission.
	 *
	 * @param ordinal the ordinal
	 * @param name    the name
	 */
	protected PolicyPermission(final int ordinal, final String name) {
		super(ordinal, name, PolicyPermission.class);
	}

	/**
	 * Find the enum value with the specified ordinal value.
	 *
	 * @param ordinal the ordinal value
	 * @return the enum value
	 */
	public static PolicyPermission valueOf(final int ordinal) {
		return valueOf(ordinal, PolicyPermission.class);
	}

	/**
	 * Find the enum value with the specified name value.
	 *
	 * @param name the name
	 * @return the enum value
	 */
	public static PolicyPermission valueOf(final String name) {
		return valueOf(name, PolicyPermission.class);
	}

	/**
	 * Get all available values.
	 *
	 * @return all available values.
	 */
	public static Collection<PolicyPermission> values() {
		return values(PolicyPermission.class);
	}

	@Override
	protected Class<PolicyPermission> getEnumType() {
		return PolicyPermission.class;
	}
}