/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.BillingaddressInfoIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormBillingInfoAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.OrderBillingAddressValidationService;

/**
 * Advisor on the purchase form for missing billing info on order.
 */
public class PurchaseFormBillingInfoAdvisorImpl implements PurchaseFormBillingInfoAdvisor.LinkedFormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;

	private final OrderBillingAddressValidationService orderBillingAddressValidationService;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier         createPurchaseFormIdentifier
	 * @param orderBillingAddressValidationService orderBillingAddressValidationService
	 */
	@Inject
	public PurchaseFormBillingInfoAdvisorImpl(
			@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
			@ResourceService final OrderBillingAddressValidationService orderBillingAddressValidationService) {
		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.orderBillingAddressValidationService = orderBillingAddressValidationService;
	}

	@Override
	public Observable<LinkedMessage<BillingaddressInfoIdentifier>> onLinkedAdvise() {
		return orderBillingAddressValidationService.validateBillingAddressExist(createPurchaseFormIdentifier.getOrder());
	}
}
