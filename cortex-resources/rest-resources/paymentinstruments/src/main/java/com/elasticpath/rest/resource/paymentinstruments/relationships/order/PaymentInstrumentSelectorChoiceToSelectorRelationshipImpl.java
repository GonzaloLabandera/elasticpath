/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceOrderPaymentInstrumentSelectorRelationship;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Payment Instrument Selector Choice link back to selector.
 */
public class PaymentInstrumentSelectorChoiceToSelectorRelationshipImpl implements
		OrderPaymentInstrumentSelectorChoiceOrderPaymentInstrumentSelectorRelationship.LinkTo {

	private final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;

	/**
	 * Constructor.
	 *
	 * @param selectorChoiceIdentifier selector choice identifier
	 */
	@Inject
	public PaymentInstrumentSelectorChoiceToSelectorRelationshipImpl(
			@RequestIdentifier final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
	}

	@Override
	public Observable<OrderPaymentInstrumentSelectorIdentifier> onLinkTo() {
		return Observable.just(selectorChoiceIdentifier.getOrderPaymentInstrumentSelector());
	}

}
