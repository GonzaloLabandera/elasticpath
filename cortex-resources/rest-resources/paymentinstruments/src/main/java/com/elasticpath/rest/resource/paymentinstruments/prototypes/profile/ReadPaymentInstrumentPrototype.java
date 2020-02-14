/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Read operation of Payment Instrument.
 */
public class ReadPaymentInstrumentPrototype implements PaymentInstrumentResource.Read {

	private final PaymentInstrumentIdentifier paymentInstrumentIdentifier;

	private final Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentIdentifier identifier
	 * @param repository                  identifier-entity repository
	 */
	@Inject
	public ReadPaymentInstrumentPrototype(
			@RequestIdentifier final PaymentInstrumentIdentifier paymentInstrumentIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> repository) {
		this.paymentInstrumentIdentifier = paymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentInstrumentEntity> onRead() {
		return repository.findOne(paymentInstrumentIdentifier);
	}
}