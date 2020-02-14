/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories;

import io.reactivex.Single;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;

/**
 * Integration Repository for Purchase interactions with CE.
 */
public interface PurchaseRepository {

	/**
	 * Checkout. Successful result does not imply that checkout was successful just that it completed without error.
	 *
	 * @param shoppingCart    Cart to checkout.
	 * @param taxSnapshot     the taxed pricing snapshot of the given shopping cart
	 * @param customerSession the customer session
	 * @return the result of the checkout.
	 */
	Single<CheckoutResults> checkout(ShoppingCart shoppingCart, ShoppingCartTaxSnapshot taxSnapshot, CustomerSession customerSession);

}
