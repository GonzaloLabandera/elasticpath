/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentmethodInfoIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormPaymentMethodInfoAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.paymentmethod.service.PaymentMethodValidationService;

/**
 * Advisor on the purchase form for missing payment method information.
 * FIXME this MUST move to PaymentMethod resource after PaymentMethod resource is converted to Helix
 */
public class PurchaseFormPaymentMethodInfoAdvisorImpl implements PurchaseFormPaymentMethodInfoAdvisor.LinkedFormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;

	private final PaymentMethodValidationService paymentMethodValidationService;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier create purchase form identifier
	 * @param paymentMethodValidationService service for the payment method
	 */
	@Inject
	public PurchaseFormPaymentMethodInfoAdvisorImpl(
			@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
			@ResourceService final PaymentMethodValidationService paymentMethodValidationService) {

		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.paymentMethodValidationService = paymentMethodValidationService;
	}

	@Override
	public Observable<LinkedMessage<PaymentmethodInfoIdentifier>> onLinkedAdvise() {
		OrderIdentifier order = createPurchaseFormIdentifier.getOrder();
		return paymentMethodValidationService.validatePaymentForOrder(order);
	}
}
