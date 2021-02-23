/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceToChoicesRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Default Payment Instrument Selector Choice link back to choices.
 */
public class AccountDefaultPaymentInstrumentSelectorChoiceToChoicesRelationshipImpl implements
		AccountDefaultPaymentInstrumentSelectorChoiceToChoicesRelationship.LinkTo {

	private final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;

	/**
	 * Constructor.
	 *
	 * @param selectorChoiceIdentifier selector choice identifier.
	 */
	@Inject
	public AccountDefaultPaymentInstrumentSelectorChoiceToChoicesRelationshipImpl(
			@RequestIdentifier final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
	}

	@Override
	public Observable<AccountDefaultPaymentInstrumentSelectorIdentifier> onLinkTo() {
		return Observable.just(selectorChoiceIdentifier.getAccountDefaultPaymentInstrumentSelector());
	}
}
