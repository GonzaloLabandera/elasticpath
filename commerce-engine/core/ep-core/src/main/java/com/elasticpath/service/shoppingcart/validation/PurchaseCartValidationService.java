/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation;

import java.util.Collection;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;

/**
 * Service for validating a shopping cart before a purchase.
 */
public interface PurchaseCartValidationService {

	/**
	 * Execute the validators for a shopping cart and return any structured error messages.
	 * @param shoppingCart the shopping cart
	 * @param shopper the shopper
	 * @param store the store
	 * @return the structured error messages
	 */
	Collection<StructuredErrorMessage> validate(ShoppingCart shoppingCart, Shopper shopper, Store store);
}
