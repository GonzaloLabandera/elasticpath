/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;

/**
 * Tests for {@link CustomerDefaultPaymentInstrumentRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerDefaultPaymentInstrumentRepositoryTest {

	@InjectMocks
	private CustomerDefaultPaymentInstrumentRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService;

	@Mock
	private Customer customer;

	private final CustomerPaymentInstrument customerPaymentInstrument = mock(CustomerPaymentInstrument.class);


	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
	}

	@Test
	public void getDefaultReturnsDefaultInstrumentWhenOneExists() {
		when(customerDefaultPaymentInstrumentService.getDefaultForCustomer(customer)).thenReturn(customerPaymentInstrument);
		repository.getDefaultForCustomer(customer)
				.test()
				.assertNoErrors()
				.assertValue(customerPaymentInstrument);
	}

	@Test
	public void getDefaultReturnsEmptyWhenCustomerDoesNotHaveDefault() {
		repository.getDefaultForCustomer(customer)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void hasDefaultPaymentInstrumentReturnsTrueWhenDefaultInstrumentExists() {
		when(customerDefaultPaymentInstrumentService.hasDefaultPaymentInstrument(customer)).thenReturn(true);
		repository.hasDefaultPaymentInstrument(customer)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void hasDefaultPaymentInstrumentReturnsFalseWhenNoDefaultInstrumentExists() {
		when(customerDefaultPaymentInstrumentService.hasDefaultPaymentInstrument(customer)).thenReturn(false);
		repository.hasDefaultPaymentInstrument(customer)
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void isDefaultReturnsTrueWhenDefaultInstrumentQueried() {
		when(customerDefaultPaymentInstrumentService.isDefault(customerPaymentInstrument)).thenReturn(true);
		repository.isDefault(customerPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void isDefaultReturnsFalseQueriedInstrumentIsNotDefault() {
		when(customerDefaultPaymentInstrumentService.isDefault(customerPaymentInstrument)).thenReturn(false);
		repository.isDefault(customerPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void saveAsDefaultCompletesSuccessfully() {
		repository.saveAsDefault(customerPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertComplete();
	}

	@Test
	public void saveAsDefaultFails() {
		EpServiceException exception = new EpServiceException("Failure");

		doThrow(exception).when(customerDefaultPaymentInstrumentService).saveAsDefault(customerPaymentInstrument);

		repository.saveAsDefault(customerPaymentInstrument)
				.test()
				.assertNoValues()
				.assertNotComplete()
				.assertError(exception);
	}
}
