/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.identity.RolePrincipal;
import com.elasticpath.rest.relos.rs.authentication.User;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.transformer.CustomerTransformer;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;


/**
 * Test class for {@link UserAuthenticationDetailsServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticationDetailsServiceImplTest {

	private static final String INVALID_USER_NAME = "invalidUser";
	private static final String DISABLED_USER_NAME = "DISABLED_USER_NAME";
	private static final String INVALID_STORE_CODE = "INVALID_STORE_CODE";
	private static final String USER_GUID = "00000000-0000-0000-0000-000000000000";
	private static final String DEFAULT_STORE_CODE = "SNAPITUP";
	private static final String VALID_USER_NAME = "validUsername";
	private static final String VALID_PASSWORD = "validPassword";

	@Mock
	private CustomerRepository customerRepository;
	@Mock
	private CustomerTransformer mockCustomerTransformer;
	@Mock
	private Customer customer;
	@Mock
	private CustomerAuthentication customerAuthentication;

	@InjectMocks
	private UserAuthenticationDetailsServiceImpl userAuthenticationDetailsService;


	/**
	 * Test loadUserByUsername() with valid user.
	 */
	@Test
	public void testLoadByUserName() {
		User expectedUser = createValidUser();

		when(customerRepository.findCustomerByUserId(DEFAULT_STORE_CODE, VALID_USER_NAME))
				.thenReturn(ExecutionResultFactory.createReadOK(customer));
		when(customer.getCustomerAuthentication()).thenReturn(customerAuthentication);
		when(customer.isEnabled()).thenReturn(true);
		when(mockCustomerTransformer.transform(customer)).thenReturn(expectedUser);

		String storePlusUser = AuthenticationUtil.combinePrincipals(DEFAULT_STORE_CODE, VALID_USER_NAME);
		UserDetails userDetails = userAuthenticationDetailsService.loadUserByUsername(storePlusUser);
		assertEquals(VALID_USER_NAME, userDetails.getUsername());
	}

	/**
	 * Test loadUserByUsername() with invalid user.
	 */
	@Test(expected = UsernameNotFoundException.class)
	public void testLoadWithInvalidStoreCode() {
		when(customerRepository.findCustomerByUserId(INVALID_STORE_CODE, INVALID_STORE_CODE))
				.thenReturn(ExecutionResultFactory.createNotFound());
		userAuthenticationDetailsService.loadUserByUsername(INVALID_STORE_CODE, INVALID_STORE_CODE);
	}

	/**
	 * Test loadUserByUsername() with invalid user.
	 */
	@Test(expected = UsernameNotFoundException.class)
	public void testLoadInvalidUser() {
		when(customerRepository.findCustomerByUserId(DEFAULT_STORE_CODE, INVALID_USER_NAME))
				.thenReturn(ExecutionResultFactory.createNotFound());

		String storePlusUser = AuthenticationUtil.combinePrincipals(DEFAULT_STORE_CODE, INVALID_USER_NAME);
		userAuthenticationDetailsService.loadUserByUsername(storePlusUser);
	}

	/**
	 * Test loadUserByUsername() with unauthenticatable user.
	 */
	@Test(expected = UsernameNotFoundException.class)
	public void testLoadUnauthenticatableUser() {
		when(customerRepository.findCustomerByUserId(DEFAULT_STORE_CODE, DISABLED_USER_NAME))
				.thenReturn(ExecutionResultFactory.createReadOK(customer));
		when(customer.getCustomerAuthentication()).thenReturn(null);

		String storePlusUser = AuthenticationUtil.combinePrincipals(DEFAULT_STORE_CODE, DISABLED_USER_NAME);
		userAuthenticationDetailsService.loadUserByUsername(storePlusUser);
	}

	/**
	 * Test loadUserByUsername() with disabled user.
	 */
	@Test(expected = UsernameNotFoundException.class)
	public void testLoadDisabledUser() {
		when(customerRepository.findCustomerByUserId(DEFAULT_STORE_CODE, DISABLED_USER_NAME))
				.thenReturn(ExecutionResultFactory.createReadOK(customer));
		when(customer.getCustomerAuthentication()).thenReturn(customerAuthentication);
		when(customer.isEnabled()).thenReturn(false);

		String storePlusUser = AuthenticationUtil.combinePrincipals(DEFAULT_STORE_CODE, DISABLED_USER_NAME);
		userAuthenticationDetailsService.loadUserByUsername(storePlusUser);
	}

	private User createValidUser() {
		Collection<RolePrincipal> principals = new ArrayList<>();
		User user = new UserImpl();
		user.setUsername(VALID_USER_NAME)
				.setPassword(VALID_PASSWORD)
				.setPrincipals(principals)
				.setUserId(USER_GUID)
				.setAccountEnabled(true)
				.setAccountExpired(false)
				.setAccountLocked(false);
		return user;
	}
}
