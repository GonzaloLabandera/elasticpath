/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.relationships.order;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderRequestInstructionsFormIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormForOrderPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Payment Providers to orders link.
 */
public class PaymentMethodToRequestInstructionsForOrderFormRelationshipImpl
		implements RequestInstructionsFormForOrderPaymentMethodRelationship.LinkTo {

	private final OrderPaymentMethodIdentifier paymentMethod;


	/**
	 * Constructor.
	 *
	 * @param paymentMethod {@link OrderPaymentMethodIdentifier}
	 */
	@Inject
	public PaymentMethodToRequestInstructionsForOrderFormRelationshipImpl(@RequestIdentifier final OrderPaymentMethodIdentifier paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	@Override
	public Observable<OrderRequestInstructionsFormIdentifier> onLinkTo() {
		IdentifierPart<String> scope = paymentMethod.getOrderPaymentMethods().getOrder().getScope();
		IdentifierPart<String> paymentMethodId = paymentMethod.getPaymentMethodId();
		IdentifierPart<String> orderId = paymentMethod.getOrderPaymentMethods().getOrder().getOrderId();

		return Observable.just(buildOrderRequestInstructionsFormIdentifier(scope, paymentMethodId, orderId));
	}
}
