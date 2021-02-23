/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentToAccountPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentToPaymentMethodLinkRepository;

/**
 * Payment Instrument to Account Payment Method link.
 */
public class AccountPaymentInstrumentToAccountPaymentMethodRelationshipImpl
		implements AccountPaymentInstrumentToAccountPaymentMethodRelationship.LinkTo {

	private final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier;
	private final PaymentInstrumentToPaymentMethodLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentIdentifier payment instrument identifier.
	 * @param repository                         repository
	 */
	@Inject
	public AccountPaymentInstrumentToAccountPaymentMethodRelationshipImpl(
			@RequestIdentifier final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier,
			@ResourceRepository final PaymentInstrumentToPaymentMethodLinkRepository repository) {
		this.accountPaymentInstrumentIdentifier = accountPaymentInstrumentIdentifier;
		this.repository = repository;
	}

	/**
	 * Link generator.
	 *
	 * @return ProfilePaymentMethodIdentifier.
	 */
	@Override
	public Observable<AccountPaymentMethodIdentifier> onLinkTo() {
		String accountId = accountPaymentInstrumentIdentifier
				.getAccountPaymentInstruments()
				.getAccount()
				.getAccountId().getValue();

		return repository.getAccountPaymentMethodIdentifier(accountId, accountPaymentInstrumentIdentifier);
	}
}
