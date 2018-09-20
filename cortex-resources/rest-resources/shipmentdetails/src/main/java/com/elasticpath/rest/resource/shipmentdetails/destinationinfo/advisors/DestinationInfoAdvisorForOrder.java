/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.shipmentdetails.destinationinfo.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.DestinationInfoIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.OrderDestinationInfoAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.destinationinfo.DestinationInfoValidationService;

/**
 * Destination info advisor for an order.
 */
public class DestinationInfoAdvisorForOrder implements OrderDestinationInfoAdvisor.ReadLinkedAdvisor {

	private final OrderIdentifier orderIdentifier;
	private final DestinationInfoValidationService destinationInfoValidationService;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier						identifier
	 * @param destinationInfoValidationService		service
	 */
	@Inject
	public DestinationInfoAdvisorForOrder(@RequestIdentifier final OrderIdentifier orderIdentifier,
										  @ResourceService final DestinationInfoValidationService destinationInfoValidationService) {
		this.orderIdentifier = orderIdentifier;
		this.destinationInfoValidationService = destinationInfoValidationService;
	}

	@Override
	public Observable<LinkedMessage<DestinationInfoIdentifier>> onLinkedAdvise() {
		return destinationInfoValidationService.validateDestinationInfo(orderIdentifier);
	}
}
