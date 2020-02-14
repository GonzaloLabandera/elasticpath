/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement the handling of the selector for the Profile Payment Instrument.
 * Implements {@link ProfileDefaultPaymentInstrumentSelectorResource.Choices}.
 */
public class ProfileDefaultPaymentInstrumentSelectorChoicesPrototype implements ProfileDefaultPaymentInstrumentSelectorResource.Choices {

	private final ProfileDefaultPaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier;
	private final SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier,
			ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param profilePaymentInstrumentSelectorIdentifier the Profile Payment Instrument Selector Identifier.
	 * @param selectorRepository                         the Profile Payment Instrument Selector repository.
	 */
	@Inject
	public ProfileDefaultPaymentInstrumentSelectorChoicesPrototype(
			@RequestIdentifier final ProfileDefaultPaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier,
			@ResourceRepository final SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier,
					ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository) {
		this.profilePaymentInstrumentSelectorIdentifier = profilePaymentInstrumentSelectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(profilePaymentInstrumentSelectorIdentifier);
	}
}
