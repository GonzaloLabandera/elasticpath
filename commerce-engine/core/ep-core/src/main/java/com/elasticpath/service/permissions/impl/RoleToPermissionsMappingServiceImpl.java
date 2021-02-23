/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.permissions.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * Implementation of RoleToPermissionsMappingService.
 */
public class RoleToPermissionsMappingServiceImpl implements RoleToPermissionsMappingService {

	private Map<String, List<String>> roleToPermissionsMap;

	@Override
	public Set<String> getPermissionsForRole(final String roleCode) {
		if (roleToPermissionsMap.containsKey(roleCode)) {
			return new HashSet<>(roleToPermissionsMap.get(roleCode));
		}
		throw new EpServiceException(roleCode + " is not valid role.");
	}

	@Override
	public Set<String> getDefinedRoleKeys() {
		return getRoleToPermissionsMap().keySet();
	}

	public void setRoleToPermissionsMap(final Map<String, List<String>> roleToPermissionsMap) {
		this.roleToPermissionsMap = roleToPermissionsMap;
	}

	protected Map<String, List<String>> getRoleToPermissionsMap() {
		return roleToPermissionsMap;
	}
}
