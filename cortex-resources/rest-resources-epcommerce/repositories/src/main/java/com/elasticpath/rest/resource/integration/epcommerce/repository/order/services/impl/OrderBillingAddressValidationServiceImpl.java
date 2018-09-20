/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.impl;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.BillingaddressInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.OrderBillingAddressValidationService;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Validation service for checking if there is a billing address on an order.
 */
@Component
public class OrderBillingAddressValidationServiceImpl implements OrderBillingAddressValidationService {

	private static final String MESSAGE_NEED_BILLING_ADDRESS = "A billing address must be provided before you can complete the purchase.";

	private CartOrderRepository cartOrderRepository;

	@Override
	public Observable<LinkedMessage<BillingaddressInfoIdentifier>> validateBillingAddressExist(final OrderIdentifier orderIdentifier) {
		return isMissingBillingAddress(orderIdentifier)
				.flatMapObservable(noSelectedAddress -> getLinkedMessage(noSelectedAddress, orderIdentifier));
	}

	/**
	 * Check if order doesn't have billing address.
	 *
	 * @param orderIdentifier orderIdentifier
	 * @return true if order doesn't have billing address
	 */
	protected Single<Boolean> isMissingBillingAddress(final OrderIdentifier orderIdentifier) {
		String orderId = orderIdentifier.getOrderId().getValue();
		String scope = orderIdentifier.getScope().getValue();
		return cartOrderRepository.findByGuidAsSingle(scope, orderId)
				.flatMapMaybe(cartOrder -> cartOrderRepository.getBillingAddress(cartOrder))
				.isEmpty();
	}

	/**
	 * Get linked message if order doesn't have billing address.
	 *
	 * @param noSelectedAddress true if order doesn't have billing address
	 * @param orderIdentifier   orderIdentifier
	 * @return linked message for billing address
	 */
	protected Observable<LinkedMessage<BillingaddressInfoIdentifier>> getLinkedMessage(
			final Boolean noSelectedAddress, final OrderIdentifier orderIdentifier) {
		if (noSelectedAddress) {
			return Observable.just(createLinkedMessage(BillingaddressInfoIdentifier.builder()
					.withOrder(orderIdentifier)
					.build()));
		}
		return Observable.empty();
	}

	/**
	 * Create linked message.
	 *
	 * @param billingaddressInfoIdentifier billingaddressInfoIdentifier
	 * @return message with link to billing address info identifier
	 */
	protected LinkedMessage<BillingaddressInfoIdentifier> createLinkedMessage(final BillingaddressInfoIdentifier billingaddressInfoIdentifier) {
		return LinkedMessage.<BillingaddressInfoIdentifier>builder()
				.withType(StructuredMessageTypes.NEEDINFO)
				.withId(StructuredErrorMessageIdConstants.ID_NEED_BILLING_ADDRESS)
				.withDebugMessage(MESSAGE_NEED_BILLING_ADDRESS)
				.withLinkedIdentifier(billingaddressInfoIdentifier)
				.build();
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}
}
