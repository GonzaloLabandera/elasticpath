/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.purchase;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentToPurchasePaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository;

/**
 * Purchase Payment Instrument to Purchase Payment Instruments link.
 */
public class PurchasePaymentInstrumentToPurchasePaymentMethodRelationshipImpl
		implements PurchasePaymentInstrumentToPurchasePaymentMethodRelationship.LinkTo {

	private final PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier;
	private final PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param purchasePaymentInstrumentIdentifier payment instrument identifier.
	 * @param repository                          repository
	 */
	@Inject
	public PurchasePaymentInstrumentToPurchasePaymentMethodRelationshipImpl(
			@RequestIdentifier final PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier,

			@ResourceRepository final PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository repository) {
		this.purchasePaymentInstrumentIdentifier = purchasePaymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<PurchasePaymentMethodIdentifier> onLinkTo() {
		return repository.getPurchasePaymentMethodIdentifier(purchasePaymentInstrumentIdentifier);
	}
}
