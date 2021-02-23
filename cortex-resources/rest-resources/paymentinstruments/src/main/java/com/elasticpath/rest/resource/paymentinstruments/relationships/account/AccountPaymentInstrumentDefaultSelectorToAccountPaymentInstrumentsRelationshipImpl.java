/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorForAccountRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Payment Instrument Default Selector to Account Payment Instruments.
 * Implements {@link AccountDefaultPaymentInstrumentSelectorForAccountRelationship.LinkFrom}.
 */
public class AccountPaymentInstrumentDefaultSelectorToAccountPaymentInstrumentsRelationshipImpl
		implements AccountDefaultPaymentInstrumentSelectorForAccountRelationship.LinkFrom {

	private final AccountDefaultPaymentInstrumentSelectorIdentifier accountPaymentInstrumentSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentSelectorIdentifier Account Payment Instrument selector Identifier.
	 */
	@Inject
	public AccountPaymentInstrumentDefaultSelectorToAccountPaymentInstrumentsRelationshipImpl(
			@RequestIdentifier final AccountDefaultPaymentInstrumentSelectorIdentifier accountPaymentInstrumentSelectorIdentifier) {
		this.accountPaymentInstrumentSelectorIdentifier = accountPaymentInstrumentSelectorIdentifier;
	}

	@Override
	public Observable<AccountPaymentInstrumentsIdentifier> onLinkFrom() {
		return Observable.just(accountPaymentInstrumentSelectorIdentifier.getAccountPaymentInstruments());
	}
}
