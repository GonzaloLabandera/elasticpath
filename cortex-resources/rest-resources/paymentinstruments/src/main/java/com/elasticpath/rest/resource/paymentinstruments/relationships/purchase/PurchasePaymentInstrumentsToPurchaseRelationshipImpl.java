/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.purchase;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsToPurchaseRelationship;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Purchase Payment Instruments to Purchase link.
 */
public class PurchasePaymentInstrumentsToPurchaseRelationshipImpl implements PurchasePaymentInstrumentsToPurchaseRelationship.LinkTo {

	private final PurchasePaymentInstrumentsIdentifier purchasePaymentInstrumentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param purchasePaymentInstrumentsIdentifier purchase payment instruments identifier.
	 */
	@Inject
	public PurchasePaymentInstrumentsToPurchaseRelationshipImpl(
			@RequestIdentifier final PurchasePaymentInstrumentsIdentifier purchasePaymentInstrumentsIdentifier) {
		this.purchasePaymentInstrumentsIdentifier = purchasePaymentInstrumentsIdentifier;
	}

	@Override
	public Observable<PurchaseIdentifier> onLinkTo() {
		return Observable.just(purchasePaymentInstrumentsIdentifier.getPurchase());
	}
}
