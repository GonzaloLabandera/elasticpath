/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsResource;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Account Payment Method prototype for Read operation.
 */
public class ReadAccountPaymentMethodsPrototype implements AccountPaymentMethodsResource.Read {

	private final AccountPaymentMethodsIdentifier methodIdentifiers;
	private final Repository<PaymentMethodEntity, AccountPaymentMethodIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param methodsIdentifier the account payment methods identifier
	 * @param repository        repository
	 */
	@Inject
	public ReadAccountPaymentMethodsPrototype(@RequestIdentifier final AccountPaymentMethodsIdentifier methodsIdentifier,
											  @ResourceRepository final Repository<PaymentMethodEntity, AccountPaymentMethodIdentifier> repository) {
		this.methodIdentifiers = methodsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountPaymentMethodIdentifier> onRead() {
		IdentifierPart<String> scope = methodIdentifiers.getAccount().getAccounts().getScope();

		return repository.findAll(scope);
	}
}
