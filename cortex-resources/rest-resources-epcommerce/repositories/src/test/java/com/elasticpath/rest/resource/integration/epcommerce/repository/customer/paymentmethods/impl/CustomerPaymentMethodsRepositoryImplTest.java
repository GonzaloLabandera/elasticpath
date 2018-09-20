/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.paymentmethods.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.CustomerPaymentMethods;
import com.elasticpath.domain.customer.impl.CustomerCreditCardImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test that {@link CustomerPaymentMethodsRepositoryImpl} behaves as expected.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerPaymentMethodsRepositoryImplTest {

	private static final String CUSTOMER_GUID = "customer";
	private static final String CREDIT_CARD_IDENTIFIER = "2000";

	private static final String RESULT_FAILURE_ASSERT_MESSAGE = "The operation should have failed";
	private static final String RESULT_STATUS_NOT_FOUND_MESSAGE = "Result status should be NOT FOUND";

	@Mock
	private CustomerRepository mockCustomerRepository;
	@InjectMocks
	private CustomerPaymentMethodsRepositoryImpl repository;

	private final Customer customer = new CustomerImpl();

	@Test
	public void ensurePaymentMethodFoundWhenPresent() {
		customer.setCreditCards(Collections.singletonList(getTestCreditCard()));

		shouldFindCustomer();
		ExecutionResult<PaymentMethod> result = repository.findPaymentMethodByCustomerGuidAndPaymentMethodId(CUSTOMER_GUID, CREDIT_CARD_IDENTIFIER);

		assertTrue("The operation should have been successful", result.isSuccessful());
		assertEquals("The result should be the credit card belonging to the customer", getTestCreditCard(), result.getData());
	}


	@Test
	public void ensurePaymentMethodNotFoundWhenCustomerNotFound() {
		shouldNotFindCustomer();

		ExecutionResult<PaymentMethod> result = repository.findPaymentMethodByCustomerGuidAndPaymentMethodId(CUSTOMER_GUID, CREDIT_CARD_IDENTIFIER);
		assertTrue(RESULT_FAILURE_ASSERT_MESSAGE, result.isFailure());
		assertEquals(RESULT_STATUS_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void ensurePaymentMethodNotFoundWhenPaymentMethodIdIsInvalid() {
		customer.setCreditCards(Collections.singletonList(getTestCreditCard()));
		shouldFindCustomer();

		ExecutionResult<PaymentMethod> result = repository.findPaymentMethodByCustomerGuidAndPaymentMethodId(CUSTOMER_GUID, String.valueOf(1L));
		assertTrue(RESULT_FAILURE_ASSERT_MESSAGE, result.isFailure());
		assertEquals(RESULT_STATUS_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void ensurePaymentMethodNotFoundReturnedWhenNoCustomerPaymentMethods() {
		shouldFindCustomer();

		ExecutionResult<PaymentMethod> result = repository.findPaymentMethodByCustomerGuidAndPaymentMethodId(CUSTOMER_GUID, CREDIT_CARD_IDENTIFIER);
		assertTrue(RESULT_FAILURE_ASSERT_MESSAGE, result.isFailure());
		assertEquals(RESULT_STATUS_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void ensurePaymentMethodsNotFoundWhenNonePresent() {
		shouldFindCustomer();

		ExecutionResult<CustomerPaymentMethods> result = repository.findPaymentMethodsByCustomerGuid(CUSTOMER_GUID);
		assertTrue(RESULT_FAILURE_ASSERT_MESSAGE, result.isFailure());
		assertEquals(RESULT_STATUS_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void ensureDefaultPaymentMethodFoundWhenPresent() {
		PaymentMethod defaultPaymentMethod = new PaymentMethod() { };
		
		customer.getPaymentMethods().setDefault(defaultPaymentMethod);

		shouldFindCustomer();

		ExecutionResult<PaymentMethod> result = repository.findDefaultPaymentMethodByCustomerGuid(CUSTOMER_GUID);
		assertTrue("Default credit card result should be successful.", result.isSuccessful());
		assertEquals("Default credit card returned does not match expected credit card.", defaultPaymentMethod, result.getData());
	}

	@Test
	public void ensureDefaultPaymentMethodNotFoundWhenNoDefaultPaymentMethod() {
		shouldFindCustomer();

		ExecutionResult<PaymentMethod> result = repository.findDefaultPaymentMethodByCustomerGuid(CUSTOMER_GUID);
		assertTrue(RESULT_FAILURE_ASSERT_MESSAGE, result.isFailure());
		assertEquals(RESULT_STATUS_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void ensureDefaultPaymentMethodNotFoundWhenCustomerIsNotFound() {
		shouldNotFindCustomer();

		ExecutionResult<PaymentMethod> result = repository.findDefaultPaymentMethodByCustomerGuid(CUSTOMER_GUID);
		assertTrue(RESULT_FAILURE_ASSERT_MESSAGE, result.isFailure());
		assertEquals(RESULT_STATUS_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	private CustomerCreditCard getTestCreditCard() {
		CustomerCreditCard creditCard = new CustomerCreditCardImpl();
		creditCard.setUidPk(Long.valueOf(CREDIT_CARD_IDENTIFIER));
		return creditCard;
	}

	private void shouldFindCustomer() {
		when(mockCustomerRepository.findCustomerByGuid(CUSTOMER_GUID)).thenReturn(ExecutionResultFactory.createReadOK(customer));
	}

	private void shouldNotFindCustomer() {
		when(mockCustomerRepository.findCustomerByGuid(CUSTOMER_GUID))
				.thenReturn(ExecutionResultFactory.<Customer>createNotFound("Customer not found"));
	}
}
