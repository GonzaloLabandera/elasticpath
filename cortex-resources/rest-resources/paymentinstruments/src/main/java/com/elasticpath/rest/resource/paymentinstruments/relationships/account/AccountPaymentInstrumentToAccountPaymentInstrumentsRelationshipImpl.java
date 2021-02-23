/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentToAccountPaymentInstrumentsRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Payment Instrument to Payment Instruments link.
 */
public class AccountPaymentInstrumentToAccountPaymentInstrumentsRelationshipImpl
		implements AccountPaymentInstrumentToAccountPaymentInstrumentsRelationship.LinkTo {

	private final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentIdentifier the payment instrument identifier
	 */
	@Inject
	public AccountPaymentInstrumentToAccountPaymentInstrumentsRelationshipImpl(
			@RequestIdentifier final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier) {
		this.accountPaymentInstrumentIdentifier = accountPaymentInstrumentIdentifier;
	}

	@Override
	public Observable<AccountPaymentInstrumentsIdentifier> onLinkTo() {
		return Observable.just(accountPaymentInstrumentIdentifier.getAccountPaymentInstruments());
	}
}
