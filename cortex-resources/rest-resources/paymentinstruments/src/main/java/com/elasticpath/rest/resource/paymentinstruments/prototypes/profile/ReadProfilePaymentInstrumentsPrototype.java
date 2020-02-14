/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for order-payment instruments read operation.
 */
public class ReadProfilePaymentInstrumentsPrototype implements PaymentInstrumentsResource.Read {

	private final PaymentInstrumentsIdentifier paymentInstrumentsIdentifier;

	private final Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentsIdentifier identifier
	 * @param repository                   repository
	 */
	@Inject
	public ReadProfilePaymentInstrumentsPrototype(
			@RequestIdentifier final PaymentInstrumentsIdentifier paymentInstrumentsIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> repository) {
		this.paymentInstrumentsIdentifier = paymentInstrumentsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<PaymentInstrumentIdentifier> onRead() {
		return repository.findAll(paymentInstrumentsIdentifier.getScope());
	}
}
