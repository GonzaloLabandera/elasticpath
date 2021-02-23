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
import com.elasticpath.rest.selector.SelectResult;

/**
 * Account Payment Instrument when the user chooses an item.
 * Implements {@link AccountDefaultPaymentInstrumentSelectorChoiceResource.SelectWithResult}.
 */
public class AccountDefaultPaymentInstrumentSelectorChoiceSelectPrototype
		implements AccountDefaultPaymentInstrumentSelectorChoiceResource.SelectWithResult {

	private final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;
	private final SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier,
			AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorChoiceIdentifier Account Payment Instrument identifier.
	 * @param selectorRepository       the Account Payment Instrument Selector repository.
	 */
	@Inject
	public AccountDefaultPaymentInstrumentSelectorChoiceSelectPrototype(
			@RequestIdentifier final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier,
			@ResourceRepository final SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier,
					AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository
	) {
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<SelectResult<AccountDefaultPaymentInstrumentSelectorIdentifier>> onSelectWithResult() {
		return selectorRepository.selectChoice(selectorChoiceIdentifier);
	}
}
