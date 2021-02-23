/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.account;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsForAccountRelationship;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Payment methods to account link.
 */
public class AccountPaymentMethodsToAccountRelationshipImpl implements AccountPaymentMethodsForAccountRelationship.LinkFrom {
	private final AccountPaymentMethodsIdentifier accountPaymentMethodsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentMethodsIdentifier accountPaymentMethodsIdentifier
	 */
	@Inject
	public AccountPaymentMethodsToAccountRelationshipImpl(@RequestIdentifier final AccountPaymentMethodsIdentifier accountPaymentMethodsIdentifier) {
		this.accountPaymentMethodsIdentifier = accountPaymentMethodsIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkFrom() {
		return Observable.just(accountPaymentMethodsIdentifier.getAccount());
	}
}
