/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.money.Money;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodShoppingCartValidatorTest {
	private static final String GUID = "GUID";
	private static final String STORECODE = "STORECODE";

	@InjectMocks
	private PaymentMethodShoppingCartValidatorImpl validator;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Store store;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private Shopper shopper;

	@Mock
	private Customer customer;

	@Mock
	private PricingSnapshotService pricingSnapshotService;

	@Mock
	private ShoppingCartValidationContext context;

	@Mock
	private ShoppingCartPricingSnapshot pricingSnapshot;

	@Mock
	private CartOrderPaymentInstrument cartOrderPaymentInstrument;

	@Mock
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Mock
	private CustomerPaymentInstrument customerPaymentInstrument;

	@Before
	public void setUp() throws Exception {
		given(cartOrder.getGuid()).willReturn(GUID);
		given(pricingSnapshotService.getPricingSnapshotForCart(shoppingCart)).willReturn(pricingSnapshot);
		given(context.getCartOrder()).willReturn(cartOrder);
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(context.getShoppingCart().getStore()).willReturn(store);
		given(store.getCode()).willReturn(STORECODE);
		given(shoppingCart.getShopper()).willReturn(shopper);
		given(shoppingCart.getShopper().getCustomer()).willReturn(customer);
	}

	@Test
	public void paymentMethodRequiredAndCartOrderPaymentInstrumentSpecified() {
		// Given
		given(pricingSnapshot.getSubtotal()).willReturn(BigDecimal.TEN);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderAndStore(
				cartOrder, context.getShoppingCart().getStore().getCode()))
				.willReturn(Collections.singleton(cartOrderPaymentInstrument));
		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void paymentMethodRequiredAndCartOrderPaymentInstrumentAreNotSpecified() {
		StructuredErrorMessage errorMessage = new StructuredErrorMessage(StructuredErrorMessageType.NEEDINFO, "need.payment.method",
				"Payment method must be specified.", Collections.emptyMap(),
				new StructuredErrorResolution(CartOrder.class, cartOrder.getGuid()));
		// Given
		given(pricingSnapshot.getSubtotal()).willReturn(BigDecimal.TEN);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderAndStore(
				cartOrder, context.getShoppingCart().getStore().getCode())).willReturn(Collections.emptyList());
		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void paymentMethodRequiredAndCustomerDefaultPaymentInstrumentSpecified() {
		// Given
		given(pricingSnapshot.getSubtotal()).willReturn(BigDecimal.TEN);
		given(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(
				customer, context.getShoppingCart().getStore().getCode())).willReturn(customerPaymentInstrument);
		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void paymentMethodRequiredAndInstrumentsAreNotSpecified() {
		StructuredErrorMessage errorMessage = new StructuredErrorMessage(StructuredErrorMessageType.NEEDINFO, "need.payment.method",
				"Payment method must be specified.", Collections.emptyMap(),
				new StructuredErrorResolution(CartOrder.class, cartOrder.getGuid()));
		// Given
		given(pricingSnapshot.getSubtotal()).willReturn(BigDecimal.TEN);
		given(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer,
				context.getShoppingCart().getStore().getCode())).willReturn(null);
		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void paymentMethodRequiredButNotSpecified() {
		StructuredErrorMessage errorMessage = new StructuredErrorMessage(StructuredErrorMessageType.NEEDINFO, "need.payment.method",
				"Payment method must be specified.", Collections.emptyMap(),
				new StructuredErrorResolution(CartOrder.class, cartOrder.getGuid()));

		// Given
		given(pricingSnapshot.getSubtotal()).willReturn(BigDecimal.TEN);

		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void paymentMethodNotRequired() {
		// Given
		given(pricingSnapshot.getSubtotal()).willReturn(BigDecimal.ZERO);
		given(pricingSnapshot.getShippingCost()).willReturn(Money.valueOf(BigDecimal.ZERO, Currency.getInstance("USD")));

		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}
}