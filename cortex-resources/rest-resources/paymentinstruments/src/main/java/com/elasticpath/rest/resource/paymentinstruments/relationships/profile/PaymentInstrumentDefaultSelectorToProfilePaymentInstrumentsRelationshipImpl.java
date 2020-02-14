/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorForProfileRelationship;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Payment Instrument Default Selector to Profile Payment Instruments.
 * Implements {@link ProfileDefaultPaymentInstrumentSelectorForProfileRelationship.LinkFrom}.
 */
public class PaymentInstrumentDefaultSelectorToProfilePaymentInstrumentsRelationshipImpl implements
		ProfileDefaultPaymentInstrumentSelectorForProfileRelationship.LinkFrom {

	private final ProfileDefaultPaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param profilePaymentInstrumentSelectorIdentifier Profile Payment Instrument selector Identifier.
	 */
	@Inject
	public PaymentInstrumentDefaultSelectorToProfilePaymentInstrumentsRelationshipImpl(
			@RequestIdentifier final ProfileDefaultPaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier) {
		this.profilePaymentInstrumentSelectorIdentifier = profilePaymentInstrumentSelectorIdentifier;
	}

	@Override
	public Observable<PaymentInstrumentsIdentifier> onLinkFrom() {
		return Observable.just(profilePaymentInstrumentSelectorIdentifier.getPaymentInstruments());
	}
}
