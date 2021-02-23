/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

import java.util.List;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;

/**
 * Defining action context for shopping cart checkout.
 */
public interface PreCaptureCheckoutActionContext extends CheckoutActionContext {

	/**
	 * Gets the {@link Shopper}.
	 *
	 * @return the {@link Shopper}
	 */
	Shopper getShopper();

	/**
	 * Gets the {@link ShoppingCart}.
	 *
	 * @return the {@link ShoppingCart}
	 */
	ShoppingCart getShoppingCart();

	/**
	 * Returns the Customer Session.
	 *
	 * @return the Customer Session
	 */
	CustomerSession getCustomerSession();

	/**
	 * Returns the Shopping Cart Tax Snapshot.
	 *
	 * @return the Shopping Cart Tax Snapshot
	 */
	ShoppingCartTaxSnapshot getShoppingCartTaxSnapshot();
	
	/**
	 * Gets cart order for this checkout action context.
	 *
	 * @return cart order for this checkout action context.
	 */
	CartOrder getCartOrder();

	/**
	 * Returns the list of potential order holds for this checkout context.
	 *
	 * @return the list of potential order holds for this checkout context
	 */
	List<OrderHold> getOrderHolds();

	/**
	 * Add a potential order hold to this context for later evaluation by an {@link OrderHoldStrategy}.
	 *
	 * @param orderHold - the potential order hold to add to this context for later evaluation
	 */
	void addOrderHold(OrderHold orderHold);
}
