/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test class for {@link CustomerIdentifierByAttributeValueKeyStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerIdentifierByAttributeValueKeyStrategyImplTest {

	private static final String VALID_USER_ID = "punchoutCustomer";
	private static final String INVALID_USER_ID = "invalidValue";
	private static final String VALID_STORE_CODE = "TestStore";
	private static final String VALID_CUSTOMER_IDENTIFIER_KEY = "ATTRIBUTE_VALUE:CP_PUNCHOUT_CUSTOMER_KEY";
	private static final String CUSTOMER_GUID = "customerGuid";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerIdentifierByAttributeValueKeyStrategyImpl customerIdentifierByAttributeValueKeyStrategy;

	/**
	 * Test isCustomerExists with valid user Id value.
	 */
	@Test
	public void testValidCustomerExists() {
		when(customerRepository.getCustomerCountByProfileAttributeKeyAndValue(VALID_CUSTOMER_IDENTIFIER_KEY.split(":")[1], VALID_USER_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(1L));

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByAttributeValueKeyStrategy.isCustomerExists(VALID_USER_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertTrue(executionResult.isSuccessful());
	}

	/**
	 * Test isCustomerExists with invalid user Id value for which no customer exists.
	 */
	@Test
	public void testNoCustomerExists() {
		String customerIdentifierKey =
				getKeyFieldStringFromCustomerIdentifierKey();

		when(customerRepository.getCustomerCountByProfileAttributeKeyAndValue(customerIdentifierKey, VALID_USER_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(0L));

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByAttributeValueKeyStrategy.isCustomerExists(VALID_USER_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);

		String errorMessage = CustomerIdentifierByAttributeValueKeyStrategyImpl.prepareNoCustomerFoundError(customerIdentifierKey, VALID_USER_ID);

		assertFalse(executionResult.isSuccessful());
		assertEquals(ExecutionResultFactory.createBadRequestBody(errorMessage), executionResult);
		assertEquals(errorMessage, executionResult.getErrorMessage());
	}

	/**
	 * Test isCustomerExists returns an error if multiple customers match the given input.
	 */
	@Test
	public void testMultipleCustomersWithGivenKeyValueError() {
		when(customerRepository.getCustomerCountByProfileAttributeKeyAndValue(getKeyFieldStringFromCustomerIdentifierKey(), VALID_USER_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(2L));

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByAttributeValueKeyStrategy.isCustomerExists(VALID_USER_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);

		String errorMessage =
				CustomerIdentifierByAttributeValueKeyStrategyImpl.prepareMultipleCustomersFoundError(getKeyFieldStringFromCustomerIdentifierKey(),
						VALID_USER_ID);

		assertEquals(ExecutionResultFactory.createBadRequestBody(errorMessage), executionResult);
		assertEquals(errorMessage, executionResult.getErrorMessage());
	}

	/**
	 * Test deriveCustomer with valid user Id value.
	 */
	@Test
	public void testDeriveValidCustomer() {
		when(customerRepository.findCustomerGuidByProfileAttributeKeyAndValue(getKeyFieldStringFromCustomerIdentifierKey(), VALID_USER_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(CUSTOMER_GUID));

		ExecutionResult<String> executionResult =
				customerIdentifierByAttributeValueKeyStrategy.deriveCustomerGuid(VALID_USER_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);

		assertTrue(executionResult.isSuccessful());
		assertEquals(CUSTOMER_GUID, executionResult.getData());
	}

	/**
	 * Test deriveCustomer with invalid user Id value.
	 */
	@Test
	public void testDeriveInvalidCustomer() {
		String customerIdentifierKey = getKeyFieldStringFromCustomerIdentifierKey();
		when(customerRepository.findCustomerGuidByProfileAttributeKeyAndValue(customerIdentifierKey, INVALID_USER_ID))
				.thenReturn(ExecutionResultFactory.createNotFound());

		ExecutionResult<String> executionResult =
				customerIdentifierByAttributeValueKeyStrategy.deriveCustomerGuid(INVALID_USER_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertFalse(executionResult.isSuccessful());
		assertEquals(CustomerIdentifierByAttributeValueKeyStrategyImpl.prepareNoCustomerFoundError(customerIdentifierKey, INVALID_USER_ID),
				executionResult.getErrorMessage());
	}

	private String getKeyFieldStringFromCustomerIdentifierKey() {
		return CustomerIdentifierByAttributeValueKeyStrategyImpl.getKeyFieldStringFromCustomerIdentifierKey(VALID_CUSTOMER_IDENTIFIER_KEY);
	}
}