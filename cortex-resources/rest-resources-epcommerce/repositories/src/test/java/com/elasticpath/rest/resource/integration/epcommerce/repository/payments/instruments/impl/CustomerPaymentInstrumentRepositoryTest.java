/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CUSTOMER_PAYMENT_INSTRUMENT_ID;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;

/**
 * Tests for {@link CustomerPaymentInstrumentRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerPaymentInstrumentRepositoryTest {

	@InjectMocks
	private CustomerPaymentInstrumentRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Mock
	private Customer customer;

	private final CustomerPaymentInstrument customerPaymentInstrument = mock(CustomerPaymentInstrument.class);


	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
	}

	@Test
	public void findByCustomerReturnsExpectedCustomerPaymentInstruments() {
		CustomerPaymentInstrument customerPaymentInstrument2 = mock(CustomerPaymentInstrument.class);

		when(customerPaymentInstrumentService.findByCustomer(customer))
				.thenReturn(ImmutableList.of(customerPaymentInstrument, customerPaymentInstrument2));

		repository.findByCustomer(customer)
				.test()
				.assertNoErrors()
				.assertValues(customerPaymentInstrument, customerPaymentInstrument2);
	}

	@Test
	public void findByCustomerReturnsEmptyWhenCustomerHasNoInstruments() {
		when(customerPaymentInstrumentService.findByCustomer(customer)).thenReturn(Collections.emptyList());

		repository.findByCustomer(customer)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void findByGuidReturnsExpectedCustomerPaymentInstrument() {
		when(customerPaymentInstrumentService.findByGuid(CUSTOMER_PAYMENT_INSTRUMENT_ID)).thenReturn(customerPaymentInstrument);

		repository.findByGuid(CUSTOMER_PAYMENT_INSTRUMENT_ID)
				.test()
				.assertNoErrors()
				.assertValue(customerPaymentInstrument);
	}

	@Test
	public void findByGuidReturnsErrorWhenExpectedCustomerPaymentInstrumentDoesNotExist() {
		repository.findByGuid(CUSTOMER_PAYMENT_INSTRUMENT_ID)
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound(
						"Customer payment instrument not found for guid " + CUSTOMER_PAYMENT_INSTRUMENT_ID + "."));
	}

	@Test
	public void removeDelegatesToService() {
		repository.remove(customerPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertNoValues();
		verify(customerPaymentInstrumentService).remove(customerPaymentInstrument);
	}

	@Test
	public void removeThrowsErrorWhenServiceFails() {
		EpServiceException exception = new EpServiceException("Failure");

		doThrow(exception).when(customerPaymentInstrumentService).remove(customerPaymentInstrument);

		repository.remove(customerPaymentInstrument)
				.test()
				.assertNoValues()
				.assertError(exception);
	}

	@Test
	public void saveOrUpdateReturnsExpectedInstrumentOnSuccess() {
		when(customerPaymentInstrumentService.saveOrUpdate(customerPaymentInstrument)).thenReturn(customerPaymentInstrument);

		repository.saveOrUpdate(customerPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertValue(customerPaymentInstrument);
	}

	@Test
	public void saveOrUpdateReturnsErrorOnFailure() {
		repository.saveOrUpdate(customerPaymentInstrument)
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.serverError("Unable to save customer payment instrument."));
	}
}
