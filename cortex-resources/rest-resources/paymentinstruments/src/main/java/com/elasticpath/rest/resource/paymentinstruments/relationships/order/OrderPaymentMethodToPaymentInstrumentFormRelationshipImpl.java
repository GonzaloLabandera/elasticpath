/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentFormForOrderPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Order Payment Provider to Payment Instrument Form link.
 */
public class OrderPaymentMethodToPaymentInstrumentFormRelationshipImpl implements PaymentInstrumentFormForOrderPaymentMethodRelationship.LinkTo {

	private final OrderPaymentMethodIdentifier orderPaymentMethodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentMethodIdentifier {@link OrderPaymentMethodIdentifier}
	 */
	@Inject
	public OrderPaymentMethodToPaymentInstrumentFormRelationshipImpl(
			@RequestIdentifier final OrderPaymentMethodIdentifier orderPaymentMethodIdentifier) {
		this.orderPaymentMethodIdentifier = orderPaymentMethodIdentifier;
	}

	@Override
	public Observable<OrderPaymentInstrumentFormIdentifier> onLinkTo() {
		return Observable.just(OrderPaymentInstrumentFormIdentifier.builder()
				.withOrderPaymentMethod(orderPaymentMethodIdentifier)
				.build());
	}
}
