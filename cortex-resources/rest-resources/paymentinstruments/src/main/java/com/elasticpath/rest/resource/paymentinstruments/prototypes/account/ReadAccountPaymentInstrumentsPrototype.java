/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsResource;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Prototype for order-payment instruments read operation.
 */
public class ReadAccountPaymentInstrumentsPrototype implements AccountPaymentInstrumentsResource.Read {

	/*
This unused field is required to access the accountEntityPurchaseRepository in AccountIdParameterStrategy.
Note: Injecting an OSGi service in non-prototype classes (e.g. a PermissionParameterStrategy) with @ResourceRepository or @ResourceService
will not work unless the services are already injected in a prototype class.  See "Data Injectors" in the cortex documentation.
*/
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final Repository<AccountEntity, AccountIdentifier> accountEntityRepository;

	@SuppressWarnings("PMD.UnusedPrivateField")
	private final PaymentInstrumentRepository paymentInstrumentRepository;

	private final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier;

	private final Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentsIdentifier identifier
	 * @param repository                          repository
	 * @param accountEntityRepository             accountEntityRepository
	 * @param paymentInstrumentRepository         paymentInstrumentRepository
	 */
	@Inject
	public ReadAccountPaymentInstrumentsPrototype(
			@RequestIdentifier final AccountPaymentInstrumentsIdentifier accountPaymentInstrumentsIdentifier,
			@ResourceRepository final Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> repository,
			@ResourceRepository final Repository<AccountEntity, AccountIdentifier> accountEntityRepository,
			@ResourceService final PaymentInstrumentRepository paymentInstrumentRepository) {
		this.accountPaymentInstrumentsIdentifier = accountPaymentInstrumentsIdentifier;
		this.repository = repository;
		this.accountEntityRepository = accountEntityRepository;
		this.paymentInstrumentRepository = paymentInstrumentRepository;
	}

	@Override
	public Observable<AccountPaymentInstrumentIdentifier> onRead() {
		return repository.findAll(accountPaymentInstrumentsIdentifier.getAccount().getAccounts().getScope());
	}
}
