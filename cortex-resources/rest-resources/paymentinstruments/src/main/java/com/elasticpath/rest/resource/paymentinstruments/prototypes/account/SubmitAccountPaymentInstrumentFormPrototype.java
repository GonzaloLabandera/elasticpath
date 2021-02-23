/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormResource;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Prototype for submission of Account-Payment Instrument form.
 */
public class SubmitAccountPaymentInstrumentFormPrototype implements AccountPaymentInstrumentFormResource.SubmitWithResult {

	private final PaymentInstrumentForFormEntity formEntity;
	private final AccountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier;
	private final PaymentInstrumentRepository repository;

	/**
	 * Constructor.
	 *
	 * @param formEntity                             form entity
	 * @param accountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier
	 * @param repository                             payment instrument repository
	 */
	@Inject
	public SubmitAccountPaymentInstrumentFormPrototype(
			@RequestForm final PaymentInstrumentForFormEntity formEntity,
			@RequestIdentifier final AccountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier,
			@ResourceRepository final PaymentInstrumentRepository repository) {
		this.formEntity = formEntity;
		this.accountPaymentInstrumentFormIdentifier = accountPaymentInstrumentFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<AccountPaymentInstrumentIdentifier>> onSubmitWithResult() {
		return repository.submitAccountPaymentInstrument(
				accountPaymentInstrumentFormIdentifier.getAccountPaymentMethod().getAccountPaymentMethods().getAccount().getAccounts().getScope(),
				formEntity);
	}
}
