/*
 * Copyright (c) Elastic Path Software Inc., 2020
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
import com.elasticpath.rest.selector.SelectResult;

/**
 * Profile Payment Instrument when the user chooses an item.
 * Implements {@link ProfileDefaultPaymentInstrumentSelectorChoiceResource.SelectWithResult}.
 */
public class ProfileDefaultPaymentInstrumentSelectorChoiceSelectPrototype
		implements ProfileDefaultPaymentInstrumentSelectorChoiceResource.SelectWithResult {

	private final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;
	private final SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier,
			ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorChoiceIdentifier Profile Payment Instrument identifier.
	 * @param selectorRepository       the Profile Payment Instrument Selector repository.
	 */
	@Inject
	public ProfileDefaultPaymentInstrumentSelectorChoiceSelectPrototype(
			@RequestIdentifier final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier,
			@ResourceRepository final SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier,
					ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository
	) {
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<SelectResult<ProfileDefaultPaymentInstrumentSelectorIdentifier>> onSelectWithResult() {
		return selectorRepository.selectChoice(selectorChoiceIdentifier);
	}
}
