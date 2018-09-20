/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.services;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.BillingaddressInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;

/**
 * Validation service for checking if there is a billing address on an order.
 */
public interface OrderBillingAddressValidationService {

	/**
	 * Check if an order has a billing address.
	 *
	 * @param orderIdentifier orderIdentifier
	 * @return the message of missing billing address
	 */
	Observable<LinkedMessage<BillingaddressInfoIdentifier>> validateBillingAddressExist(OrderIdentifier orderIdentifier);
}
