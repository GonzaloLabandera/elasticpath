/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.permissions;

import java.util.Set;

/**
 * Service to retrieve the list of permissions associated with the provided role.
 */
public interface RoleToPermissionsMappingService {

	/**
	 * Retrieves the set of permissions associated with the provided role.
	 *
	 * @param roleCode The role code.
	 * @return The list of permissions.
	 */
	Set<String> getPermissionsForRole(String roleCode);

	/**
	 * Retrieves the set of all of the currently defined role keys.
	 *
	 * @return The set of role keys.
	 */
	Set<String> getDefinedRoleKeys();
}

