/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsForAccountRelationship;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account to account payment methods link.
 */
public class AccountToAccountPaymentMethodsRelationshipImpl implements AccountPaymentMethodsForAccountRelationship.LinkTo {

	private final AccountIdentifier accountIdentifier;

	/*
This unused field is required to access the accountEntityPurchaseRepository in AccountIdParameterStrategy.
Note: Injecting an OSGi service in non-prototype classes (e.g. a PermissionParameterStrategy) with @ResourceRepository or @ResourceService
will not work unless the services are already injected in a prototype class.  See "Data Injectors" in the cortex documentation.
*/
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final Repository<AccountEntity, AccountIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 * @param repository        the repository
	 */
	@Inject
	public AccountToAccountPaymentMethodsRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier,
														  @ResourceRepository final Repository<AccountEntity, AccountIdentifier> repository) {
		this.accountIdentifier = accountIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountPaymentMethodsIdentifier> onLinkTo() {
		return Observable.just(AccountPaymentMethodsIdentifier.builder().withAccount(accountIdentifier).build());
	}

}
