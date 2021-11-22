/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;

/**
 * Builder for {@code com.elasticpath.xpf.connectivity.context.ShoppingCartValidationContext}.
 */
public interface ShoppingCartValidationContextBuilder {

	/**
	 * Builds {@code com.elasticpath.xpf.connectivity.context.ShoppingCartValidationContext} using inputs provide.
	 *
	 * @param shoppingCart the shopping cart
	 *
	 * @return ShoppingCartValidationContext built using the inputs provided.
	 */
	XPFShoppingCartValidationContext build(ShoppingCart shoppingCart);
}
