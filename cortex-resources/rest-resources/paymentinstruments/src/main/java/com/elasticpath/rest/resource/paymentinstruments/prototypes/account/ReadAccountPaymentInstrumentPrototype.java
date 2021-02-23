/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentResource;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Read operation of Payment Instrument.
 */
public class ReadAccountPaymentInstrumentPrototype implements AccountPaymentInstrumentResource.Read {

	private final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier;

	private final Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentIdentifier identifier
	 * @param repository                         identifier-entity repository
	 */
	@Inject
	public ReadAccountPaymentInstrumentPrototype(
			@RequestIdentifier final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> repository) {
		this.accountPaymentInstrumentIdentifier = accountPaymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentInstrumentEntity> onRead() {
		return repository.findOne(accountPaymentInstrumentIdentifier);
	}
}