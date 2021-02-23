/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceResource;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * When the Account Payment Instrument Selector Choice is selected, return data about the Account Payment Instrument.
 * Implements {@link AccountDefaultPaymentInstrumentSelectorChoiceResource.Read}.
 */
public class AccountDefaultPaymentInstrumentSelectorChoiceReadPrototype implements AccountDefaultPaymentInstrumentSelectorChoiceResource.Read {

	private final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;
	private final SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier,
			AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository       the Account Payment Instrument Selector repository.
	 * @param selectorChoiceIdentifier Account Payment Instrument Selector Choice Identifier.
	 */
	@Inject
	public AccountDefaultPaymentInstrumentSelectorChoiceReadPrototype(
			@ResourceRepository final SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier,
					AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		this.selectorRepository = selectorRepository;
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
	}

	@Override
	public Single<Choice> onRead() {
		return selectorRepository.getChoice(selectorChoiceIdentifier);
	}
}
