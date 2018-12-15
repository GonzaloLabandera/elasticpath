/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.cmuser.UserStatus;

/**
 * Test <code>UserSearchCriteria</code>.
 */
public class UserSearchCriteriaTest {


	private UserSearchCriteria userSearchCriteria;

	@Before
	public void setUp() throws Exception {
		this.userSearchCriteria = new UserSearchCriteria();
	}

	/**
	 * Test method for 'com.elasticpath.service.search.query.UserSearchCriteria.getUserRole()'.
	 */
	@Test
	public void testGetUserRole() {
		assertThat(this.userSearchCriteria.getUserRoleName()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.service.search.query.UserSearchCriteria.getUserStatus()'.
	 */
	@Test
	public void testGetUserState() {
		assertThat(this.userSearchCriteria.getUserStatus()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.service.search.query.UserSearchCriteria.setUserStatus'.
	 */
	@Test
	public void testSetUserStatus() {
		final UserStatus userStatus = UserStatus.ENABLED;
		this.userSearchCriteria.setUserStatus(userStatus);
		assertThat(this.userSearchCriteria.getUserStatus()).isEqualTo(userStatus);
	}

	/**
	 * Test method for 'com.elasticpath.domain.search.impl.UserSearchCriteria.clear()'.
	 */
	@Test
	public void testClear() {
		assertThat(this.userSearchCriteria.isEmpty()).isTrue();
		userSearchCriteria.setUserStatus(UserStatus.ENABLED);
		userSearchCriteria.setCatalogCode("Cat01");
		userSearchCriteria.setStoreCode("Store01");
		userSearchCriteria.setEmail("user@server.com");
		userSearchCriteria.setFirstName("Peter");
		userSearchCriteria.setLastName("Jensen");
		userSearchCriteria.setUserName("Peter Jensen");
		assertThat(this.userSearchCriteria.isEmpty()).isFalse();

		this.userSearchCriteria.clear();
		assertThat(this.userSearchCriteria.isEmpty()).isTrue();
	}
}
