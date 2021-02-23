/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.UserAccountAssociationImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * Test <code>UserAccountAssociationServiceImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAccountAssociationServiceImplTest {

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private CustomerService customerService;

	@Mock
	private ElasticPath elasticPath;

	@Mock
	private FetchPlanHelper fetchPlanHelper;

	@Mock
	private RoleToPermissionsMappingService roleToPermissionMappingService;

	@InjectMocks
	private UserAccountAssociationServiceImpl userAccountAssociationService;

	@Mock
	private Customer account;

	@Mock
	private Customer user;

	private static final String USER_GUID = "userGuid";

	private static final String ACCOUNT_GUID = "accountGuid";

	private static final String ACCOUNT_ROLE = "BUYER";

	private static final String BUYER_ROLE = "BUYER";

	@Mock
	private UserAccountAssociation userAccountAssociation;

	@Before
	public void setUp() {
		userAccountAssociationService.setElasticPath(elasticPath);
		userAccountAssociationService.setFetchPlanHelper(fetchPlanHelper);
		userAccountAssociationService.setPersistenceEngine(persistenceEngine);
		when(elasticPath.getPrototypeBean(ContextIdNames.USER_ACCOUNT_ASSOCIATION,
				UserAccountAssociationImpl.class)).thenReturn(new UserAccountAssociationImpl());
		when(persistenceEngine.saveOrUpdate(any(UserAccountAssociation.class))).thenReturn(userAccountAssociation);
		when(customerService.isCustomerGuidExists(ACCOUNT_GUID)).thenReturn(true);
		when(customerService.getCustomerTypeByGuid(ACCOUNT_GUID)).thenReturn(CustomerType.ACCOUNT);
		when(customerService.isCustomerGuidExists(USER_GUID)).thenReturn(true);
		when(customerService.getCustomerTypeByGuid(USER_GUID)).thenReturn(CustomerType.REGISTERED_USER);
		when(user.getGuid()).thenReturn(USER_GUID);
		when(account.getGuid()).thenReturn(ACCOUNT_GUID);
		when(userAccountAssociation.getAccountGuid()).thenReturn(ACCOUNT_GUID);
		when(userAccountAssociation.getUserGuid()).thenReturn(USER_GUID);
		when(userAccountAssociation.getAccountRole()).thenReturn(BUYER_ROLE);
		when(roleToPermissionMappingService.getDefinedRoleKeys()).thenReturn(Collections.singleton(BUYER_ROLE));
	}

	/**
	 * Test associating a user to an account.
	 */
	@Test
	public void testAssociateUserToAccount() {
		UserAccountAssociation userAccountAssociation = userAccountAssociationService.associateUserToAccount(user, account, BUYER_ROLE);
		assertThat(userAccountAssociation.getUserGuid()).isEqualTo(USER_GUID);
		assertThat(userAccountAssociation.getAccountGuid()).isEqualTo(ACCOUNT_GUID);
		assertThat(userAccountAssociation.getAccountRole()).isEqualTo(BUYER_ROLE);
	}

	/**
	 * Test disassociating a user from an account.
	 */
	@Test
	public void testDisassociateUserFromAccount() {
		assertThatCode(() -> userAccountAssociationService.disassociateUserFromAccount(user, account)).doesNotThrowAnyException();
	}

	/**
	 * Test exception is thrown if the account does not exist.
	 */
	@Test
	public void testAccountDoesntExist() {
		when(customerService.isCustomerGuidExists(ACCOUNT_GUID)).thenReturn(false);

		assertThatThrownBy(() -> userAccountAssociationService.associateUserToAccount(USER_GUID, ACCOUNT_GUID, ACCOUNT_ROLE))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageStartingWith("Invalid UserAccountAssociation: The supplied account guid does not exist: " + ACCOUNT_GUID);
	}

	/**
	 * Test exception is thrown if the user does not exist.
	 */
	@Test
	public void testUserDoesntExist() {
		when(customerService.isCustomerGuidExists(USER_GUID)).thenReturn(false);

		assertThatThrownBy(() -> userAccountAssociationService.associateUserToAccount(USER_GUID, ACCOUNT_GUID, ACCOUNT_ROLE))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageStartingWith("Invalid UserAccountAssociation: The supplied user guid does not exist: " + USER_GUID);
	}

	/**
	 * Test exception is thrown if the role is missing.
	 */
	@Test
	public void testMissingRole() {
		assertThatThrownBy(() -> userAccountAssociationService.associateUserToAccount(USER_GUID, ACCOUNT_GUID, null))

				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageStartingWith("Invalid UserAccountAssociation: Missing role.");
	}

	/**
	 * Test exception is thrown if the role is invalid.
	 */
	@Test
	public void testInvalidRole() {
		assertThatThrownBy(() -> userAccountAssociationService.associateUserToAccount(USER_GUID, ACCOUNT_GUID, "invalid_role"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageStartingWith("Invalid UserAccountAssociation: The supplied role invalid_role is not a valid role. ");
	}

}
