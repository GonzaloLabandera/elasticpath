/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CUSTOMER_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CUSTOMER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.ORDER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CART_ORDER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.STORE_PAYMENT_PROVIDER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.createTestPaymentInstrumentDTO;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentMethodIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentMethodIdentifier;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * Tests for {@link PaymentInstrumentToPaymentMethodLinkRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentInstrumentToPaymentMethodLinkRepositoryTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("MOBEE");

	@InjectMocks
	private PaymentInstrumentToPaymentMethodLinkRepositoryImpl repository;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@Mock
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;

	@Mock
	private CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;

	@Mock
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	private final StorePaymentProviderConfig storePaymentProviderConfig = mock(StorePaymentProviderConfig.class);

	@Before
	public void setUp() {
		repository = new PaymentInstrumentToPaymentMethodLinkRepositoryImpl(paymentMethodRepository, customerPaymentInstrumentRepository,
				cartOrderPaymentInstrumentRepository, paymentInstrumentManagementRepository);

		CartOrderPaymentInstrument cartOrderPaymentInstrument = mock(CartOrderPaymentInstrument.class);
		when(cartOrderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_ID);

		CustomerPaymentInstrument customerPaymentInstrument = mock(CustomerPaymentInstrument.class);
		when(customerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_ID);

		when(storePaymentProviderConfig.getGuid()).thenReturn(STORE_PAYMENT_PROVIDER_ID);
		when(paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(SCOPE.getValue()))
				.thenReturn(Observable.just(storePaymentProviderConfig));
		when(paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(PAYMENT_INSTRUMENT_ID))
				.thenReturn(Single.just(createTestPaymentInstrumentDTO()));
		when(cartOrderPaymentInstrumentRepository.findByGuid(CART_ORDER_PAYMENT_INSTRUMENT_ID)).thenReturn(Single.just(cartOrderPaymentInstrument));
		when(customerPaymentInstrumentRepository.findByGuid(CUSTOMER_PAYMENT_INSTRUMENT_ID)).thenReturn(Single.just(customerPaymentInstrument));
		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(PAYMENT_PROVIDER_ID);
	}

	@Test
	public void getOrderPaymentMethodIdentifierReturnsEmptyWhenNoMatchingStoreConfig() {
		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(PAYMENT_PROVIDER_CONFIG_ID);
		repository.getOrderPaymentMethodIdentifier(buildOrderPaymentInstrumentIdentifier(SCOPE, StringIdentifier.of(ORDER_ID),
				StringIdentifier.of(CART_ORDER_PAYMENT_INSTRUMENT_ID)))
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void getOrderPaymentMethodIdentifierReturnsExpectedValueWhenMatchingStoreConfigExists() {
		repository.getOrderPaymentMethodIdentifier(buildOrderPaymentInstrumentIdentifier(SCOPE, StringIdentifier.of(ORDER_ID),
				StringIdentifier.of(CART_ORDER_PAYMENT_INSTRUMENT_ID)))
				.test()
				.assertNoErrors()
				.assertValue(buildOrderPaymentMethodIdentifier(SCOPE, StringIdentifier.of(STORE_PAYMENT_PROVIDER_ID),
						StringIdentifier.of(ORDER_ID)));
	}

	@Test
	public void getProfilePaymentMethodIdentifierReturnsEmptyWhenNoMatchingStoreConfig() {
		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(PAYMENT_PROVIDER_CONFIG_ID);
		PaymentInstrumentIdentifier paymentInstrumentIdentifier =
				buildPaymentInstrumentIdentifier(SCOPE, StringIdentifier.of(CUSTOMER_PAYMENT_INSTRUMENT_ID));

		repository.getProfilePaymentMethodIdentifier(CUSTOMER_ID, paymentInstrumentIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void getProfilePaymentMethodIdentifierReturnsExpectedValueWhenMatchingStoreConfigExists() {
		PaymentInstrumentIdentifier paymentInstrumentIdentifier =
				buildPaymentInstrumentIdentifier(SCOPE, StringIdentifier.of(CUSTOMER_PAYMENT_INSTRUMENT_ID));
		ProfilePaymentMethodIdentifier profilePaymentMethodIdentifier = buildProfilePaymentMethodIdentifier(
				StringIdentifier.of(CUSTOMER_ID), SCOPE, StringIdentifier.of(STORE_PAYMENT_PROVIDER_ID));

		repository.getProfilePaymentMethodIdentifier(CUSTOMER_ID,  paymentInstrumentIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(profilePaymentMethodIdentifier);
	}
}
