/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.order.OrderMessageIds;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shipping.ShippingServiceLevelService;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;

/**
 * Tests the {@link ShippingInformationCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShippingInformationCheckoutActionTest {

	private static final String SHIPPING_SERVICE_LEVEL_GUID = "shippingServiceLevelGuid";
	private static final String CART_GUID = "cartGuid";
	private static final String MESSAGE_FIELD = "structuredErrorMessages";
	private static final String SHIPPING_LEVEL = "shipping-level";

	@InjectMocks
	private ShippingInformationCheckoutAction shippingInformationCheckoutAction;
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private CheckoutActionContext checkoutActionContext;
	@Mock
	private ShippingServiceLevelService shippingServiceLevelService;
	@Mock
	private ShippingServiceLevel shippingServiceLevel;
	@Mock
	private Address shippingAddress;

	/**
	 * Initialize mock objects.
	 */
	@Before
	public void setUp() {
		when(checkoutActionContext.getShoppingCart()).thenReturn(shoppingCart);
		when(shoppingCart.requiresShipping()).thenReturn(true);
		when(shoppingCart.getGuid()).thenReturn(CART_GUID);
		when(shippingServiceLevel.getGuid()).thenReturn(SHIPPING_SERVICE_LEVEL_GUID);
	}

	/**
	 * Validate the structured error message received when the shopping cart has no shipping address.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenCheckingOutWithNoShippingAddress() {

		String errorMessage = "No shipping address set on shopping cart with guid: " + CART_GUID;

		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.SHIPPING_ADDRESS_MISSING,
				errorMessage, null);

		assertThatThrownBy(() -> shippingInformationCheckoutAction.execute(checkoutActionContext))
				.isInstanceOf(MissingShippingAddressException.class)
				.hasMessage(errorMessage)
				.hasFieldOrPropertyWithValue(MESSAGE_FIELD, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Validate the structured error message received when the shopping cart does not have a shipping service level.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenNoShippingServiceLevelExists() {

		when(shoppingCart.getShippingAddress()).thenReturn(shippingAddress);

		String errorMessage = "No shipping service level set on shopping cart with guid: " + CART_GUID;

		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.SHIPPING_SERVICE_LEVEL_INVALID,
				errorMessage, ImmutableMap.of(SHIPPING_LEVEL, ""));

		assertThatThrownBy(() -> shippingInformationCheckoutAction.execute(checkoutActionContext))
				.isInstanceOf(MissingShippingServiceLevelException.class)
				.hasMessage(errorMessage)
				.hasFieldOrPropertyWithValue(MESSAGE_FIELD, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Validate the structured error message received when the shopping cart has an invalid shipping service level.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenShippingServiceLevelIsInvalid() {

		when(shoppingCart.getShippingAddress()).thenReturn(shippingAddress);
		when(shoppingCart.getSelectedShippingServiceLevel()).thenReturn(shippingServiceLevel);

		String errorMessage = "Invalid shipping service level with guid " + SHIPPING_SERVICE_LEVEL_GUID
				+ " set on shopping cart with guid: " + CART_GUID;

		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.SHIPPING_SERVICE_LEVEL_INVALID,
				errorMessage, ImmutableMap.of(SHIPPING_LEVEL, SHIPPING_SERVICE_LEVEL_GUID));

		assertThatThrownBy(() -> shippingInformationCheckoutAction.execute(checkoutActionContext))
				.isInstanceOf(InvalidShippingServiceLevelException.class)
				.hasMessage(errorMessage)
				.hasFieldOrPropertyWithValue(MESSAGE_FIELD, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Validate cart with valid shipping information.
	 */
	@Test()
	public void verifyNoStructuredErrorMessagesWhenCartMeetsCheckoutRequirements() {

		when(shoppingCart.getShippingAddress()).thenReturn(shippingAddress);
		when(shoppingCart.getSelectedShippingServiceLevel()).thenReturn(shippingServiceLevel);
		when(shippingServiceLevelService.retrieveShippingServiceLevel(shoppingCart)).thenReturn(Collections.singletonList(shippingServiceLevel));

		shippingInformationCheckoutAction.execute(checkoutActionContext);

		verify(shoppingCart).getShippingAddress();
		verify(shoppingCart, times(2)).getSelectedShippingServiceLevel();
		verify(shippingServiceLevelService).retrieveShippingServiceLevel(shoppingCart);
	}


}
 