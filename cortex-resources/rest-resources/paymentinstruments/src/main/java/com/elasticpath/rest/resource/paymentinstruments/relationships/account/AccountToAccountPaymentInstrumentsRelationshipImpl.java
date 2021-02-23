/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentInstrumentsIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsForAccountRelationship;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Account to Payment Instruments Link.
 */
public class AccountToAccountPaymentInstrumentsRelationshipImpl implements AccountPaymentInstrumentsForAccountRelationship.LinkTo {

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
	 * @param accountIdentifier the account identifier
	 * @param repository        the repository
	 */
	@Inject
	public AccountToAccountPaymentInstrumentsRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier,
															  @ResourceRepository final Repository<AccountEntity, AccountIdentifier> repository) {
		this.accountIdentifier = accountIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountPaymentInstrumentsIdentifier> onLinkTo() {
		IdentifierPart<String> accountId = accountIdentifier.getAccountId();
		IdentifierPart<String> scope = accountIdentifier.getAccounts().getScope();

		return Observable.just(buildAccountPaymentInstrumentsIdentifier(scope, accountId));
	}
}
