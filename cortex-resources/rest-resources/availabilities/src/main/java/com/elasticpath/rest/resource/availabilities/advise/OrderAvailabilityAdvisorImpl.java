/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.availabilities.advise;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.availabilities.AvailabilityForCartLineItemIdentifier;
import com.elasticpath.rest.definition.availabilities.OrderAvailabilityAdvisor;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.ItemAvailabilityValidationService;

/**
 * Advisor for order.
 */
public class OrderAvailabilityAdvisorImpl implements OrderAvailabilityAdvisor.ReadLinkedAdvisor {

	private final OrderIdentifier orderIdentifier;

	private final ItemAvailabilityValidationService itemAvailabilityValidationService;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier                   orderIdentifier
	 * @param itemAvailabilityValidationService itemAvailabilityValidationService
	 */
	@Inject
	public OrderAvailabilityAdvisorImpl(
			@RequestIdentifier final OrderIdentifier orderIdentifier,
			@ResourceService final ItemAvailabilityValidationService itemAvailabilityValidationService) {
		this.orderIdentifier = orderIdentifier;
		this.itemAvailabilityValidationService = itemAvailabilityValidationService;
	}

	@Override
	public Observable<LinkedMessage<AvailabilityForCartLineItemIdentifier>> onLinkedAdvise() {
		return itemAvailabilityValidationService.validateItemUnavailable(orderIdentifier);
	}
}
