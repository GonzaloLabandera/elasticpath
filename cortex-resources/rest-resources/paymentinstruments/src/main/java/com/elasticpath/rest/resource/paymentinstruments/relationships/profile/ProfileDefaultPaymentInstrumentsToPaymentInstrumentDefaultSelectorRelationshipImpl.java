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
 * Create the relationship from the Profile Payment Instruments to Payment Instrument Default Selector.
 * Implements {@link ProfileDefaultPaymentInstrumentSelectorForProfileRelationship.LinkTo}.
 */
public class ProfileDefaultPaymentInstrumentsToPaymentInstrumentDefaultSelectorRelationshipImpl implements
		ProfileDefaultPaymentInstrumentSelectorForProfileRelationship.LinkTo {

	private final PaymentInstrumentsIdentifier paymentInstrumentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentsIdentifier Profile Payment Instrument Identifier.
	 */
	@Inject
	public ProfileDefaultPaymentInstrumentsToPaymentInstrumentDefaultSelectorRelationshipImpl(
			@RequestIdentifier final PaymentInstrumentsIdentifier paymentInstrumentsIdentifier) {
		this.paymentInstrumentsIdentifier = paymentInstrumentsIdentifier;
	}

	@Override
	public Observable<ProfileDefaultPaymentInstrumentSelectorIdentifier> onLinkTo() {
		return Observable.just(ProfileDefaultPaymentInstrumentSelectorIdentifier.builder()
				.withPaymentInstruments(paymentInstrumentsIdentifier)
				.build());
	}
}
