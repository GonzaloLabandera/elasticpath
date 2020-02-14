/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.purchase;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsForPurchaseRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Purchase to Purchase Payment Instruments link.
 */
public class PurchaseToPurchasePaymentInstrumentsRelationshipImpl implements PurchasePaymentInstrumentsForPurchaseRelationship.LinkTo {

	private final PurchaseIdentifier purchaseIdentifier;

	/**
	 * Constructor.
	 *
	 * @param purchaseIdentifier purchase identifier
	 */
	@Inject
	public PurchaseToPurchasePaymentInstrumentsRelationshipImpl(@RequestIdentifier final PurchaseIdentifier purchaseIdentifier) {
		this.purchaseIdentifier = purchaseIdentifier;
	}

	@Override
	public Observable<PurchasePaymentInstrumentsIdentifier> onLinkTo() {
		return Observable.just(PurchasePaymentInstrumentsIdentifier.builder()
				.withPurchase(purchaseIdentifier)
				.build());
	}

}
