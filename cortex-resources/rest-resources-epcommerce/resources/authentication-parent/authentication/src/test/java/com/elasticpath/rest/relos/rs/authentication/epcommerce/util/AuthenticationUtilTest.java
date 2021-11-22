/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.core.GrantedAuthority;

import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.rest.identity.RolePrincipal;
import com.elasticpath.rest.identity.TestRoleConstants;
import com.elasticpath.rest.util.collection.CollectionUtil;
import com.elasticpath.service.permissions.RoleValidator;
import com.elasticpath.service.permissions.impl.RoleValidatorImpl;

/**
 * Test that the authentication utilities behave as expected.
 */
public class AuthenticationUtilTest {
	private static final String STORE_CODE = "STORE_CODE";
	private static final String USERNAME = "USERNAME";
	private static final RoleValidator ROLE_VALIDATOR = new RoleValidatorImpl(Collections.singleton("PUBLIC"));

	@Rule
	public ExpectedException thrownException = ExpectedException.none();

	/**
	 * Test create authorities.
	 */
	@Test
	public void testCreateAuthorities() {
		Collection<GrantedAuthority> authorities = AuthenticationUtil.createAuthorities(Collections.singleton(TestRoleConstants.REGISTERED_ROLE));
		assertThat("There should be 1 authority", authorities, Matchers.hasSize(1));
		assertEquals("The authority name should match", TestRoleConstants.REGISTERED, CollectionUtil.first(authorities).getAuthority());
	}

	/**
	 * Test create authorities with an empty collection of principals.
	 */
	@Test
	public void testCreateEmptyAuthorities() {
		Collection<GrantedAuthority> authorities = AuthenticationUtil.createAuthorities(Collections.<RolePrincipal>emptySet());
		assertTrue("The result set should be empty", authorities.isEmpty());
	}

	/**
	 * Test that the scope and username can be combined and split as expected.
	 */
	@Test
	public void testSplitAndCombine() {
		String combined = AuthenticationUtil.combinePrincipals(STORE_CODE, USERNAME);
		String[] splitScopeAndUsername = AuthenticationUtil.splitPrincipals(combined);
		assertEquals("The results should be a 2 element array", 2, splitScopeAndUsername.length);
		assertEquals("The scope should round-trip correctly", STORE_CODE, splitScopeAndUsername[0]);
		assertEquals("The username should round-trip correctly", USERNAME, splitScopeAndUsername[1]);
	}

	/**
	 * Test split with an invalid string.
	 */
	@Test(expected = AssertionError.class)
	public void testSplitInvalidString() {
		AuthenticationUtil.splitPrincipals("invalid");
	}

	/**
	 * Test that empty roles are not valid.
	 */
	@Test
	public void testEmptyRoles() {
		thrownException.expect(EpStructureErrorMessageException.class);
		thrownException.expectMessage("Missing role headers: [error,authentication.missing.header,Missing role headers,{}]");
		AuthenticationUtil.isValidRoles(new ArrayList<>(), ROLE_VALIDATOR);
	}

	/**
	 * Test that multiple roles are not valid.
	 */
	@Test
	public void testMultipleRoles() throws EpStructureErrorMessageException {
		thrownException.expect(EpStructureErrorMessageException.class);
		thrownException.expectMessage("Too many roles in request header: [error,authentication.too.many.roles,Too many roles in request header,{}]");
		AuthenticationUtil.isValidRoles(Arrays.asList("role1", "role2"), ROLE_VALIDATOR);
	}

	/**
	 * Test that invalid role is not valid.
	 */
	@Test
	public void testInvalidRole() {
		thrownException.expect(EpStructureErrorMessageException.class);
		thrownException.expectMessage("Current role is invalid. Valid roles are: PUBLIC: "
				+ "[error,authentication.wrong.role,Current role is invalid. Valid roles are: PUBLIC,{}]");
		AuthenticationUtil.isValidRoles(Collections.singletonList("role1"), ROLE_VALIDATOR);
	}

	/**
	 * Test valid role.
	 */
	@Test
	public void testValidRole() {
		// Since Junit 4 does not have a way to assert no exception is thrown, this test fails when the isValidRoles call results in an exception.
		AuthenticationUtil.isValidRoles(Collections.singletonList("PUBLIC"), ROLE_VALIDATOR);
	}
}
