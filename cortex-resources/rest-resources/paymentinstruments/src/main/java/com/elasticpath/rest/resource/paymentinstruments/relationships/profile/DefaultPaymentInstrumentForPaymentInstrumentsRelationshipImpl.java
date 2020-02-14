/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.DefaultPaymentInstrumentForPaymentInstrumentsRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.DefaultPaymentInstrumentLinkRepository;

/**
 * Payment Instruments link to default payment instrument.
 */
public class DefaultPaymentInstrumentForPaymentInstrumentsRelationshipImpl
		implements DefaultPaymentInstrumentForPaymentInstrumentsRelationship.LinkTo {

	private final PaymentInstrumentsIdentifier identifier;
	private final DefaultPaymentInstrumentLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param identifier payment instruments identifier
	 * @param repository default payment instrument link repository
	 */
	@Inject
	public DefaultPaymentInstrumentForPaymentInstrumentsRelationshipImpl(
			@RequestIdentifier final PaymentInstrumentsIdentifier identifier,
			@ResourceRepository final DefaultPaymentInstrumentLinkRepository repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<PaymentInstrumentIdentifier> onLinkTo() {
		return repository.getDefaultPaymentInstrumentIdentifier(identifier);
	}

}
