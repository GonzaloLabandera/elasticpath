/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.customer;

import java.util.Currency;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.EpPersistenceService;

/**
 * Provide customer-session related business service.
 */
public interface CustomerSessionService extends EpPersistenceService {

	/**
	 * Creates a new {@link CustomerSession} with a particular {@link Shopper}.
	 *
	 * @param shopper the {@link Shopper} you want to create this {@link CustomerSession} with.
	 * @return a new {@link CustomerSession} attached to a {@link Shopper}.
	 */
	CustomerSession createWithShopper(Shopper shopper);

	/**
	 * Change the Customer associated with the session to a new registered Customer.
	 * Creates a new Shopper and handles updates (such as merging {@link com.elasticpath.domain.shoppingcart.ShoppingCart}s
	 * and {@link com.elasticpath.domain.shoppingcart.WishList}s) and updating the {@link CustomerSession}.
	 *
	 * @param singleSessionShopper the single session shopper that is being removed
	 * @param registeredCustomer the registered customer that receives the cart items
	 * @param storeCode the store code
	 * @throws EpServiceException - in case of any errors
	 */
	void changeFromSingleSessionToRegisteredCustomer(Shopper singleSessionShopper, Customer registeredCustomer,
													 String storeCode) throws EpServiceException;

	/**
	 * Initializes a {@link CustomerSession} such that it can be used for pricing computations.
	 *
	 * @param customerSession the customer session
	 * @param storeCode the store code
	 * @param currency the currency
	 */
	void initializeCustomerSessionForPricing(CustomerSession customerSession, String storeCode, Currency currency);

}