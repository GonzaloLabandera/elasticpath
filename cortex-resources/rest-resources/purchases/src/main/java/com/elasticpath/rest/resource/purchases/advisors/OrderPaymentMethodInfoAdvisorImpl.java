/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentmethodInfoIdentifier;
import com.elasticpath.rest.definition.purchases.OrderPaymentMethodInfoAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.paymentmethod.service.PaymentMethodValidationService;

/**
 * This is advisor which advises on order about payment method.
 * FIXME this MUST move to PaymentMethod resource after PaymentMethod resource is converted to Helix
 */
public class OrderPaymentMethodInfoAdvisorImpl implements OrderPaymentMethodInfoAdvisor.ReadLinkedAdvisor {

	private final OrderIdentifier orderIdentifier;
	private final PaymentMethodValidationService paymentMethodValidationService;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier order identifier
	 * @param paymentMethodValidationService payment method service
	 */
	@Inject
	public OrderPaymentMethodInfoAdvisorImpl(
			@RequestIdentifier final OrderIdentifier orderIdentifier,
			@ResourceService final PaymentMethodValidationService paymentMethodValidationService) {

		this.orderIdentifier = orderIdentifier;
		this.paymentMethodValidationService = paymentMethodValidationService;
	}

	@Override
	public Observable<LinkedMessage<PaymentmethodInfoIdentifier>> onLinkedAdvise() {
		return paymentMethodValidationService.validatePaymentForOrder(orderIdentifier);
	}
}
