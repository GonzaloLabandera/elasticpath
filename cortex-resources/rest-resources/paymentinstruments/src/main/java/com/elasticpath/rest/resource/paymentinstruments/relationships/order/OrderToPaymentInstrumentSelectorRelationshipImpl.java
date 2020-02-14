/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorForOrderRelationship;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Order to Payment Instrument Selector link.
 */
public class OrderToPaymentInstrumentSelectorRelationshipImpl implements OrderPaymentInstrumentSelectorForOrderRelationship.LinkTo {

	private final OrderIdentifier orderIdentifier;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier order identifier
	 */
	@Inject
	public OrderToPaymentInstrumentSelectorRelationshipImpl(@RequestIdentifier final OrderIdentifier orderIdentifier) {
		this.orderIdentifier = orderIdentifier;
	}

	@Override
	public Observable<OrderPaymentInstrumentSelectorIdentifier> onLinkTo() {
		return Observable.just(OrderPaymentInstrumentSelectorIdentifier.builder()
				.withOrder(orderIdentifier)
				.build());
	}

}
