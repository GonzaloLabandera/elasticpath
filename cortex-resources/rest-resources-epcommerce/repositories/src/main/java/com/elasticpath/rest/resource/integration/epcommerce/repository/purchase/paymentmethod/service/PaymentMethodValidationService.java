/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.paymentmethod.service;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentmethodInfoIdentifier;

/**
 * This service is used to check if payment method was specified for the given order.
 * FIXME this MUST move to PaymentMethod resource after PaymentMethod resource is concerted to Helix
 */
public interface PaymentMethodValidationService {

	/**
	 * Check if payment method for this order was provided.
	 *
	 * @param orderIdentifier order identifier
	 * @return the message for the missing payment method
	 */
	Observable<LinkedMessage<PaymentmethodInfoIdentifier>> validatePaymentForOrder(OrderIdentifier orderIdentifier);
}
