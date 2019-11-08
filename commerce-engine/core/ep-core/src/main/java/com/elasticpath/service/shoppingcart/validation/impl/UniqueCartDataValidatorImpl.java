/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidator;

/**
 * Validator to check that the cart modifier data are unique.
 */
public class UniqueCartDataValidatorImpl  implements ShoppingCartValidator  {

	private static final String MESSAGE_ID = "cart.descriptor.not-unique";

	private ShoppingCartService shoppingCartService;
	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingCartValidationContext context) {
		ShoppingCart shoppingCart = context.getShoppingCart();

		Map<String, CartData> newCartData = shoppingCart.getCartData();
		Shopper shopper = shoppingCart.getShopper();


		Customer customer = shopper.getCustomer();
		String customerGuid = customer.getGuid();
		List<String> existingCartGuids = getShoppingCartService().findByCustomerAndStore(customerGuid, shopper.getStoreCode());
		Map<String, List<CartData>> cartDataForCarts = getShoppingCartService().findCartDataForCarts(existingCartGuids);

		for (List<CartData> existingCartData : cartDataForCarts.values()) {
			if (hasSameCartData(existingCartData, newCartData)) {
				StructuredErrorMessage errorMessage = new StructuredErrorMessage(MESSAGE_ID, MESSAGE_ID, Collections.emptyMap());
				return Collections.singletonList(errorMessage);
			}
		}
		return Collections.emptyList();
	}

	private boolean hasSameCartData(final List<CartData> existingCartData, final Map<String, CartData> newCartData) {

		if (existingCartData.isEmpty()) {
			return false;
		}

		for (CartData existingCartDatum : existingCartData) {

			CartData identifierValue = newCartData.get(existingCartDatum.getKey());
			if (identifierValue == null || !existingCartDatum.getValue().equals(identifierValue.getValue())) {
				// different identifiers between existing cart data and new cart data or
				//has same identifier key (name), but different value (cart1, cart2)
				//return false right away, one of the identifiers is different.
				return false;
			}
		}
		return true; // returns true if all identifiers same.
	}

	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}
}
