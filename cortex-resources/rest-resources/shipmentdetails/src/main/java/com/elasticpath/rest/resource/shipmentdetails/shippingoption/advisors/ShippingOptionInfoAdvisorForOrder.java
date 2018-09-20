/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.shipmentdetails.shippingoption.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.OrderShippingOptionInfoAdvisor;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.shippinginfo.ShippingOptionInfoValidationService;

/**
 * Shipping option info advisor for an order.
 */
public class ShippingOptionInfoAdvisorForOrder implements OrderShippingOptionInfoAdvisor.ReadLinkedAdvisor {

	private final OrderIdentifier orderIdentifier;
	private final ShippingOptionInfoValidationService shippingOptionInfoValidationService;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier							identifier
	 * @param shippingOptionInfoValidationService		service
	 */
	@Inject
	public ShippingOptionInfoAdvisorForOrder(@RequestIdentifier final OrderIdentifier orderIdentifier,
											 @ResourceService final ShippingOptionInfoValidationService shippingOptionInfoValidationService) {
		this.orderIdentifier = orderIdentifier;
		this.shippingOptionInfoValidationService = shippingOptionInfoValidationService;
	}

	@Override
	public Observable<LinkedMessage<ShippingOptionInfoIdentifier>> onLinkedAdvise() {
		return shippingOptionInfoValidationService.validateShippingOptionInfo(orderIdentifier);
	}
}
