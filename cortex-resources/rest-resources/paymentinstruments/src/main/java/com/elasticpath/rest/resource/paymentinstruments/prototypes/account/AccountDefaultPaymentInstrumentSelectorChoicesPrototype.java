/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement the handling of the selector for the Account Payment Instrument.
 * Implements {@link AccountDefaultPaymentInstrumentSelectorResource.Choices}.
 */
public class AccountDefaultPaymentInstrumentSelectorChoicesPrototype implements AccountDefaultPaymentInstrumentSelectorResource.Choices {

	private final AccountDefaultPaymentInstrumentSelectorIdentifier accountDefaultPaymentInstrumentSelectorIdentifier;
	private final SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier,
			AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param accountDefaultPaymentInstrumentSelectorIdentifier the Account Payment Instrument Selector Identifier.
	 * @param selectorRepository                                the Account Payment Instrument Selector repository.
	 */
	@Inject
	public AccountDefaultPaymentInstrumentSelectorChoicesPrototype(
			@RequestIdentifier final AccountDefaultPaymentInstrumentSelectorIdentifier accountDefaultPaymentInstrumentSelectorIdentifier,
			@ResourceRepository final SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier,
					AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository) {
		this.accountDefaultPaymentInstrumentSelectorIdentifier = accountDefaultPaymentInstrumentSelectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(accountDefaultPaymentInstrumentSelectorIdentifier);
	}
}
