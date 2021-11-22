/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import static com.elasticpath.rest.relos.rs.authentication.epcommerce.util.AuthenticationUtil.readBase64EncodedJson;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.relos.rs.subject.SubjectHeaderConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.dto.CustomerDTO;

/**
 * Test for {@link UserIdFallbackFilter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserIdFallBackFilterTest {

	private static final String TEST_USER_ID = "testUserId";
	private static final String SCOPE_ONE = "scopeOne";
	private static final String METADATA_USER_ID = "someId";
	private static final String ISSUER = "ep";
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String METADATA_HEADER = "x-ep-user-metadata";
	private static final String SCOPE_HEADER = "x-ep-user-scope";
	private static final String USER_ID_HEADER = "x-ep-user-id";
	private static final String METADATA_WITH_USER_ID = "{\n"
			+ "   \"user-id\":\"" + METADATA_USER_ID + "\" \n"
			+ "}";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private UserIdFallbackFilter userIdFallbackFilter;

	private final MockFilterChain mockFilterChain = new MockFilterChain();
	private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
	private final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

	/**
	 * Check that processing is skipped if user ID has already been determined.
	 */
	@Test
	public void checkThatProcessingIsSkippedIfUserIdHasAlreadyDetermined() throws IOException, ServletException {
		mockRequest.addHeader(USER_ID_HEADER, TEST_USER_ID);

		userIdFallbackFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertRequestUnchanged();
	}

	/**
	 * Test that processing is skipped if JWT token does not contain metadata claim.
	 */
	@Test
	public void checkThatProcessingIsSkippedIfJWTTokenDoesNotContainMetadata() throws IOException, ServletException {
		mockRequest.addHeader(METADATA_HEADER, StringUtils.EMPTY);

		userIdFallbackFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertRequestUnchanged();
	}

	/**
	 * Check that response status is bad request if user id from metadata is empty.
	 */
	@Test
	public void checkThatResponseStatusIsBadRequestIfUserIdFromMetadataIsEmpty() throws IOException, ServletException {
		mockRequest.addHeader(METADATA_HEADER, METADATA_WITH_USER_ID);

		userIdFallbackFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockResponse.getStatus());
	}

	/**
	 * Check that user id is set if customer returns from customer repository.
	 */
	@Test
	public void checkThatUserIdIsSetIfCustomerReturnsFromCustomerRepository() throws IOException, ServletException {
		final String encodedMetadata = encodeMetadataWithUserIdStringToBase64();
		mockRequest.addHeader(METADATA_HEADER, encodedMetadata);
		mockRequest.addHeader(SCOPE_HEADER, SCOPE_ONE);
		mockRequest.addHeader("x-ep-account-shared-id", ACCOUNT_SHARED_ID);
		mockRequest.addHeader("x-ep-issuer", ISSUER);


		final JsonObject metadata = readBase64EncodedJson(encodedMetadata);
		final CustomerDTO customerDTO = createCustomerDTO(metadata);
		final Customer customer = createSingleSessionUser(TEST_USER_ID);

		when(customerRepository.findOrCreateUser(customerDTO, SCOPE_ONE, METADATA_USER_ID, ACCOUNT_SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(customer));

		userIdFallbackFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		verify(customerRepository).findOrCreateUser(customerDTO, SCOPE_ONE, METADATA_USER_ID, ACCOUNT_SHARED_ID);
		assertThatUserIdPresentInRequest();
	}

	/**
	 * Check that user PUBLIC role is set if customer returns from customer repository.
	 */
	@Test
	public void checkThatUserPublicRoleIsSetIfCustomerReturnsFromCustomerRepository() throws IOException, ServletException {
		final String encodedMetadata = encodeMetadataWithUserIdStringToBase64();
		mockRequest.addHeader(METADATA_HEADER, encodedMetadata);
		mockRequest.addHeader(SCOPE_HEADER, SCOPE_ONE);
		mockRequest.addHeader("x-ep-account-shared-id", ACCOUNT_SHARED_ID);
		mockRequest.addHeader("x-ep-issuer", ISSUER);


		final JsonObject metadata = readBase64EncodedJson(encodedMetadata);
		final CustomerDTO customerDTO = createCustomerDTO(metadata);
		final Customer customer = createSingleSessionUser(TEST_USER_ID);

		when(customerRepository.findOrCreateUser(customerDTO, SCOPE_ONE, METADATA_USER_ID, ACCOUNT_SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(customer));

		userIdFallbackFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

		assertThatUserPublicRolePresentInRequest();
	}

	private CustomerDTO createCustomerDTO(final JsonObject metadata) {
		return new CustomerDTO(metadata, SCOPE_ONE);
	}

	private Customer createSingleSessionUser(final String userGuid) {
		final Customer existingCustomer = new CustomerImpl();
		existingCustomer.setCustomerType(CustomerType.SINGLE_SESSION_USER);
		existingCustomer.setGuid(userGuid);

		return existingCustomer;
	}

	private void assertRequestUnchanged() {
		HttpServletRequest chainRequest = (HttpServletRequest) mockFilterChain.getRequest();
		assertEquals("The filter chain request should be the original request", mockRequest, chainRequest);
	}

	private void assertThatUserIdPresentInRequest() {
		HttpServletRequest chainRequest = (HttpServletRequest) mockFilterChain.getRequest();
		assertEquals("The filter chain request should contains user id", TEST_USER_ID, chainRequest.getHeader(USER_ID_HEADER));
	}

	private void assertThatUserPublicRolePresentInRequest() {
		HttpServletRequest chainRequest = (HttpServletRequest) mockFilterChain.getRequest();
		assertEquals("The filter chain request should contains user id", "PUBLIC", chainRequest.getHeader(SubjectHeaderConstants.USER_ROLES));
	}

	private String encodeMetadataWithUserIdStringToBase64() {
		return Base64.getEncoder().encodeToString(METADATA_WITH_USER_ID.getBytes());
	}
}
