/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.purchase;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchasePaymentInstrumentRepository;

/**
 * Prototype for Read operations on Purchase Payment Instruments.
 */
public class ReadPurchasePaymentInstrumentsPrototype implements PurchasePaymentInstrumentsResource.Read {

	private final PurchasePaymentInstrumentsIdentifier paymentInstrumentsIdentifier;

	private final PurchasePaymentInstrumentRepository repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentsIdentifier identifier
	 * @param repository                   identifier-entity repository
	 */
	@Inject
	public ReadPurchasePaymentInstrumentsPrototype(
			@RequestIdentifier final PurchasePaymentInstrumentsIdentifier paymentInstrumentsIdentifier,
			@ResourceRepository final PurchasePaymentInstrumentRepository repository) {
		this.paymentInstrumentsIdentifier = paymentInstrumentsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<PurchasePaymentInstrumentIdentifier> onRead() {
		String scope = paymentInstrumentsIdentifier.getPurchase().getPurchases().getScope().getValue();
		return repository.findPurchaseInstrumentsByPurchaseId(scope, paymentInstrumentsIdentifier);
	}
}
