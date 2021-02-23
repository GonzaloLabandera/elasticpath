/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormResource;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Account-Payment Instrument Form.
 */
public class ReadAccountPaymentInstrumentFormPrototype implements AccountPaymentInstrumentFormResource.Read {

	private final AccountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier;
	private final Repository<PaymentInstrumentForFormEntity, AccountPaymentInstrumentFormIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentFormIdentifier identifier
	 * @param repository                             repository
	 */
	@Inject
	public ReadAccountPaymentInstrumentFormPrototype(
			@RequestIdentifier final AccountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentForFormEntity, AccountPaymentInstrumentFormIdentifier> repository) {
		this.accountPaymentInstrumentFormIdentifier = accountPaymentInstrumentFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentInstrumentForFormEntity> onRead() {
		return repository.findOne(accountPaymentInstrumentFormIdentifier);
	}
}
