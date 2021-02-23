/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstructions.relationships.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountRequestInstructionsForm;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormForAccountPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Payment Methods to orders link.
 */
public class AccountPaymentMethodToAccountRequestInstructionsFormRelationshipImpl
		implements AccountRequestInstructionsFormForAccountPaymentMethodRelationship.LinkTo {

	private final AccountPaymentMethodIdentifier paymentMethod;

	/**
	 * Constructor.
	 *
	 * @param paymentMethod {@link AccountPaymentMethodIdentifier}
	 */
	@Inject
	public AccountPaymentMethodToAccountRequestInstructionsFormRelationshipImpl(
			@RequestIdentifier final AccountPaymentMethodIdentifier paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	@Override
	public Observable<AccountRequestInstructionsFormIdentifier> onLinkTo() {
		AccountIdentifier accountIdentifier = paymentMethod.getAccountPaymentMethods().getAccount();
		IdentifierPart<String> paymentMethodId = paymentMethod.getAccountPaymentMethodId();

		return Observable.just(buildAccountRequestInstructionsForm(accountIdentifier, paymentMethodId));
	}
}
