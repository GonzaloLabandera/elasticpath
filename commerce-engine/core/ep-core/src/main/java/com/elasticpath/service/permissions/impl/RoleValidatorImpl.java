/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.permissions.impl;

import java.util.Set;

import com.elasticpath.service.permissions.RoleValidator;

/**
 * Implementation of RoleValidator.
 */
public class RoleValidatorImpl implements RoleValidator {

	private final Set<String> validRoles;

	/**
	 * Constructor.
	 *
	 * @param validRoles set of valid roles
	 */
	public RoleValidatorImpl(final Set<String> validRoles) {
		this.validRoles = validRoles;
	}

	@Override
	public boolean isValidRole(final String role) {
		return validRoles.contains(role);
	}

	@Override
	public Set<String> getValidUserRoles() {
		return validRoles;
	}
}
