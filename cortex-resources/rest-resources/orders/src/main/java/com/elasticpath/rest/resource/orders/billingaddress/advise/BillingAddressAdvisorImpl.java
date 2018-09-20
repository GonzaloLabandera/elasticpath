/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.orders.billingaddress.advise;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.BillingaddressInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderBillingInfoAdvisor;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.OrderBillingAddressValidationService;

/**
 * Need information advisor for the email.
 */
public class BillingAddressAdvisorImpl implements OrderBillingInfoAdvisor.ReadLinkedAdvisor {

	private final OrderIdentifier orderIdentifier;

	private final OrderBillingAddressValidationService orderBillingAddressValidationService;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier                       orderIdentifier
	 * @param orderBillingAddressValidationService orderBillingAddressValidationService
	 */
	@Inject
	public BillingAddressAdvisorImpl(
			@RequestIdentifier final OrderIdentifier orderIdentifier,
			@ResourceService final OrderBillingAddressValidationService orderBillingAddressValidationService) {
		this.orderIdentifier = orderIdentifier;
		this.orderBillingAddressValidationService = orderBillingAddressValidationService;
	}

	@Override
	public Observable<LinkedMessage<BillingaddressInfoIdentifier>> onLinkedAdvise() {
		return orderBillingAddressValidationService.validateBillingAddressExist(orderIdentifier);
	}


}
