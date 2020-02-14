/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentToProfilePaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentToPaymentMethodLinkRepository;

/**
 * Payment Instrument to Profile Payment Method link.
 */
public class PaymentInstrumentToProfilePaymentMethodRelationshipImpl implements PaymentInstrumentToProfilePaymentMethodRelationship.LinkTo {

	private final PaymentInstrumentIdentifier paymentInstrumentIdentifier;
	private final String userId;
	private final PaymentInstrumentToPaymentMethodLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentIdentifier payment instrument identifier.
	 * @param userId the user id
	 * @param repository                  repository
	 */
	@Inject
	public PaymentInstrumentToProfilePaymentMethodRelationshipImpl(
			@RequestIdentifier final PaymentInstrumentIdentifier paymentInstrumentIdentifier,
			@UserId final String userId,
			@ResourceRepository final PaymentInstrumentToPaymentMethodLinkRepository repository) {
		this.paymentInstrumentIdentifier = paymentInstrumentIdentifier;
		this.userId = userId;
		this.repository = repository;
	}

	/**
	 * Link generator.
	 *
	 * @return ProfilePaymentMethodIdentifier.
	 */
	@Override
	public Observable<ProfilePaymentMethodIdentifier> onLinkTo() {
		return repository.getProfilePaymentMethodIdentifier(userId, paymentInstrumentIdentifier);
	}
}
