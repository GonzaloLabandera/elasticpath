/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.command.ExecutionResult;

/**
 * Strategy to derive customer and customer existence check.
 */
public interface CustomerIdentifierStrategy {

	/**
	 * Checks if a customer exists for the given parameters.
	 *
	 * @param userId userId of the customer
	 * @param storeCode store code
	 * @param customerIdentifierKey customer attribute key by which the customer existence is to be determined
	 *
	 * @return true if customer exists for the given parameters.
	 */
	ExecutionResult<Boolean> isCustomerExists(String userId, String storeCode, String customerIdentifierKey);

	/**
	 * Derives the customer's guid as per given parameters.
	 *
	 * @param userId userId of the customer
	 * @param storeCode store code
	 * @param customerIdentifierKey customer attribute key by which the customer existence is to be determined
	 *
	 * @return customer's guid derived as per given parameters.
	 */
	ExecutionResult<String> deriveCustomerGuid(String userId, String storeCode, String customerIdentifierKey);

	/**
	 * Returns the customer identifier key field obtained from settings.
     *
	 * @return customer identifier key.
	 */
	String getCustomerIdentificationKeyField();

	/**
	 * Returns the user id obtained from the customer.
	 *
	 * @param customer customer
	 * @return the user id.
	 */
	ExecutionResult<String> deriveUserIdFromCustomer(Customer customer);
}
