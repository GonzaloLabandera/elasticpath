/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;

@RunWith(MockitoJUnitRunner.class)
public class ShippingOptionShoppingCartValidatorTest {

	private static final String SHOPPING_CART_GUID = "shoppingCartGuid";
	private static final String SHIPPING_OPTION_CODE = "shippingOptionCode";
	private static final String CARRIER_CODE = "carrierCode";
	private static final String SHIPPING_OPTION_FIELD = "shipping-option";
	private static final String OTHER_SHIPPING_OPTION_CODE = "otherShippingOptionCode";
	private static final String STORE_CODE = "testStoreCode";

	@InjectMocks
	private ShippingOptionShoppingCartValidatorImpl validator;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private XPFShoppingCart xpfShoppingCart;

	@Mock
	private XPFShippingOption selectedShippingOption;

	@Mock
	private XPFShippingOption availableShippingOption;

	@Before
	public void setUp() throws Exception {
		when(context.getShoppingCart()).thenReturn(xpfShoppingCart);
		when(xpfShoppingCart.getGuid()).thenReturn(SHOPPING_CART_GUID);
		when(xpfShoppingCart.isRequiresShipping()).thenReturn(true);
		when(xpfShoppingCart.getShopper().getStore().getCode()).thenReturn(STORE_CODE);
		when(selectedShippingOption.getCode()).thenReturn(SHIPPING_OPTION_CODE);
		when(selectedShippingOption.getCarrierCode()).thenReturn(CARRIER_CODE);
		when(availableShippingOption.getCode()).thenReturn(SHIPPING_OPTION_CODE);
		when(availableShippingOption.getCarrierCode()).thenReturn(CARRIER_CODE);
		when(context.getAvailableShippingOptions()).thenReturn(Collections.singleton(availableShippingOption));
		when(xpfShoppingCart.getSelectedShippingOption()).thenReturn(selectedShippingOption);
	}

	/**
	 * Validate the structured error message received when the shopping cart does not have a shipping option.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenNoShippingOptionExists() {
		// Given
		when(xpfShoppingCart.getSelectedShippingOption()).thenReturn(null);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "need.shipping.option",
				"Shipping option must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(ShoppingCart.class, SHOPPING_CART_GUID));
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	/**
	 * Validate the structured error message received when the shopping cart has an invalid shipping option.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenShippingOptionsUnavailable() {
		// Given
		when(context.getAvailableShippingOptions()).thenReturn(null);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR, "shipping.options.unavailable",
				"There was a problem retrieving shipping options from the shipping service", Collections.emptyMap(),
				new XPFStructuredErrorResolution(ShoppingCart.class, SHOPPING_CART_GUID));
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	/**
	 * Validate the structured error message received when the shopping cart has an invalid shipping option.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenShippingOptionIsInvalid() {
		// Given
		when(availableShippingOption.getCode()).thenReturn(OTHER_SHIPPING_OPTION_CODE);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR, "invalid.shipping.option",
				"Selected shipping option is not valid.", ImmutableMap.of(SHIPPING_OPTION_FIELD, SHIPPING_OPTION_CODE),
				new XPFStructuredErrorResolution(ShoppingCart.class, SHOPPING_CART_GUID));
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	/**
	 * Validate cart with valid shipping information.
	 */
	@Test
	public void verifyNoStructuredErrorMessagesWhenCartMeetsCheckoutRequirements() {
		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

}
