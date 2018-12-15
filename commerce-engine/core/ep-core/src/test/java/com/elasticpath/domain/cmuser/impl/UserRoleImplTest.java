/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.cmuser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.cmuser.UserPermission;
import com.elasticpath.domain.cmuser.UserRole;

/**
 * Test of the public API of <code>UserRoleImpl</code>.
 */
public class UserRoleImplTest {

	private UserRoleImpl userRoleImpl;

	/**
	 * Prepares for the next test.
	 *
	 * @throws Exception if something goes wrong.
	 */
	@Before
	public void setUp() throws Exception {
		this.userRoleImpl = new UserRoleImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.UserRoleImpl.getName()'.
	 */
	@Test
	public void testGetName() {
		assertThat(userRoleImpl.getName()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.UserRoleImpl.setName(String)'.
	 */
	@Test
	public void testSetName() {
		final String roleName = "CSR";
		userRoleImpl.setName(roleName);
		assertThat(userRoleImpl.getName()).isEqualTo(roleName);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.UserRoleImpl.getUserPermissionMap()'.
	 */
	@Test
	public void testGetUserPermissionMap() {
		final Set<UserPermission> userPermissions = userRoleImpl.getUserPermissions();
		assertThat(userPermissions).as("Check getUserPermissions").isEmpty();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.UserRoleImpl.isSuperUserRole()'.
	 */
	@Test
	public void testIsSuperUserRole() {
		assertThat(userRoleImpl.isSuperUserRole()).isFalse();

		userRoleImpl.setName(UserRole.SUPERUSER);
		assertThat(userRoleImpl.isSuperUserRole()).isTrue();
	}
	
	/**
	 * Test that the WSUSER role is permanent.
	 */
	@Test
	public void testIsPermanentRoleWsUser() {
		userRoleImpl.setName(UserRole.WSUSER);
		assertThat(userRoleImpl.isUnmodifiableRole()).isTrue();
	}
	
	/**
	 * Test that the CMUSER role is permanent.
	 */
	@Test
	public void testIsPermanentRoleCmUser() {
		userRoleImpl.setName(UserRole.CMUSER);
		assertThat(userRoleImpl.isUnmodifiableRole()).isTrue();
	}

	/**
	 * Test that UserRole can add a user permission.
	 */
	@Test
	public void testAddUserPermission() {
		UserPermission userPermission = mock(UserPermission.class);
		
		userRoleImpl.addUserPermission(userPermission);

		assertThat(userRoleImpl.getUserPermissions()).contains(userPermission);
	}

	/**
	 * Test that UserRole can remove a user permission.
	 */
	@Test
	public void testRemoveUserPermission() {
		UserPermission userPermission = mock(UserPermission.class);
		
		userRoleImpl.addUserPermission(userPermission);
		userRoleImpl.removeUserPermission(userPermission);
		assertThat(userRoleImpl.getUserPermissions()).doesNotContain(userPermission);
	}
}
