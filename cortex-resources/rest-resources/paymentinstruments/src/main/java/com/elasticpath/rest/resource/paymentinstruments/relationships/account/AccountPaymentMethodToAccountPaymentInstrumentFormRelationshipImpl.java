/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentInstrumentFormIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormForAccountPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Payment Method to Payment Instrument Form link.
 */
public class AccountPaymentMethodToAccountPaymentInstrumentFormRelationshipImpl
		implements AccountPaymentInstrumentFormForAccountPaymentMethodRelationship.LinkTo {

	private final AccountPaymentMethodIdentifier accountPaymentMethodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentMethodIdentifier {@link AccountPaymentMethodIdentifier}
	 */
	@Inject
	public AccountPaymentMethodToAccountPaymentInstrumentFormRelationshipImpl(
			@RequestIdentifier final AccountPaymentMethodIdentifier accountPaymentMethodIdentifier) {
		this.accountPaymentMethodIdentifier = accountPaymentMethodIdentifier;
	}

	@Override
	public Observable<AccountPaymentInstrumentFormIdentifier> onLinkTo() {
		return Observable.just(buildAccountPaymentInstrumentFormIdentifier(accountPaymentMethodIdentifier));
	}
}
