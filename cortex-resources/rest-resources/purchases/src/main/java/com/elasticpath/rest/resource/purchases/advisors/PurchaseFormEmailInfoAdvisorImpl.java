/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.EmailInfoIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormEmailInfoAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.OrderEmailValidationService;

/**
 * Advisor on the purchase form for missing email info on order.
 */
public class PurchaseFormEmailInfoAdvisorImpl implements PurchaseFormEmailInfoAdvisor.LinkedFormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;

	private final OrderEmailValidationService orderEmailValidationService;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier createPurchaseFormIdentifier
	 * @param orderEmailValidationService  orderEmailValidationService
	 */
	@Inject
	public PurchaseFormEmailInfoAdvisorImpl(
			@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
			@ResourceService final OrderEmailValidationService orderEmailValidationService) {
		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.orderEmailValidationService = orderEmailValidationService;
	}

	@Override
	public Observable<LinkedMessage<EmailInfoIdentifier>> onLinkedAdvise() {
		return orderEmailValidationService.validateEmailAddressExists(createPurchaseFormIdentifier.getOrder());
	}
}
