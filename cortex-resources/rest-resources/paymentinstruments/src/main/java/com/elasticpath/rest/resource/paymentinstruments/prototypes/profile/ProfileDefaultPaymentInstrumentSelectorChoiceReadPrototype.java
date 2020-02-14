/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceResource;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * When the Profile Payment Instrument Selector Choice is selected, return data about the Profile Payment Instrument.
 * Implements {@link ProfileDefaultPaymentInstrumentSelectorChoiceResource.Read}.
 */
public class ProfileDefaultPaymentInstrumentSelectorChoiceReadPrototype implements ProfileDefaultPaymentInstrumentSelectorChoiceResource.Read {

	private final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;
	private final SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier,
			ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository       the Profile Payment Instrument Selector repository.
	 * @param selectorChoiceIdentifier Profile Payment Instrument Selector Choice Identifier.
	 */
	@Inject
	public ProfileDefaultPaymentInstrumentSelectorChoiceReadPrototype(
			@ResourceRepository final SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier,
					ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		this.selectorRepository = selectorRepository;
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
	}

	@Override
	public Single<Choice> onRead() {
		return selectorRepository.getChoice(selectorChoiceIdentifier);
	}
}
