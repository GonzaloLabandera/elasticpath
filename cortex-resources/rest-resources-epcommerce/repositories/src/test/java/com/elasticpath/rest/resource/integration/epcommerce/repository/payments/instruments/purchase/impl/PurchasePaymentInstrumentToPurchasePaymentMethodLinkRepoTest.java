/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.purchase.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PURCHASE_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.STORE_PAYMENT_PROVIDER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPurchasePaymentMethodIdentifier;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * Tests for {@link PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepoImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepoTest {

	private static final String SCOPE = "MOBEE";
	private static final String INCORRECT_PROVIDER = "incorrect-provider";

	@InjectMocks
	private PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepoImpl repository;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@Mock
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	private final StorePaymentProviderConfig storePaymentProviderConfig = mock(StorePaymentProviderConfig.class);

	@Before
	public void setUp() {
		repository = new PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepoImpl(paymentMethodRepository,
				paymentInstrumentManagementRepository);

		PaymentInstrumentDTO paymentInstrumentDTO = mock(PaymentInstrumentDTO.class);
		when(paymentInstrumentDTO.getPaymentProviderConfigurationGuid()).thenReturn(PAYMENT_PROVIDER_CONFIG_ID);

		when(paymentInstrumentManagementRepository.getPaymentInstrumentByOrderPaymentInstrumentGuid(PAYMENT_INSTRUMENT_ID))
				.thenReturn(Single.just(paymentInstrumentDTO));
		when(storePaymentProviderConfig.getGuid()).thenReturn(STORE_PAYMENT_PROVIDER_ID);
		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(PAYMENT_PROVIDER_CONFIG_ID);
		when(paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(SCOPE))
				.thenReturn(Observable.just(storePaymentProviderConfig));
	}

	@Test
	public void getOrderPaymentMethodIdentifierReturnsEmptyWhenNoMatchingStoreConfig() {
		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(INCORRECT_PROVIDER);

		PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier = buildPurchasePaymentInstrumentIdentifier(SCOPE,
				PAYMENT_INSTRUMENT_ID, PURCHASE_ID);
		repository.getPurchasePaymentMethodIdentifier(purchasePaymentInstrumentIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void getPurchasePaymentMethodIdentifierReturnsExpectedValueWhenMatchingStoreConfigExists() {
		PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier = buildPurchasePaymentInstrumentIdentifier(SCOPE,
				PAYMENT_INSTRUMENT_ID, PURCHASE_ID);
		PurchasePaymentMethodIdentifier purchasePaymentMethodIdentifier = buildPurchasePaymentMethodIdentifier(
				purchasePaymentInstrumentIdentifier,
				StringIdentifier.of(STORE_PAYMENT_PROVIDER_ID));

		repository.getPurchasePaymentMethodIdentifier(purchasePaymentInstrumentIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(purchasePaymentMethodIdentifier);
	}

	private PurchasePaymentInstrumentIdentifier buildPurchasePaymentInstrumentIdentifier(final String scope, final String paymentInstrumentId,
																						 final String purchaseNumber) {
		buildPurchasePaymentInstrumentsIdentifier(scope, purchaseNumber);

		return PurchasePaymentInstrumentIdentifier.builder()
				.withPurchasePaymentInstruments(buildPurchasePaymentInstrumentsIdentifier(scope, purchaseNumber))
				.withPaymentInstrumentId(StringIdentifier.of(paymentInstrumentId))
				.build();
	}

	private PurchasePaymentInstrumentsIdentifier buildPurchasePaymentInstrumentsIdentifier(final String scope, final String purchaseNumber) {
		return PurchasePaymentInstrumentsIdentifier.builder()
				.withPurchase(buildPurchaseIdentifier(purchaseNumber, scope))
				.build();
	}

	private PurchaseIdentifier buildPurchaseIdentifier(final String purchaseNumber, final String scope) {
		return PurchaseIdentifier.builder()
				.withPurchases(PurchasesIdentifier.builder()
						.withScope(StringIdentifier.of(scope))
						.build())
				.withPurchaseId(StringIdentifier.of(purchaseNumber))
				.build();
	}
}
