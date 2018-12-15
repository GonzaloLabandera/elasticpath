/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;

/**
 * Service for validating deleting item from the cart.
 */
public interface RemoveShoppingItemFromCartValidationService extends Validator<ShoppingItemValidationContext> {

	/**
	 * Builds validation context.
	 *
	 * @param shoppingCart         the shopping cart
	 * @param shoppingItem         the shopping item
	 * @return the shopping item validation context
	 */
	ShoppingItemValidationContext buildContext(ShoppingCart shoppingCart, ShoppingItem shoppingItem);

}
