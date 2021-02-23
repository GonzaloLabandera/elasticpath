/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsForAccountRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Payment Instruments to account link.
 */
public class AccountPaymentInstrumentsToAccountRelationship implements AccountPaymentInstrumentsForAccountRelationship.LinkFrom {

	private final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentsIdentifier identifier
	 */
	@Inject
	public AccountPaymentInstrumentsToAccountRelationship(
			@RequestIdentifier final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier) {
		this.accountPaymentInstrumentsIdentifier = accountPaymentInstrumentsIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkFrom() {
		return Observable.just(accountPaymentInstrumentsIdentifier.getAccount());
	}
}
