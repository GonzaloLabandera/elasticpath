/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions;

import java.util.Map;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.plugin.tax.domain.TaxDocument;
import com.elasticpath.plugin.tax.domain.TaxDocumentId;

/**
 * Container class for data required by setupCheckoutActions and reversibleCheckoutActions.
 */
public interface CheckoutActionContext {

	/**
	 * Gets the {@link Customer}.  If you need to save the Customer, you need to know where it comes from.
	 * <p>
	 * Look inside the implementation.
	 *
	 * @return the {@link Customer}
	 */
	Customer getCustomer();

	/**
	 * Sets the order.
	 *
	 * @param order the order
	 */
	void setOrder(Order order);

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	Order getOrder();

	/**
	 * Gets the is order exchange.
	 *
	 * @return the is order exchange
	 */
	boolean isOrderExchange();

	/**
	 * Gets is Await Exchange Completion.
	 *
	 * @return is Await Exchange Completion
	 */
	boolean isAwaitExchangeCompletion();

	/**
	 * Gets the order return exchange.
	 *
	 * @return order return exchange
	 */
	OrderReturn getExchange();

	/**
	 * Gets a map of tax document id and tax document.
	 *
	 * @return a map of tax document id and tax document
	 */
	Map<TaxDocumentId, TaxDocument> getTaxDocuments();

	/**
	 * Adds one tax document to the taxDocuments map.
	 *
	 * @param taxDocumentId the key of the taxDocuments map
	 * @param taxDocument   the value of the taxDocuments map
	 */
	void addTaxDocument(TaxDocumentId taxDocumentId, TaxDocument taxDocument);
}
