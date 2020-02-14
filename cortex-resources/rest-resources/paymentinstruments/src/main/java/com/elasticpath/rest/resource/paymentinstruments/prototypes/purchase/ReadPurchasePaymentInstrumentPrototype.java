/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.purchase;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchasePaymentInstrumentRepository;

/**
 * Prototype for Read operations on a Purchase Payment Instrument.
 */
public class ReadPurchasePaymentInstrumentPrototype implements PurchasePaymentInstrumentResource.Read {

	private final PurchasePaymentInstrumentIdentifier paymentInstrumentIdentifier;

	private final PurchasePaymentInstrumentRepository repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentIdentifier identifier
	 * @param repository                  identifier-entity repository
	 */
	@Inject
	public ReadPurchasePaymentInstrumentPrototype(
			@RequestIdentifier final PurchasePaymentInstrumentIdentifier paymentInstrumentIdentifier,
			@ResourceRepository final PurchasePaymentInstrumentRepository repository) {
		this.paymentInstrumentIdentifier = paymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PurchasePaymentInstrumentEntity> onRead() {
		return repository.findOne(paymentInstrumentIdentifier);
	}
}
