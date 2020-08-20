/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer;

import java.util.Currency;

import com.elasticpath.domain.shopper.ShopperReference;


/**
 * A customer session keeps track of information about customers
 * who may not be logged in (using cookies).
 *
 */
public interface CustomerSession extends CustomerSessionTransientData, ShopperReference {
	/**
	 * Get the currency of the customer corresponding to the shopping cart.
	 *
	 * @return the <code>Currency</code>
	 */
	Currency getCurrency();

	/**
	 * Set the currency of the customer corresponding to the shopping cart.
	 *
	 * @param currency the <code>Currency</code>
	 */
	void setCurrency(Currency currency);
}
