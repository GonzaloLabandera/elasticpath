/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.relationships.order;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentMethodIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsToOrderPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Order Request Instructions Form to Order Payment Method link.
 */
public class PaymentInstructionsToOrderPaymentMethodRelationshipImpl implements PaymentInstructionsToOrderPaymentMethodRelationship.LinkTo {

	private final OrderPaymentInstructionsIdentifier instructionsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param instructionsIdentifier instructionsIdentifier
	 */
	@Inject
	public PaymentInstructionsToOrderPaymentMethodRelationshipImpl(@RequestIdentifier final OrderPaymentInstructionsIdentifier
																		   instructionsIdentifier) {
		this.instructionsIdentifier = instructionsIdentifier;
	}

	@Override
	public Observable<OrderPaymentMethodIdentifier> onLinkTo() {
		return Observable.just(buildOrderPaymentMethodIdentifier(
				instructionsIdentifier.getOrderPaymentMethod().getOrderPaymentMethods().getOrder().getScope(),
				instructionsIdentifier.getOrderPaymentMethod().getPaymentMethodId(),
				instructionsIdentifier.getOrderPaymentMethod().getOrderPaymentMethods().getOrder().getOrderId()
		));
	}
}
