/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.plugin.tax.domain.TaxDocument;
import com.elasticpath.plugin.tax.domain.TaxDocumentId;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;

/**
 * Container class for data required by setupCheckoutActions and reversibleCheckoutActions.
 */
public class CheckoutActionContextImpl implements CheckoutActionContext {

	private Order order;
	private final boolean orderExchange;
	private final boolean awaitExchangeCompletion;
	private final OrderReturn exchange;
	private final Map<TaxDocumentId, TaxDocument> taxDocuments = new HashMap<>();

	/**
	 * Constructor for creating the initial CheckoutActionContext object.
	 * @param isOrderExchange indicates whether or not the cart is for an order exchange
	 * @param awaitExchangeCompletion indicates whether or not the cart should wait for exchange completion
	 * @param exchange                the orderReturn object which is used for exchanges
	 */
	public CheckoutActionContextImpl(final boolean isOrderExchange,
			final boolean awaitExchangeCompletion,
			final OrderReturn exchange
	) {
		this.orderExchange = isOrderExchange;
		this.awaitExchangeCompletion = awaitExchangeCompletion;
		this.exchange = exchange;
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
	public Map<TaxDocumentId, TaxDocument> getTaxDocuments() {
		return taxDocuments;
	}

	@Override
	public void addTaxDocument(final TaxDocumentId taxDocumentId, final TaxDocument taxDocument) {
		taxDocuments.put(taxDocumentId, taxDocument);
	}

	@Override
	public Customer getCustomer() {
		return order.getCustomer();
	}
}
