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
 * Payment Instrument Selector link back to Order.
 */
public class PaymentInstrumentSelectorToOrderRelationshipImpl implements OrderPaymentInstrumentSelectorForOrderRelationship.LinkFrom {

	private final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param selectorIdentifier selector identifier
	 */
	@Inject
	public PaymentInstrumentSelectorToOrderRelationshipImpl(
			@RequestIdentifier final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier) {
		this.selectorIdentifier = selectorIdentifier;
	}

	@Override
	public Observable<OrderIdentifier> onLinkFrom() {
		return Observable.just(selectorIdentifier.getOrder());
	}

}
