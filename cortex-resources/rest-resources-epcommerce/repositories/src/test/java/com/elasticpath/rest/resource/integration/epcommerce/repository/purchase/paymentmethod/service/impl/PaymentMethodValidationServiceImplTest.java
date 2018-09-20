/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.paymentmethod.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentmethodInfoIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.TotalsCalculator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;


/**
 * The test of {@link PaymentMethodValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodValidationServiceImplTest {

	private static final String ORDER_ID = "orderid";
	private static final String SCOPE = "mobee";
	private static final String MESSAGE_NEED_PAYMENT_METHOD = "A payment method must be provided before you can complete the purchase.";

	@Mock
	private TotalsCalculator totalsCalculator;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private CartOrder cartOrder;

	@InjectMocks
	private PaymentMethodValidationServiceImpl validationService;

	@Before
	public void setup() {

		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
	}

	@Test
	public void validationSucceedsWhenPaymentMethodExists() {
		when(totalsCalculator.calculateSubTotalForCartOrderSingle(SCOPE, ORDER_ID))
				.thenReturn(Single.just(Money.valueOf(BigDecimal.TEN, Currency.getInstance(Locale.CANADA))));

		when(cartOrder.getPaymentMethod()).thenReturn(mock(PaymentMethod.class));

		validationService.validatePaymentForOrder(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

	@Test
	public void validationFailsWhenPriceIsPositiveAndNoPaymentMethodExists() {
		when(totalsCalculator.calculateSubTotalForCartOrderSingle(SCOPE, ORDER_ID))
				.thenReturn(Single.just(Money.valueOf(BigDecimal.TEN, Currency.getInstance(Locale.CANADA))));

		when(cartOrder.getPaymentMethod()).thenReturn(null);

		PaymentmethodInfoIdentifier paymentInfoIdentifier = PaymentmethodInfoIdentifier.builder().withOrder(getOrderIdentifier()).build();

		validationService.validatePaymentForOrder(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(linkedMessage -> linkedMessage.getType().equals(StructuredMessageTypes.NEEDINFO))
				.assertValue(linkedMessage -> linkedMessage.getId().equals(StructuredErrorMessageIdConstants.NEED_PAYMENT_METHOD))
				.assertValue(linkedMessage -> linkedMessage.getDebugMessage().equals(MESSAGE_NEED_PAYMENT_METHOD))
				.assertValue(linkedMessage -> linkedMessage.getLinkedIdentifier().equals(Optional.of(paymentInfoIdentifier)));
	}

	@Test
	public void validationSucceedsWhenPriceIsZeroAndNoPaymentMethodExists() {
		when(totalsCalculator.calculateSubTotalForCartOrderSingle(SCOPE, ORDER_ID))
				.thenReturn(Single.just(Money.valueOf(BigDecimal.ZERO, Currency.getInstance(Locale.CANADA))));

		when(cartOrder.getPaymentMethod()).thenReturn(null);

		validationService.validatePaymentForOrder(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

	private OrderIdentifier getOrderIdentifier() {
		return OrderIdentifier.builder()
				.withOrderId(StringIdentifier.of(ORDER_ID))
				.withScope(StringIdentifier.of(SCOPE))
				.build();
	}
}
