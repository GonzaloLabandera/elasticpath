/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer;

import java.util.Currency;

/**
 * A customer session keeps track of request-specific information that is primarily passed to Cortex via request headers.
 */
public interface CustomerSession extends CustomerSessionTransientData {
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
