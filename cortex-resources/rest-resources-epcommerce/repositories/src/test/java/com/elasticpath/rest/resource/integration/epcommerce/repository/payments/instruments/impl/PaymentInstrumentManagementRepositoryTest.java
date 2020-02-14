/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.ORDER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;

/**
 * Tests for {@link PaymentInstrumentManagementRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentInstrumentManagementRepositoryTest {

	@InjectMocks
	private PaymentInstrumentManagementRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private PaymentInstrumentManagementService paymentInstrumentManagementService;

	@Mock
	private PaymentInstrumentDTO paymentInstrumentDTO;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		when(paymentInstrumentManagementService.getPaymentInstrument(PAYMENT_INSTRUMENT_ID)).thenReturn(paymentInstrumentDTO);
		when(paymentInstrumentManagementService.findByOrderPaymentInstrumentGuid(ORDER_PAYMENT_INSTRUMENT_ID)).thenReturn(paymentInstrumentDTO);
	}

	@Test
	public void getPaymentInstrumentByGuidReturnsExpectedPaymentInstrumentDTO() {
		repository.getPaymentInstrumentByGuid(PAYMENT_INSTRUMENT_ID)
				.test()
				.assertNoErrors()
				.assertValue(paymentInstrumentDTO);
	}

	@Test
	public void getPaymentInstrumentByGuidReturnsFailureWhenNoSuchInstrumentExists() {
		when(paymentInstrumentManagementService.getPaymentInstrument(PAYMENT_INSTRUMENT_ID)).thenReturn(null);
		repository.getPaymentInstrumentByGuid(PAYMENT_INSTRUMENT_ID)
				.test()
				.assertError(ResourceOperationFailure.notFound("No payment instrument found for guid " + PAYMENT_INSTRUMENT_ID + "."));
	}

	@Test
	public void getPaymentInstrumentByOrderPaymentInstrumentGuidReturnsExpectedPaymentInstrumentDTO() {
		repository.getPaymentInstrumentByOrderPaymentInstrumentGuid(ORDER_PAYMENT_INSTRUMENT_ID)
				.test()
				.assertNoErrors()
				.assertValue(paymentInstrumentDTO);
	}

	@Test
	public void getPaymentInstrumentByOrderPaymentInstrumentGuidReturnsFailureWhenNoSuchInstrumentExists() {
		when(paymentInstrumentManagementService.findByOrderPaymentInstrumentGuid(ORDER_PAYMENT_INSTRUMENT_ID)).thenReturn(null);
		repository.getPaymentInstrumentByOrderPaymentInstrumentGuid(ORDER_PAYMENT_INSTRUMENT_ID)
				.test()
				.assertError(ResourceOperationFailure.notFound("No payment instrument found for order payment instrument guid "
						+ ORDER_PAYMENT_INSTRUMENT_ID + "."));
	}
}
