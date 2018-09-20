/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.availabilities.AvailabilityForCartLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormAvailabilityAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.ItemAvailabilityValidationService;

/**
 * Advisor on the purchase form for unavailable items on the cart order.
 */
public class PurchaseFormAvailabilityAdvisorImpl implements PurchaseFormAvailabilityAdvisor.LinkedFormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;

	private final ItemAvailabilityValidationService itemAvailabilityValidationService;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier      createPurchaseFormIdentifier
	 * @param itemAvailabilityValidationService itemAvailabilityValidationService
	 */
	@Inject
	public PurchaseFormAvailabilityAdvisorImpl(
			@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
			@ResourceService final ItemAvailabilityValidationService itemAvailabilityValidationService) {
		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.itemAvailabilityValidationService = itemAvailabilityValidationService;
	}

	@Override
	public Observable<LinkedMessage<AvailabilityForCartLineItemIdentifier>> onLinkedAdvise() {
		return itemAvailabilityValidationService.validateItemUnavailable(createPurchaseFormIdentifier.getOrder());
	}
}
