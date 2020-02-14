/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceToChoicesRelationship;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Profile Default Payment Instrument Selector Choice link back to choices.
 */
public class ProfileDefaultPaymentInstrumentSelectorChoiceToChoicesRelationshipImpl implements
		ProfileDefaultPaymentInstrumentSelectorChoiceToChoicesRelationship.LinkTo {

	private final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;

	/**
	 * Constructor.
	 *
	 * @param selectorChoiceIdentifier selector choice identifier.
	 */
	@Inject
	public ProfileDefaultPaymentInstrumentSelectorChoiceToChoicesRelationshipImpl(
			@RequestIdentifier final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
	}

	@Override
	public Observable<ProfileDefaultPaymentInstrumentSelectorIdentifier> onLinkTo() {
		return Observable.just(selectorChoiceIdentifier.getProfileDefaultPaymentInstrumentSelector());
	}
}
