/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.impl;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.EmailInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.OrderEmailValidationService;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Validation service for checking if there is a email address on an order.
 */
@Component
public class OrderEmailValidationServiceImpl implements OrderEmailValidationService {

	private static final String MESSAGE_NEED_EMAIL = "An email address must be provided before you can complete the purchase.";

	private CustomerRepository customerRepository;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<LinkedMessage<EmailInfoIdentifier>> validateEmailAddressExists(final OrderIdentifier orderIdentifier) {
		return doesEmailExists()
				.flatMapObservable(hasEmail -> getLinkedMessage(hasEmail, orderIdentifier));
	}

	/**
	 * Check if email exists for order.
	 *
	 * @return true if email exists for order
	 */
	protected Single<Boolean> doesEmailExists() {
		return customerRepository.getCustomer(resourceOperationContext.getUserIdentifier())
				.map(Customer::getEmail)
				.map(this::isEmailValid);
	}

	/**
	 * Check if customer email is valid.
	 *
	 * @param customerEmail customerEmail
	 * @return true if customer email is valid
	 */
	protected boolean isEmailValid(final String customerEmail) {
		return StringUtils.isNotEmpty(customerEmail)
				&& ObjectUtils.notEqual(AuthenticationConstants.ANONYMOUS_USER_ID, customerEmail);
	}

	/**
	 * Get linked message if order doesn't have email address.
	 *
	 * @param hasEmail        true if order has email
	 * @param orderIdentifier orderIdentifier
	 * @return linked message for email address
	 */
	protected Observable<LinkedMessage<EmailInfoIdentifier>> getLinkedMessage(final Boolean hasEmail, final OrderIdentifier orderIdentifier) {
		if (!hasEmail) {
			return Observable.just(createLinkedMessage(EmailInfoIdentifier.builder()
					.withOrder(orderIdentifier)
					.build()));
		}
		return Observable.empty();
	}

	/**
	 * Create linked message.
	 *
	 * @param emailInfoIdentifier email info identifier
	 * @return message with link to email info identifier
	 */
	protected LinkedMessage<EmailInfoIdentifier> createLinkedMessage(final EmailInfoIdentifier emailInfoIdentifier) {
		return LinkedMessage.<EmailInfoIdentifier>builder()
				.withType(StructuredMessageTypes.NEEDINFO)
				.withId(StructuredErrorMessageIdConstants.NEED_EMAIL)
				.withDebugMessage(MESSAGE_NEED_EMAIL)
				.withLinkedIdentifier(emailInfoIdentifier)
				.build();
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
