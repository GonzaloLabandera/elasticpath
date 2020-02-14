/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormResource;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Prototype for submission of Profile-Payment Instrument form.
 */
public class SubmitProfilePaymentInstrumentFormPrototype implements ProfilePaymentInstrumentFormResource.SubmitWithResult {

	private final PaymentInstrumentForFormEntity formEntity;
	private final ProfilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier;
	private final PaymentInstrumentRepository repository;

	/**
	 * Constructor.
	 *
	 * @param formEntity form entity
	 * @param profilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier
	 * @param repository payment instrument repository
	 */
	@Inject
	public SubmitProfilePaymentInstrumentFormPrototype(
			@RequestForm final PaymentInstrumentForFormEntity formEntity,
			@RequestIdentifier final ProfilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier,
			@ResourceRepository final PaymentInstrumentRepository repository) {
		this.formEntity = formEntity;
		this.profilePaymentInstrumentFormIdentifier = profilePaymentInstrumentFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<PaymentInstrumentIdentifier>> onSubmitWithResult() {
		return repository.submitProfilePaymentInstrument(
				profilePaymentInstrumentFormIdentifier.getProfilePaymentMethod().getProfilePaymentMethods().getProfile().getScope(),
				formEntity);
	}
}
