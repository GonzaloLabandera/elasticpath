/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormForAccountPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Payment Method to Payment Instrument Form link.
 */
public class AccountPaymentInstrumentFormForAccountPaymentMethodRelationshipImpl
		implements AccountPaymentInstrumentFormForAccountPaymentMethodRelationship.LinkTo {

	private final AccountPaymentMethodIdentifier accountPaymentMethodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentMethodIdentifier {@link AccountPaymentMethodIdentifier}
	 */
	@Inject
	public AccountPaymentInstrumentFormForAccountPaymentMethodRelationshipImpl(
			@RequestIdentifier final AccountPaymentMethodIdentifier accountPaymentMethodIdentifier) {
		this.accountPaymentMethodIdentifier = accountPaymentMethodIdentifier;
	}

	@Override
	public Observable<AccountPaymentInstrumentFormIdentifier> onLinkTo() {
		return Observable.just(AccountPaymentInstrumentFormIdentifier.builder()
				.withAccountPaymentMethod(accountPaymentMethodIdentifier)
				.build());
	}
}
