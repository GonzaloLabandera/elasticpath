/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Profile Payment Instrument - Delete Operation.
 */
public class DeleteProfilePaymentInstrumentPrototype implements PaymentInstrumentResource.Delete {

	private final PaymentInstrumentIdentifier paymentInstrumentIdentifier;
	private final Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentIdentifier identifier
	 * @param repository                  repository
	 */
	@Inject
	public DeleteProfilePaymentInstrumentPrototype(
			@RequestIdentifier final PaymentInstrumentIdentifier paymentInstrumentIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> repository) {
		this.paymentInstrumentIdentifier = paymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onDelete() {
		return repository.delete(paymentInstrumentIdentifier);
	}
}
