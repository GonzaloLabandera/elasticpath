/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentResource;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Account Payment Instrument - Delete Operation.
 */
public class DeleteAccountPaymentInstrumentPrototype implements AccountPaymentInstrumentResource.Delete {

	private final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier;
	private final Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentIdentifier identifier
	 * @param repository                         repository
	 */
	@Inject
	public DeleteAccountPaymentInstrumentPrototype(
			@RequestIdentifier final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> repository) {
		this.accountPaymentInstrumentIdentifier = accountPaymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onDelete() {
		return repository.delete(accountPaymentInstrumentIdentifier);
	}
}
