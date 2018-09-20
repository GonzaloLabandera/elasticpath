/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.services;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.EmailInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;

/**
 * Validation service for checking if there is a email address on an order.
 */
public interface OrderEmailValidationService {

	/**
	 * Check if an order has an email.
	 *
	 * @param orderIdentifier orderIdentifier
	 * @return the message of missing email address
	 */
	Observable<LinkedMessage<EmailInfoIdentifier>> validateEmailAddressExists(OrderIdentifier orderIdentifier);
}
