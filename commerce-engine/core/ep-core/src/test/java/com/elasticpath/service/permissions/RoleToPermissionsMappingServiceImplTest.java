/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.permissions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.service.permissions.impl.RoleToPermissionsMappingServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class RoleToPermissionsMappingServiceImplTest {

	private final List<String> rolePermissions = new ArrayList<>();

	private final Map<String, List<String>> roleToPermissionsMap = new HashMap<>();

	private final RoleToPermissionsMappingServiceImpl roleToPermissionsMappingService = new RoleToPermissionsMappingServiceImpl();

	@Before
	public void setUp() throws Exception {
		rolePermissions.add("SHIRO_ROLE");
		roleToPermissionsMap.put("BUYER", rolePermissions);
		roleToPermissionsMappingService.setRoleToPermissionsMap(roleToPermissionsMap);
	}

	@Test
	public void verifyPermissionsReturnedForValidRoleAsString() {
		assertThat(roleToPermissionsMappingService.getPermissionsForRole("BUYER"))
				.containsExactlyElementsOf(rolePermissions);
	}

	@Test
	public void verifyServiceReturnsSetOfAllRoles() {
		assertThat(roleToPermissionsMappingService.getDefinedRoleKeys())
				.containsOnly("BUYER");
	}

	@Test(expected = EpServiceException.class)
	public void verifyExceptionWhenRoleNotInPermissionsMap() {
		roleToPermissionsMappingService.getPermissionsForRole("FAKE_ROLE");
	}

}
