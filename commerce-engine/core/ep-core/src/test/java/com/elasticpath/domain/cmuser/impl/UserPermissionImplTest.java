/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.cmuser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.EpDomainException;

/**
 * Test of the public API of <code>UserPermissionImpl</code>.
 */
public class UserPermissionImplTest {
	private static final String EP_DOMAIN_EXCEPTION_EXPECTED = "EpDomainException expected.";
	
	private static final String AUTHORITY = "createOrder";

	private UserPermissionImpl userPermissionImpl;
	
	/**
	 * Prepares for the next test.
	 *
	 * @throws Exception if something goes wrong.
	 */
	@Before
	public void setUp() throws Exception {
		userPermissionImpl = new UserPermissionImpl();
	}
	
	/**
	 * Test case for init method.
	 */
	@Test
	public void testInit() {
		assertThatThrownBy(() -> userPermissionImpl.init())
			.as(EP_DOMAIN_EXCEPTION_EXPECTED)
			.isInstanceOf(EpDomainException.class);

		userPermissionImpl.setAuthority(AUTHORITY);
		assertThat(userPermissionImpl.getAuthority()).isEqualTo(AUTHORITY);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.impl.UserPermissionImpl.getAuthority()'.
	 */
	@Test
	public void testGetAuthority() {
		assertThat(userPermissionImpl.getAuthority()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.UserPermissionImpl.setAuthority(String)'.
	 */
	@Test
	public void testSetAuthority() {
		userPermissionImpl.setAuthority(AUTHORITY);
		assertThat(userPermissionImpl.getAuthority()).isEqualTo(AUTHORITY);
	}
}

