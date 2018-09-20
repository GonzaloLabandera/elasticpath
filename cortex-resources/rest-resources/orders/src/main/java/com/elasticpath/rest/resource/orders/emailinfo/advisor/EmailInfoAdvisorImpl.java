/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.orders.emailinfo.advisor;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.EmailInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderEmailInfoAdvisor;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.OrderEmailValidationService;

/**
 * Advisor for the email info.
 */
public class EmailInfoAdvisorImpl implements OrderEmailInfoAdvisor.ReadLinkedAdvisor {

	private final OrderIdentifier orderIdentifier;

	private final OrderEmailValidationService orderEmailValidationService;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier                orderIdentifier
	 * @param orderEmailValidationService orderEmailValidationService
	 */
	@Inject
	public EmailInfoAdvisorImpl(
			@RequestIdentifier final OrderIdentifier orderIdentifier,
			@ResourceService final OrderEmailValidationService orderEmailValidationService) {
		this.orderIdentifier = orderIdentifier;
		this.orderEmailValidationService = orderEmailValidationService;
	}

	@Override
	public Observable<LinkedMessage<EmailInfoIdentifier>> onLinkedAdvise() {
		return orderEmailValidationService.validateEmailAddressExists(orderIdentifier);
	}
}
