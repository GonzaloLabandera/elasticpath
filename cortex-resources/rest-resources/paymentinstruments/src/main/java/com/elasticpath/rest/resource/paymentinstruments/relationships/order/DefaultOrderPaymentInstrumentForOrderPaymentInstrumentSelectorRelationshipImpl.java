/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.DefaultPaymentInstrumentForOrderPaymentInstrumentSelectorRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.DefaultPaymentInstrumentLinkRepository;

/**
 * Payment Instrument Selector link to default payment instrument.
 */
public class DefaultOrderPaymentInstrumentForOrderPaymentInstrumentSelectorRelationshipImpl
		implements DefaultPaymentInstrumentForOrderPaymentInstrumentSelectorRelationship.LinkTo {

	private final IdentifierPart<String> scope;
	private final DefaultPaymentInstrumentLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param scope      scope
	 * @param repository default payment instrument link repository
	 */
	@Inject
	public DefaultOrderPaymentInstrumentForOrderPaymentInstrumentSelectorRelationshipImpl(
			@UriPart(ProfileIdentifier.SCOPE) final IdentifierPart<String> scope,
			@ResourceRepository final DefaultPaymentInstrumentLinkRepository repository) {
		this.scope = scope;
		this.repository = repository;
	}

	@Override
	public Observable<PaymentInstrumentIdentifier> onLinkTo() {
		return repository.getDefaultPaymentInstrumentIdentifier(PaymentInstrumentsIdentifier.builder().withScope(scope).build());
	}

}
