/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.plugin.tax.domain.TaxDocument;
import com.elasticpath.plugin.tax.domain.TaxDocumentId;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;

/**
 * Container class for data required by setupCheckoutActions and reversibleCheckoutActions.
 */
public class CheckoutActionContextImpl implements CheckoutActionContext {

	private Order order;
	private final ShoppingCart shoppingCart;
	private final CustomerSession customerSession;
	private final boolean orderExchange;
	private final boolean awaitExchangeCompletion;
	private final OrderReturn exchange;
	private final Map<TaxDocumentId, TaxDocument> taxDocuments = new HashMap<>();
	private final ShoppingCartTaxSnapshot shoppingCartTaxSnapshot;
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
	public CheckoutActionContextImpl(final ShoppingCart shoppingCart,
									 final ShoppingCartTaxSnapshot shoppingCartTaxSnapshot,
									 final CustomerSession customerSession,
									 final boolean isOrderExchange,
									 final boolean awaitExchangeCompletion,
									 final OrderReturn exchange,
									 final BiFunction<Shopper, Order, CartOrder> cartOrderExtractor) {
		this.shoppingCart = shoppingCart;
		this.shoppingCartTaxSnapshot = shoppingCartTaxSnapshot;
		this.customerSession = customerSession;
		this.orderExchange = isOrderExchange;
		this.awaitExchangeCompletion = awaitExchangeCompletion;
		this.exchange = exchange;
		this.cartOrderExtractor = cartOrderExtractor;
	}

	@Override
	public void setOrder(final Order order) {
		this.order = order;
	}

	@Override
	public Order getOrder() {
		return order;
	}

	@Override
	public boolean isOrderExchange() {
		return orderExchange;
	}

	@Override
	public boolean isAwaitExchangeCompletion() {
		return awaitExchangeCompletion;
	}

	@Override
	public OrderReturn getExchange() {
		return exchange;
	}

	@Override
	public Shopper getShopper() {
		return shoppingCart.getShopper();
	}

	@Override
	public Customer getCustomer() {
		return getShopper().getCustomer();
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
	public String getCustomerIpAddress() {
		return customerSession.getIpAddress();
	}

	@Override
	public CustomerSession getCustomerSession() {
		return customerSession;
	}

	@Override
	public Map<TaxDocumentId, TaxDocument> getTaxDocuments() {
		return taxDocuments;
	}

	@Override
	public void addTaxDocument(final TaxDocumentId taxDocumentId, final TaxDocument taxDocument) {
		taxDocuments.put(taxDocumentId, taxDocument);
	}

	@Override
	public CartOrder getCartOrder() {
		return cartOrderExtractor.apply(getShopper(), getOrder());
	}

	protected BiFunction<Shopper, Order, CartOrder> getCartOrderExtractor() {
		return cartOrderExtractor;
	}

}
