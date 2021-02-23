/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.elasticpath.rest.relos.rs.subject.SubjectHeaderConstants;
import com.elasticpath.service.auth.ShiroRolesDeterminationService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Test for {@link RoleToPermissionsExpandingFilterTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleToPermissionsExpandingFilterTest {
	private static final String SCOPE = "mobee";
	private static final String MODIFY_CART_ROLE = "MODIFY_CART";
	private static final String READ_PRICES_ROLE = "READ_PRICES";
	private static final String ACCOUNT_SHARED_ID = "ACCOUNT_SHARED_ID";
	private static final String ACCOUNT_GUID = "ACCOUNT_GUID";
	private static final String PUBLIC_HEADER_VALUE = "PUBLIC";
	private static final String USER_GUID = "USER_GUID";
	private static final String OWNER_ROLE = "OWNER";

	@Mock
	private ShiroRolesDeterminationService shiroRolesDeterminationService;

	@Mock
	private CustomerService customerService;

	@InjectMocks
	private RoleToPermissionsExpandingFilter roleToPermissionsExpandingFilter;

	private final MockFilterChain mockFilterChain = new MockFilterChain();
	private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
	private final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

	@Before
	public void setup() {
		mockRequest.addHeader(SubjectHeaderConstants.USER_SCOPE, SCOPE);
		mockRequest.addHeader(SubjectHeaderConstants.USER_ID, USER_GUID);

		when(customerService.findCustomerGuidBySharedId(ACCOUNT_SHARED_ID)).thenReturn(ACCOUNT_GUID);
	}

	/**
	 * Check that filter added roles for Account.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void checkThatAddedRolesForAccount() throws IOException, ServletException {
		mockRequest.addHeader(SubjectHeaderConstants.ACCOUNT_SHARED_ID, ACCOUNT_SHARED_ID);

		when(shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, ACCOUNT_GUID))
				.thenReturn(Collections.singleton(MODIFY_CART_ROLE));

		roleToPermissionsExpandingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertThatRequestContainsRoleInHeader(MODIFY_CART_ROLE);
	}

	/**
	 * Check that filter not rewrite roles for Account.
	 */
	@Test
	public void checkThatFilterNotRewriteRolesForAccount() throws IOException, ServletException {
		mockRequest.addHeader(SubjectHeaderConstants.ACCOUNT_SHARED_ID, ACCOUNT_SHARED_ID);
		mockRequest.addHeader(SubjectHeaderConstants.USER_ROLES, PUBLIC_HEADER_VALUE);

		when(shiroRolesDeterminationService.determineShiroRoles(SCOPE, false, USER_GUID, ACCOUNT_GUID))
				.thenReturn(Collections.singleton(MODIFY_CART_ROLE));

		roleToPermissionsExpandingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertThatRequestContainsRoleInHeader(PUBLIC_HEADER_VALUE, MODIFY_CART_ROLE);
	}

	/**
	 * Check that filter added roles for Registered User.
	 */
	@Test
	public void checkThatAddedRolesForRegisteredUser() throws IOException, ServletException {
		when(shiroRolesDeterminationService.determineShiroRoles(SCOPE, true, USER_GUID, null))
				.thenReturn(Collections.singleton(MODIFY_CART_ROLE));

		roleToPermissionsExpandingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertThatRequestContainsRoleInHeader(MODIFY_CART_ROLE);
	}

	/**
	 * Check that filter added roles for Public User.
	 */
	@Test
	public void checkThatAddedRolesForPublicUser() throws IOException, ServletException {
		mockRequest.addHeader(SubjectHeaderConstants.USER_ROLES, PUBLIC_HEADER_VALUE);

		when(shiroRolesDeterminationService.determineShiroRoles(SCOPE, false, USER_GUID, null))
				.thenReturn(Collections.singleton(READ_PRICES_ROLE));

		roleToPermissionsExpandingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertThatRequestContainsRoleInHeader(READ_PRICES_ROLE);
	}

	private void assertThatRequestContainsRoleInHeader(final String... additionalHeaders) {
		HttpServletRequest chainRequest = (HttpServletRequest) mockFilterChain.getRequest();

		assertThat(Collections.list(chainRequest.getHeaders(SubjectHeaderConstants.USER_ROLES))).asList().contains(OWNER_ROLE);
		Arrays.stream(additionalHeaders)
				.forEach(header -> assertThat(Collections.list(chainRequest.getHeaders(SubjectHeaderConstants.USER_ROLES)))
						.asList()
						.contains(header));
	}
}
