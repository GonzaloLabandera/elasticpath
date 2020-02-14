/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CART_ORDER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl.PaymentInstrumentRepositoryImpl.PAYMENT_METHOD_IS_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentAttributesEntity;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
public class OrderPaymentInstrumentRepositoryTest extends AbstractPaymentInstrumentRepositoryTest {

	private static final long ORDER_UID = 404L;

	@Before
	public void orderPaymentInstrumentSetUp() {
		when(cartOrderPaymentInstrument.getGuid()).thenReturn(CART_ORDER_PAYMENT_INSTRUMENT_ID);
		when(beanFactory.getPrototypeBean(CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class)).thenReturn(cartOrderPaymentInstrument);
		when(cartOrderRepository.findByGuid(SCOPE.getValue(), CART_ORDER_ID.getValue())).thenReturn(Single.just(cartOrder));
		when(cartOrderPaymentInstrumentRepository.saveOrUpdate(cartOrderPaymentInstrument)).thenReturn(Single.just(cartOrderPaymentInstrument));
		when(cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder)).thenReturn(Observable.empty());
	}

	@Test
	public void submitOrderPaymentInstrumentReturnsAppropriateSubmitResultAndCreatesCartOrderPaymentInstrument() {
		when(cartOrder.getUidPk()).thenReturn(ORDER_UID);

        repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(false, false))
                .test()
                .assertNoErrors()
                .assertValue(getExpectedOrderPISubmitResult());

		verify(cartOrderPaymentInstrument).setCartOrderUid(ORDER_UID);
		verify(cartOrderPaymentInstrument).setLimitAmount(BigDecimal.TEN);
        verify(cartOrderPaymentInstrument).setPaymentInstrumentGuid(PAYMENT_INSTRUMENT_ID);
        verify(cartOrderPaymentInstrumentRepository).saveOrUpdate(cartOrderPaymentInstrument);
        verify(customerPaymentInstrumentRepository, never()).saveOrUpdate(any());
        verify(customerDefaultPaymentInstrumentRepository, never()).saveAsDefault(any());
    }

	@Test
	public void submitOrderPaymentInstrumentCreatesCartOrderPaymentInstrumentAndCustomerPaymentInstrument() {
		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(true, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());

		verify(cartOrderPaymentInstrumentRepository).saveOrUpdate(cartOrderPaymentInstrument);
		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository, never()).saveAsDefault(any());
	}

	@Test
	public void submitOrderPaymentInstrumentCreatesCartOrderPaymentInstrumentAndCustomerDefaultPaymentInstrumentIfThereIsNone() {
		when(customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(customer)).thenReturn(Single.just(false));

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(true, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());

		verify(cartOrderPaymentInstrumentRepository).saveOrUpdate(cartOrderPaymentInstrument);
		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test
	public void submitOrderPaymentInstrumentOverwritesCustomerPaymentInstrumentAsDefault() {
		lenient().when(customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(customer)).thenReturn(Single.just(false));

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(true, true))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());

		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test
	public void submitOrderPaymentInstrumentSavesCustomerPaymentInstrumentAsDefaultRegardlessOfSaveOnProfileFlagState() {
		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(false, true))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());

		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test
	public void submitOrderPaymentInstrumentThrowsExceptionWhenAttemptingToSaveInstrumentToProfileForAnonymousCustomer() {
		when(customer.isAnonymous()).thenReturn(true);

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(true, false))
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound(
						"Customer with id " + CUSTOMER_ID.getValue() + " is anonymous and cannot save instruments to profile"));

		verify(customerPaymentInstrumentRepository, never()).saveOrUpdate(any());
	}

	@Test
	public void submitOrderPaymentInstrumentThrowsExceptionWhenAttemptingToSaveInstrumentAsDefaultForAnonymousCustomer() {
		when(customer.isAnonymous()).thenReturn(true);

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(false, true))
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound(
						"Customer with id " + CUSTOMER_ID.getValue() + " is anonymous and cannot save instruments to profile"));

		verify(customerPaymentInstrumentRepository, never()).saveOrUpdate(any());
		verify(customerDefaultPaymentInstrumentRepository, never()).saveAsDefault(any());
	}

	@Test
	public void submitOrderPaymentInstrumentReturnsPaymentMethodNotFoundError() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(false, false))
				.test()
				.assertError(ResourceOperationFailure.notFound(PAYMENT_METHOD_IS_NOT_FOUND));
	}

	@Test
	public void submitOrderPaymentInstrumentHandlesPaymentsExceptionCases() {
		when(orderPaymentApiRepository.createPI(any(), any(), any())).thenReturn(Single.error(ResourceOperationFailure.notFound()));

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(false, false))
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound());

		verify(cartOrderPaymentInstrumentRepository, never()).remove(any());

	}

	@Test
	public void submitOrderPaymentInstrumentNullDataReturnsAppropriateSubmitResult() {
		repository.submitOrderPaymentInstrument(SCOPE, OrderPaymentInstrumentForFormEntity.builder().build())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());
	}

	@Test
	public void submitOrderPaymentInstrumentEmptyDataReturnsAppropriateSubmitResult() {
		repository.submitOrderPaymentInstrument(SCOPE,
				OrderPaymentInstrumentForFormEntity.builder()
						.withPaymentInstrumentIdentificationForm(PaymentInstrumentAttributesEntity.builder().build())
						.build())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());
	}

	@Test
	public void submitOrderPaymentInstrumentRemovesExistingInstrumentOnOrder() {
		when(cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder)).thenReturn(Observable.just(cartOrderPaymentInstrument));
		when(cartOrderPaymentInstrumentRepository.remove(cartOrderPaymentInstrument)).thenReturn(Completable.complete());

		repository.submitOrderPaymentInstrument(SCOPE, createOrderPIFormEntity(true, true))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedOrderPISubmitResult());

		verify(cartOrderPaymentInstrumentRepository).saveOrUpdate(cartOrderPaymentInstrument);
		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(cartOrderPaymentInstrumentRepository).remove(cartOrderPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(any());
	}
}
