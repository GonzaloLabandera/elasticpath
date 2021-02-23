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
 * Test class for {@link CustomerIdentifierBySharedIdStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerIdentifierBySharedIdStrategyImplTest {

	private static final String VALID_SHARED_ID = "testUser";
	private static final String INVALID_SHARED_ID = "invalidSharedId";
	private static final String VALID_STORE_CODE = "TestStore";
	private static final String VALID_CUSTOMER_IDENTIFIER_KEY = "SHARED_ID";
	private static final String CUSTOMER_GUID = "customerGuid";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerIdentifierBySharedIdStrategyImpl customerIdentifierByUserIdStrategy;

	/**
	 * Test isCustomerExists with valid user Id value.
	 */
	@Test
	public void testValidCustomerExists() {
		when(customerRepository.isCustomerExistsBySharedIdAndStoreCode(VALID_STORE_CODE, VALID_SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(null));

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByUserIdStrategy.isCustomerExists(VALID_SHARED_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertTrue(executionResult.isSuccessful());
	}

	/**
	 * Test isCustomerExists with invalid user Id value.
	 */
	@Test
	public void testCustomerExistsWithInvalidUserId() {
		when(customerRepository.isCustomerExistsBySharedIdAndStoreCode(VALID_STORE_CODE, INVALID_SHARED_ID))
				.thenReturn(ExecutionResultFactory.createNotFound());

		ExecutionResult<Boolean> executionResult =
				customerIdentifierByUserIdStrategy.isCustomerExists(INVALID_SHARED_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertFalse(executionResult.isSuccessful());
	}

	/**
	 * Test deriveCustomerGuid with valid user Id value.
	 */
	@Test
	public void deriveCustomerGuidValidTest() {
		when(customerRepository.findCustomerGuidBySharedId(VALID_STORE_CODE, VALID_SHARED_ID, VALID_CUSTOMER_IDENTIFIER_KEY))
				.thenReturn(ExecutionResultFactory.createReadOK(CUSTOMER_GUID));

		ExecutionResult<String> executionResult =
				customerIdentifierByUserIdStrategy.deriveCustomerGuid(VALID_SHARED_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertTrue(executionResult.isSuccessful());
		assertEquals(CUSTOMER_GUID, executionResult.getData());
	}

	/**
	 * Test deriveCustomerGuid with invalid user Id value.
	 */
	@Test
	public void deriveCustomerGuidInvalidTest() {
		when(customerRepository.findCustomerGuidBySharedId(VALID_STORE_CODE, INVALID_SHARED_ID, VALID_CUSTOMER_IDENTIFIER_KEY))
				.thenReturn(ExecutionResultFactory.createNotFound());

		ExecutionResult<String> executionResult =
				customerIdentifierByUserIdStrategy.deriveCustomerGuid(INVALID_SHARED_ID, VALID_STORE_CODE, VALID_CUSTOMER_IDENTIFIER_KEY);
		assertTrue(executionResult.isFailure());
	}
}