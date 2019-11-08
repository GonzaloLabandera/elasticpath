/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation;

import com.elasticpath.domain.shoppingcart.ShoppingCart;

/**
 * Service for validating creating a cart.
 */
public interface CreateShoppingCartValidationService extends Validator<ShoppingCartValidationContext> {

	/**
	 * Builds validation context.
	 *
	 * @param shoppingCart         the shopping cart
	 * @return the shopping item validation context
	 */
	ShoppingCartValidationContext buildContext(ShoppingCart shoppingCart);

}
