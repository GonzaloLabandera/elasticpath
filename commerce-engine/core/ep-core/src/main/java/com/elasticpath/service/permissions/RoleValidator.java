/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.permissions;

import java.util.Set;

/**
 * Validator to validate the provided role.
 */
public interface RoleValidator {

	/**
	 * Validates the provided role.
	 *
	 * @param role he role to validate
	 * @return true if the role is valid, false otherwise
	 */
	boolean isValidRole(String role);

	/**
	 * Retrieves the set of all valid user roles.
	 *
	 * @return the set of user roles
	 */
	Set<String> getValidUserRoles();

}
