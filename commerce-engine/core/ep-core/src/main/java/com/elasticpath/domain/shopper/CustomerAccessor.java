/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.shopper;

import com.elasticpath.domain.customer.Customer;

/**
 * Provides access to a {@link Customer} on the implementing object. 
 */
public interface CustomerAccessor {

	/**
	 * Gets a {@link Customer}.
	 *
	 * @return a {@link Customer}.
	 */
	Customer getCustomer();

	/**
	 * Sets a {@link Customer}.
	 *
	 * @param customer the {@link Customer}.
	 */
	void setCustomer(Customer customer);

	/**
	 * Gets a {@link Customer} representing an account.
	 *
	 * @return a {@link Customer} account.
	 */
	Customer getAccount();

	/**
	 * Sets a {@link Customer} representing an account.
	 *
	 * @param account the {@link Customer} account.
	 */
	void setAccount(Customer account);

	/**
	 * Set to true if the {@link Customer} is signed in.
	 *
	 * @param signedIn set to true if the {@link Customer} is signed in.
	 */
	void setSignedIn(boolean signedIn);

	/**
	 * Returns true if the {@link Customer} is signed in.
	 *
	 * @return true if the {@link Customer} is signed in.
	 */
	boolean isSignedIn();
	
}