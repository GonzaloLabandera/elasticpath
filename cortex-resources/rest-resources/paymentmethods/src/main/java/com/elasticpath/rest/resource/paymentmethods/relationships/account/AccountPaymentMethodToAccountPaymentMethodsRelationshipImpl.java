/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsForAccountPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account payment method to account payment method link.
 */
public class AccountPaymentMethodToAccountPaymentMethodsRelationshipImpl implements AccountPaymentMethodsForAccountPaymentMethodRelationship.LinkTo {
	private final AccountPaymentMethodIdentifier accountPaymentMethodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentMethodIdentifier accountPaymentMethodIdentifier
	 */
	@Inject
	public AccountPaymentMethodToAccountPaymentMethodsRelationshipImpl(@RequestIdentifier final AccountPaymentMethodIdentifier
																			   accountPaymentMethodIdentifier) {
		this.accountPaymentMethodIdentifier = accountPaymentMethodIdentifier;
	}

	@Override
	public Observable<AccountPaymentMethodsIdentifier> onLinkTo() {
		return Observable.just(accountPaymentMethodIdentifier.getAccountPaymentMethods());
	}
}
