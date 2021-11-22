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

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test class for {@link CustomerIdentifierByGuidStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerIdentifierByGuidStrategyImplTest {

	private static final String VALID_GUID = "testGuid";
	private static final String INVALID_GUID = "invalidGuid";
	private static final String VALID_CUSTOMER_IDENTIFIER_KEY = "GUID";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerIdentifierByGuidStrategyImpl customerIdentifierByGuidStrategy;

	/**
	 * Test isCustomerExists with valid guid value.
	 */
	@Test
	public void testValidCustomerExists() {
		when(customerRepository.isCustomerGuidExists(VALID_GUID))
				.thenReturn(ExecutionResultFactory.createReadOK(null));

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByGuidStrategy.isCustomerExists(VALID_GUID, CustomerType.REGISTERED_USER, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertTrue(executionResult.isSuccessful());
	}

	/**
	 * Test isCustomerExists with invalid guid value.
	 */
	@Test
	public void testInvalidCustomerExists() {
		when(customerRepository.isCustomerGuidExists(INVALID_GUID))
				.thenReturn(ExecutionResultFactory.createNotFound());

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByGuidStrategy.isCustomerExists(INVALID_GUID, CustomerType.REGISTERED_USER, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertFalse(executionResult.isSuccessful());
	}

	/**
	 * Test for deriveCustomerGuid method.
	 */
	@Test
	public void deriveCustomerGuid() {
		when(customerRepository.isCustomerGuidExists(VALID_GUID))
				.thenReturn(ExecutionResultFactory.createReadOK(null));

		ExecutionResult<String> executionResult = customerIdentifierByGuidStrategy.deriveCustomerGuid(VALID_GUID, CustomerType.REGISTERED_USER,
				VALID_CUSTOMER_IDENTIFIER_KEY);
		assertTrue(executionResult.isSuccessful());
		assertEquals(VALID_GUID, executionResult.getData());
	}
}