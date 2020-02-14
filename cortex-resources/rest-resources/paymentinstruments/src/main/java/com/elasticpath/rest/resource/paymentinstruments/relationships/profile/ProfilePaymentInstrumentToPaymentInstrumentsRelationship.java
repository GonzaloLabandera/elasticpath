/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentToPaymentInstrumentsRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Payment Instrument to Payment Instruments link.
 */
public class ProfilePaymentInstrumentToPaymentInstrumentsRelationship implements PaymentInstrumentToPaymentInstrumentsRelationship.LinkTo {

	private final PaymentInstrumentIdentifier paymentInstrumentIdentifier;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentIdentifier the payment instrument identifier
	 */
	@Inject
	public ProfilePaymentInstrumentToPaymentInstrumentsRelationship(
			@RequestIdentifier final PaymentInstrumentIdentifier paymentInstrumentIdentifier) {
		this.paymentInstrumentIdentifier = paymentInstrumentIdentifier;
	}

	@Override
	public Observable<PaymentInstrumentsIdentifier> onLinkTo() {
		return Observable.just(paymentInstrumentIdentifier.getPaymentInstruments());
	}
}
