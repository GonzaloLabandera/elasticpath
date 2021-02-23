/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstructions.relationships.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentMethodIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsToAccountPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Payment Request Instructions Form to Account Payment Method link.
 */
public class AccountPaymentInstructionsToAccountPaymentMethodRelationshipImpl
		implements AccountPaymentInstructionsToAccountPaymentMethodRelationship.LinkTo {

	private final AccountPaymentInstructionsIdentifier instructionsIdentifier;
	/*
This unused field is required to access the accountEntityPurchaseRepository in AccountIdParameterStrategy.
Note: Injecting an OSGi service in non-prototype classes (e.g. a PermissionParameterStrategy) with @ResourceRepository or @ResourceService
will not work unless the services are already injected in a prototype class.  See "Data Injectors" in the cortex documentation.
*/
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final Repository<AccountEntity, AccountIdentifier> accountEntityPurchaseRepository;

	/**
	 * Constructor.
	 *
	 * @param instructionsIdentifier          instructionsIdentifier
	 * @param accountEntityPurchaseRepository account entity purchase repository
	 */
	@Inject
	public AccountPaymentInstructionsToAccountPaymentMethodRelationshipImpl(
			@RequestIdentifier final AccountPaymentInstructionsIdentifier instructionsIdentifier,
			@ResourceRepository final Repository<AccountEntity, AccountIdentifier> accountEntityPurchaseRepository) {
		this.instructionsIdentifier = instructionsIdentifier;

		this.accountEntityPurchaseRepository = accountEntityPurchaseRepository;
	}

	@Override
	public Observable<AccountPaymentMethodIdentifier> onLinkTo() {
		return Observable.just(buildAccountPaymentMethodIdentifier(
				instructionsIdentifier.getAccountPaymentMethod().getAccountPaymentMethods().getAccount(),
				instructionsIdentifier.getAccountPaymentMethod().getAccountPaymentMethodId()));
	}
}
