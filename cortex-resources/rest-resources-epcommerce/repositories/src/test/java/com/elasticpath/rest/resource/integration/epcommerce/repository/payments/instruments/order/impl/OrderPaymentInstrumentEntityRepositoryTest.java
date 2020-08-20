/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CART_ORDER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.TEST_MAP;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.createTestPaymentInstrumentDTO;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentIdentifier;

import java.math.BigDecimal;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.money.Money;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;

/**
 * Tests for {@link OrderPaymentInstrumentEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentInstrumentEntityRepositoryTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("MOBEE");
	private static final IdentifierPart<String> CART_ORDER_ID = StringIdentifier.of("CART_ORDER_ID");
	private static final String PAYMENT_INSTRUMENT_ID = "PAYMENT_INSTRUMENT_ID";
	private static final String USER_ID = "SHARED_ID";
	private static final String TEST_NAME = "Test Name";
	private static final Locale LOCALE = Locale.CANADA;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;

	@Mock
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	@Mock
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;

	@Mock
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private MoneyTransformer moneyTransformer;

	@Mock
	private CartOrderPaymentInstrument cartOrderPaymentInstrument;

	@Mock
	private CustomerPaymentInstrument customerPaymentInstrument;

	@Mock
	private Customer customer;

	@InjectMocks
	private OrderPaymentInstrumentEntityRepositoryImpl<OrderPaymentInstrumentEntity, OrderPaymentInstrumentIdentifier> repository;

	private CostEntity costEntity;

	@Before
	public void setUp() {
		costEntity = CostEntity.builder()
				.withAmount(BigDecimal.TEN)
				.withCurrency("CAD")
				.build();

		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE.getValue(), USER_ID, LOCALE, CURRENCY));

		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(customerPaymentInstrumentRepository.findByCustomer(customer)).thenReturn(Observable.just(customerPaymentInstrument));
		when(cartOrderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_ID);
		when(cartOrderPaymentInstrument.getLimitAmount()).thenReturn(BigDecimal.TEN);

		when(cartOrderPaymentInstrumentRepository.findByGuid(CART_ORDER_PAYMENT_INSTRUMENT_ID)).thenReturn(Single.just(cartOrderPaymentInstrument));

		when(paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(PAYMENT_INSTRUMENT_ID))
				.thenReturn(Single.just(createTestPaymentInstrumentDTO()));

		when(customerDefaultPaymentInstrumentRepository.isDefault(customerPaymentInstrument)).thenReturn(Single.just(true));

		when(moneyTransformer.transformToEntity(Money.valueOf(cartOrderPaymentInstrument.getLimitAmount(), CURRENCY), LOCALE)).thenReturn(costEntity);
	}

	private OrderPaymentInstrumentIdentifier createTestOrderPaymentInstrumentIdentifier() {
		return buildOrderPaymentInstrumentIdentifier(SCOPE, CART_ORDER_ID, StringIdentifier.of(CART_ORDER_PAYMENT_INSTRUMENT_ID));
	}

	@Test
	public void findOneReturnsExpectedSavedAndDefaultPaymentInstrument() {
		when(customerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_ID);

		OrderPaymentInstrumentEntity expectedEntity = buildOrderPaymentInstrumentEntity(
				costEntity, true, true, TEST_MAP, TEST_NAME);

		repository.findOne(createTestOrderPaymentInstrumentIdentifier())
				.test()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsExpectedSavedAndNonDefaultPaymentInstrument() {
		when(customerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_ID);
		when(customerDefaultPaymentInstrumentRepository.isDefault(customerPaymentInstrument)).thenReturn(Single.just(false));

		OrderPaymentInstrumentEntity expectedEntity = buildOrderPaymentInstrumentEntity(
				costEntity, false, true, TEST_MAP, TEST_NAME);

		repository.findOne(createTestOrderPaymentInstrumentIdentifier())
				.test()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsExpectedUnsavedPaymentInstrumentWhenDoesNotCorrespondToSavedInstrument() {
		when(customerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn("PAYMENT_INSTRUMENT_ID_2");

		OrderPaymentInstrumentEntity expectedEntity = buildOrderPaymentInstrumentEntity(costEntity, false, false, TEST_MAP, TEST_NAME);

		repository.findOne(createTestOrderPaymentInstrumentIdentifier())
				.test()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsExpectedUnsavedPaymentInstrumentWhenNoSavedInstrumentsExist() {
		when(customerPaymentInstrumentRepository.findByCustomer(customer)).thenReturn(Observable.empty());
		OrderPaymentInstrumentEntity expectedEntity = buildOrderPaymentInstrumentEntity(costEntity, false, false, TEST_MAP, TEST_NAME);

		repository.findOne(createTestOrderPaymentInstrumentIdentifier())
				.test()
				.assertValue(expectedEntity);
	}

	@Test
	public void deleteRemovesBothChosenAndProfilePaymentInstrument() {
		when(customerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_ID);
		when(cartOrderPaymentInstrumentRepository.remove(cartOrderPaymentInstrument)).thenReturn(Completable.complete());
		when(customerPaymentInstrumentRepository.remove(customerPaymentInstrument)).thenReturn(Completable.complete());

		repository.delete(createTestOrderPaymentInstrumentIdentifier())
				.test()
				.assertNoErrors()
				.assertComplete();

		verify(cartOrderPaymentInstrumentRepository).remove(cartOrderPaymentInstrument);
		verify(customerPaymentInstrumentRepository).remove(customerPaymentInstrument);
	}

	@Test
	public void deleteRemovesChosenPaymentInstrumentOnly() {
		when(customerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn("PAYMENT_INSTRUMENT_ID_2");
		when(cartOrderPaymentInstrumentRepository.remove(cartOrderPaymentInstrument)).thenReturn(Completable.complete());

		repository.delete(createTestOrderPaymentInstrumentIdentifier())
				.test()
				.assertNoErrors()
				.assertComplete();

		verify(cartOrderPaymentInstrumentRepository).remove(cartOrderPaymentInstrument);
		verify(customerPaymentInstrumentRepository, never()).remove(any());
	}
}
