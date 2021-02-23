/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.shoppingcart.actions.impl.CheckoutActionContextImpl;

/**
 * Implements {@link PreCaptureCheckoutActionContext}.
 */
public class PreCaptureCheckoutActionContextImpl extends CheckoutActionContextImpl
		implements PreCaptureCheckoutActionContext {

	private final ShoppingCartTaxSnapshot shoppingCartTaxSnapshot;
	private final ShoppingCart shoppingCart;
	private final CustomerSession customerSession;
	private final List<OrderHold> orderHolds = new LinkedList<>();
	private final BiFunction<Shopper, Order, CartOrder> cartOrderExtractor;

	/**
	 * Constructor for creating the initial CheckoutActionContext object.
	 *
	 * @param shoppingCart            the shopping cart
	 * @param shoppingCartTaxSnapshot the shopping cart tax pricing snapshot
	 * @param customerSession         the customer session
	 * @param isOrderExchange         indicates whether or not the cart is for an order exchange
	 * @param awaitExchangeCompletion indicates whether or not the cart should wait for exchange completion
	 * @param exchange                the orderReturn object which is used for exchanges
	 * @param cartOrderExtractor      the function to extract CartOrder for defined shopper and order
	 */
	public PreCaptureCheckoutActionContextImpl(final ShoppingCart shoppingCart,
											   final ShoppingCartTaxSnapshot shoppingCartTaxSnapshot,
											   final CustomerSession customerSession,
											   final boolean isOrderExchange,
											   final boolean awaitExchangeCompletion,
											   final OrderReturn exchange,
											   final BiFunction<Shopper, Order, CartOrder> cartOrderExtractor
	) {
		super(isOrderExchange, awaitExchangeCompletion, exchange);
		this.shoppingCartTaxSnapshot = shoppingCartTaxSnapshot;
		this.shoppingCart = shoppingCart;
		this.customerSession = customerSession;
		this.cartOrderExtractor = cartOrderExtractor;
	}

	/**
	 * Constructor for creating a PreCaptureCheckoutActionContext with a single order hold.
	 *
	 * @param orderHold   the order hold to store in the context.
	 */
	public PreCaptureCheckoutActionContextImpl(final OrderHold orderHold) {
		this(null, null, null, false, false, null, null);
		orderHolds.add(orderHold);
	}

	/**
	 * Constructor for creating an empty PreCaptureCheckoutActionContext.
	 *
	 */
	public PreCaptureCheckoutActionContextImpl() {
		this(null, null, null, false, false, null, null);
	}

	@Override
	public List<OrderHold> getOrderHolds() {
		return orderHolds;
	}

	@Override
	public void addOrderHold(final OrderHold orderHold) {
		orderHolds.add(orderHold);
	}

	@Override
	public ShoppingCart getShoppingCart() {
		return shoppingCart;
	}

	@Override
	public ShoppingCartTaxSnapshot getShoppingCartTaxSnapshot() {
		return shoppingCartTaxSnapshot;
	}

	@Override
	public Shopper getShopper() {
		return shoppingCart.getShopper();
	}

	@Override
	public CustomerSession getCustomerSession() {
		return customerSession;
	}

	@Override
	public CartOrder getCartOrder() {
		return cartOrderExtractor.apply(getShopper(), getOrder());
	}

	protected BiFunction<Shopper, Order, CartOrder> getCartOrderExtractor() {
		return cartOrderExtractor;
	}
}
