/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.customer;

import com.elasticpath.domain.customer.Customer;

/**
 * Services providing customer registration operations.
 */
public interface CustomerRegistrationService {

	/**
	 * Creates a password and sends it to customer.
	 *
	 * @param customer the customer whose password is to be sent
	 * @return the updated Customer
	 */
	Customer registerCustomerAndSendPassword(Customer customer);

	/**
	 * Register the given persisted anonymous customer.
	 *
	 * @param customer the persisted anonymous customer.
	 * @return the persisted customer.
	 */
	Customer registerCustomer(Customer customer);
}
