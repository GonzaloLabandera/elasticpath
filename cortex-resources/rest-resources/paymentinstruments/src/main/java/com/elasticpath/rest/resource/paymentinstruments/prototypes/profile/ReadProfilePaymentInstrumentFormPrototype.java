/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Profile-Payment Instrument Form.
 */
public class ReadProfilePaymentInstrumentFormPrototype implements ProfilePaymentInstrumentFormResource.Read {

	private final ProfilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier;
	private final Repository<PaymentInstrumentForFormEntity, ProfilePaymentInstrumentFormIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param profilePaymentInstrumentFormIdentifier identifier
	 * @param repository                             repository
	 */
	@Inject
	public ReadProfilePaymentInstrumentFormPrototype(
			@RequestIdentifier final ProfilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentForFormEntity, ProfilePaymentInstrumentFormIdentifier> repository) {
		this.profilePaymentInstrumentFormIdentifier = profilePaymentInstrumentFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentInstrumentForFormEntity> onRead() {
		return repository.findOne(profilePaymentInstrumentFormIdentifier);
	}
}
