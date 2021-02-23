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
 * Create the relationship from the Account Payment Instruments to Payment Instrument Default Selector.
 * Implements {@link AccountDefaultPaymentInstrumentSelectorForAccountRelationship.LinkTo}.
 */
public class AccountDefaultPaymentInstrumentsToAccountPaymentInstrumentDefaultSelectorRelationshipImpl implements
		AccountDefaultPaymentInstrumentSelectorForAccountRelationship.LinkTo {

	private final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentsIdentifier Account Payment Instrument Identifier.
	 */
	@Inject
	public AccountDefaultPaymentInstrumentsToAccountPaymentInstrumentDefaultSelectorRelationshipImpl(
			@RequestIdentifier final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier) {
		this.accountPaymentInstrumentsIdentifier = accountPaymentInstrumentsIdentifier;
	}

	@Override
	public Observable<AccountDefaultPaymentInstrumentSelectorIdentifier> onLinkTo() {
		return Observable.just(AccountDefaultPaymentInstrumentSelectorIdentifier.builder()
				.withAccountPaymentInstruments(accountPaymentInstrumentsIdentifier)
				.build());
	}
}
