/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.purchase;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentToPurchasePaymentInstrumentsRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Purchase Payment Instrument to Purchase Payment Instruments link.
 */
public class PurchasePaymentInstrumentToPurchasePaymentInstrumentsRelationshipImpl
		implements PurchasePaymentInstrumentToPurchasePaymentInstrumentsRelationship.LinkTo {

	private final PurchasePaymentInstrumentIdentifier paymentInstrumentIdentifier;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentIdentifier purchase payment instrument identifier
	 */
	@Inject
	public PurchasePaymentInstrumentToPurchasePaymentInstrumentsRelationshipImpl(
			@RequestIdentifier final PurchasePaymentInstrumentIdentifier paymentInstrumentIdentifier) {
		this.paymentInstrumentIdentifier = paymentInstrumentIdentifier;
	}

	@Override
	public Observable<PurchasePaymentInstrumentsIdentifier> onLinkTo() {
		return Observable.just(paymentInstrumentIdentifier.getPurchasePaymentInstruments());
	}

}
