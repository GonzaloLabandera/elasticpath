/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentForAccountPaymentInstrumentsRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.DefaultPaymentInstrumentLinkRepository;

/**
 * Payment Instruments link to default payment instrument.
 */
public class AccountDefaultPaymentInstrumentForAccountPaymentInstrumentsRelationshipImpl
		implements AccountDefaultPaymentInstrumentForAccountPaymentInstrumentsRelationship.LinkTo {

	private final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier;
	private final DefaultPaymentInstrumentLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentsIdentifier payment instruments identifier
	 * @param repository                          default payment instrument link repository
	 */
	@Inject
	public AccountDefaultPaymentInstrumentForAccountPaymentInstrumentsRelationshipImpl(
			@RequestIdentifier final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier,
			@ResourceRepository final DefaultPaymentInstrumentLinkRepository repository) {
		this.accountPaymentInstrumentsIdentifier = accountPaymentInstrumentsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountPaymentInstrumentIdentifier> onLinkTo() {

		return repository.getAccountDefaultPaymentInstrumentIdentifier(accountPaymentInstrumentsIdentifier);
	}

}
