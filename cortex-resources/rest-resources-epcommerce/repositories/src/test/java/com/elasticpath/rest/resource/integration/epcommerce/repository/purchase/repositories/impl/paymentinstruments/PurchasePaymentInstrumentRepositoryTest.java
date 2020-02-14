/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.paymentinstruments;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentAttributesEntity;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.OrderPaymentInstrumentImpl;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;

/**
 * Tests for {@link PurchasePaymentInstrumentRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchasePaymentInstrumentRepositoryTest {

	private static final String SCOPE = "SCOPE";
	private static final String PAYMENT_INSTRUMENT_ID_1 = "PAYMENT_INSTRUMENT_ID_1";
	private static final String PAYMENT_INSTRUMENT_ID_2 = "PAYMENT_INSTRUMENT_ID_2";
	private static final String ORDER_PAYMENT_INSTRUMENT_ID_1 = "ORDER_PAYMENT_INSTRUMENT_ID_1";
	private static final String ORDER_PAYMENT_INSTRUMENT_ID_2 = "ORDER_PAYMENT_INSTRUMENT_ID_2";
	private static final String PURCHASE_NUMBER = "PURCHASE_NUMBER";
	private static final String INSTRUMENT_NAME = "INSTRUMENT_NAME";

	@InjectMocks
	private PurchasePaymentInstrumentRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Mock
	private PaymentInstrumentDTO paymentInstrumentDTO;

	@Mock
	private Order order;

	private PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		purchasePaymentInstrumentIdentifier = buildPurchasePaymentInstrumentIdentifier(SCOPE, PAYMENT_INSTRUMENT_ID_1, PURCHASE_NUMBER);

		OrderPaymentInstrument orderPaymentInstrument1 = createOrderPaymentInstrument(order, PAYMENT_INSTRUMENT_ID_1, ORDER_PAYMENT_INSTRUMENT_ID_1);
		OrderPaymentInstrument orderPaymentInstrument2 = createOrderPaymentInstrument(order, PAYMENT_INSTRUMENT_ID_2, ORDER_PAYMENT_INSTRUMENT_ID_2);

        when(paymentInstrumentDTO.getName()).thenReturn(INSTRUMENT_NAME);
        when(paymentInstrumentDTO.getData()).thenReturn(Collections.emptyMap());

        when(orderPaymentInstrumentService.findByGuid(PAYMENT_INSTRUMENT_ID_1)).thenReturn(orderPaymentInstrument1);
        when(orderPaymentInstrumentService.findByOrder(order)).thenReturn(ImmutableList.of(orderPaymentInstrument1, orderPaymentInstrument2));

        when(paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(PAYMENT_INSTRUMENT_ID_1))
                .thenReturn(Single.just(paymentInstrumentDTO));

        when(orderRepository.findByGuid(SCOPE, PURCHASE_NUMBER)).thenReturn(Single.just(order));
    }

	@Test
	public void getPaymentInstrumentByGuidReturnsExpectedPaymentInstrumentDTO() {
		PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier = buildPurchasePaymentInstrumentIdentifier(SCOPE,
				PAYMENT_INSTRUMENT_ID_1, PURCHASE_NUMBER);

		repository.findOne(purchasePaymentInstrumentIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(buildTestPurchasePaymentInstrumentEntity());
	}

	@Test
	public void getPaymentInstrumentByGuidReturnsFailureWhenNoCorrespondingPurchaseInstrumentFound() {
		when(orderPaymentInstrumentService.findByGuid(PAYMENT_INSTRUMENT_ID_1)).thenReturn(null);

		repository.findOne(purchasePaymentInstrumentIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound("No purchase payment instruments found for GUID " + PAYMENT_INSTRUMENT_ID_1 + "."));
	}

	@Test
	public void getPaymentInstrumentByGuidReturnsFailureWhenNoCorrespondingCorePaymentInstrumentFound() {
		when(paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(PAYMENT_INSTRUMENT_ID_1))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		repository.findOne(purchasePaymentInstrumentIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

	@Test
	public void findPurchaseInstrumentsByPurchaseIdReturnsExpectedInstruments() {
		PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier1 = buildPurchasePaymentInstrumentIdentifier(SCOPE,
				ORDER_PAYMENT_INSTRUMENT_ID_1, PURCHASE_NUMBER);

		PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier2 = buildPurchasePaymentInstrumentIdentifier(SCOPE,
				ORDER_PAYMENT_INSTRUMENT_ID_2, PURCHASE_NUMBER);

		repository.findPurchaseInstrumentsByPurchaseId(SCOPE, buildPurchasePaymentInstrumentsIdentifier(SCOPE, PURCHASE_NUMBER))
				.test()
				.assertNoErrors()
				.assertValues(purchasePaymentInstrumentIdentifier1, purchasePaymentInstrumentIdentifier2);
	}

	@Test
	public void findPurchaseInstrumentsByPurchaseIdReturnsEmptyWhenNoPurchaseInstrumentsExist() {

		when(orderPaymentInstrumentService.findByOrder(order)).thenReturn(Collections.emptyList());

		repository.findPurchaseInstrumentsByPurchaseId(SCOPE, buildPurchasePaymentInstrumentsIdentifier(SCOPE, PURCHASE_NUMBER))
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	private OrderPaymentInstrument createOrderPaymentInstrument(final Order linkedOrder, final String paymentInstrumentId,
																final String orderPaymentInstrumentId) {
        OrderPaymentInstrument orderPaymentInstrument = new OrderPaymentInstrumentImpl();

        orderPaymentInstrument.setOrderNumber(linkedOrder.getOrderNumber());
        orderPaymentInstrument.setPaymentInstrumentGuid(paymentInstrumentId);
        orderPaymentInstrument.setGuid(orderPaymentInstrumentId);

        return orderPaymentInstrument;
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
				.withPurchase(buildPurchaseIdentifier(purchaseNumber, StringIdentifier.of(scope)))
				.build();
	}

	private PurchaseIdentifier buildPurchaseIdentifier(final String purchaseNumber, final IdentifierPart<String> scope) {
		return PurchaseIdentifier.builder()
				.withPurchases(PurchasesIdentifier.builder()
						.withScope(scope)
						.build())
				.withPurchaseId(StringIdentifier.of(purchaseNumber))
				.build();
	}

	private PurchasePaymentInstrumentEntity buildTestPurchasePaymentInstrumentEntity() {
		return PurchasePaymentInstrumentEntity.builder()
				.withName(INSTRUMENT_NAME)
				.withPaymentInstrumentIdentificationAttributes(buildPaymentInstrumentAttributesEntity(Collections.emptyMap()))
				.build();
	}
}
