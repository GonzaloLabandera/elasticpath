/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CART_ORDER_PAYMENT_INSTRUMENT_ID;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;

/**
 * Tests for {@link CartOrderPaymentInstrumentRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartOrderPaymentInstrumentRepositoryTest {

	@InjectMocks
	private CartOrderPaymentInstrumentRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Mock
	private CartOrder cartOrder;

	private final CartOrderPaymentInstrument cartOrderPaymentInstrument = mock(CartOrderPaymentInstrument.class);

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
	}

	@Test
	public void findByCartOrderReturnsAllPaymentInstrumentsForACartOrder() {
		final CartOrderPaymentInstrument cartOrderPaymentInstrument2 = mock(CartOrderPaymentInstrument.class);

		when(cartOrderPaymentInstrumentService.findByCartOrder(cartOrder))
				.thenReturn(ImmutableList.of(cartOrderPaymentInstrument, cartOrderPaymentInstrument2));

		repository.findByCartOrder(cartOrder)
				.test()
				.assertNoErrors()
				.assertValues(cartOrderPaymentInstrument, cartOrderPaymentInstrument2);
	}

	@Test
	public void findByCartOrderReturnsEmptyWhenNoPaymentInstrumentsExistForACartOrder() {
		when(cartOrderPaymentInstrumentService.findByCartOrder(cartOrder)).thenReturn(Collections.emptyList());

		repository.findByCartOrder(cartOrder)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void findByGuidReturnsExpectedCartOrderPaymentInstrument() {
		when(cartOrderPaymentInstrumentService.findByGuid(CART_ORDER_PAYMENT_INSTRUMENT_ID)).thenReturn(cartOrderPaymentInstrument);

		repository.findByGuid(CART_ORDER_PAYMENT_INSTRUMENT_ID)
				.test()
				.assertNoErrors()
				.assertValue(cartOrderPaymentInstrument);
	}

	@Test
	public void findByGuidReturnsErrorWhenExpectedCartOrderPaymentInstrumentNotFound() {
		repository.findByGuid(CART_ORDER_PAYMENT_INSTRUMENT_ID)
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound(
						"Cart order payment instrument not found for guid " + CART_ORDER_PAYMENT_INSTRUMENT_ID + "."));
	}

	@Test
	public void removeCompletesSuccessfully() {
		repository.remove(cartOrderPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertComplete();
	}

	@Test
	public void removeReturnsErrorOnFailure() {
		EpServiceException exception = new EpServiceException("Failure");

		doThrow(exception).when(cartOrderPaymentInstrumentService).remove(cartOrderPaymentInstrument);

		repository.remove(cartOrderPaymentInstrument)
				.test()
				.assertNotComplete()
				.assertError(exception);
	}

	@Test
	public void saveOrUpdateReturnsSuccessfully() {
		when(cartOrderPaymentInstrumentService.saveOrUpdate(cartOrderPaymentInstrument)).thenReturn(cartOrderPaymentInstrument);

		repository.saveOrUpdate(cartOrderPaymentInstrument)
				.test()
				.assertNoErrors()
				.assertValue(cartOrderPaymentInstrument);
	}

	@Test
	public void saveOrUpdateReturnsErrorOnFailure() {
		EpServiceException exception = new EpServiceException("Failure");

		doThrow(exception).when(cartOrderPaymentInstrumentService).saveOrUpdate(cartOrderPaymentInstrument);

		repository.saveOrUpdate(cartOrderPaymentInstrument)
				.test()
				.assertNotComplete()
				.assertError(ResourceOperationFailure.serverError("Unable to save cart order payment instrument."));
	}
}
