/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static java.util.Arrays.asList;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.order.OrderMessageIds;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shipping.ShippingServiceLevelService;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;

/**
 * {@link CheckoutAction} to validate the shipping information on a {@link ShoppingCart}.
 */
public class ShippingInformationCheckoutAction implements CheckoutAction {

	private ShippingServiceLevelService shippingServiceLevelService;  
	
	@Override
	public void execute(final CheckoutActionContext context) throws EpSystemException {
		ShoppingCart shoppingCart = context.getShoppingCart();
		
		if (shoppingCart.requiresShipping()) {
			verifyCartHasShippingAddress(shoppingCart);
			verifyCartHasShippingServiceLevel(shoppingCart);
			verifyCartHasValidShippingServiceLevel(shoppingCart);
		}
	}

	private void verifyCartHasShippingAddress(final ShoppingCart shoppingCart) {
		if (shoppingCart.getShippingAddress() == null) {
			String errorMessage = "No shipping address set on shopping cart with guid: " + shoppingCart.getGuid();
			throw new MissingShippingAddressException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.SHIPPING_ADDRESS_MISSING,
									errorMessage,
									null
							)
					)
			);

		}
	}

	private void verifyCartHasShippingServiceLevel(final ShoppingCart shoppingCart) {
		if (shoppingCart.getSelectedShippingServiceLevel() == null) {
			String errorMessage = "No shipping service level set on shopping cart with guid: " + shoppingCart.getGuid();
			throw new MissingShippingServiceLevelException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.SHIPPING_SERVICE_LEVEL_INVALID,
									errorMessage,
									ImmutableMap.of("shipping-level", "")
							)
					)
			);

		}
	}

	private void verifyCartHasValidShippingServiceLevel(final ShoppingCart shoppingCart) {
		ShippingServiceLevel levelFromCart = shoppingCart.getSelectedShippingServiceLevel();
		List<ShippingServiceLevel> validCartLevels = shippingServiceLevelService.retrieveShippingServiceLevel(shoppingCart);
		if (!validCartLevels.contains(levelFromCart)) {
			String errorMessage = "Invalid shipping service level with guid " + levelFromCart.getGuid()
					+ " set on shopping cart with guid: " + shoppingCart.getGuid();
			throw new InvalidShippingServiceLevelException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.SHIPPING_SERVICE_LEVEL_INVALID,
									errorMessage,
									ImmutableMap.of("shipping-level", levelFromCart.getGuid())
							)
					)
			);
		}
	}

	public void setShippingServiceLevelService(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}

	protected ShippingServiceLevelService getShippingServiceLevelService() {
		return shippingServiceLevelService;
	}
}
