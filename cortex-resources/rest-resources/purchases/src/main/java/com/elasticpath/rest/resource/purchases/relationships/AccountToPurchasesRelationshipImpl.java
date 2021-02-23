/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.purchases.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.purchases.AccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.AccountToPurchasesRelationship;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;


/**
 * Account purchases from Account link.
 */
public class AccountToPurchasesRelationshipImpl implements AccountToPurchasesRelationship.LinkTo {

	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 */
	@Inject
	public AccountToPurchasesRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	@Override
	public Observable<AccountPurchasesIdentifier> onLinkTo() {
		return Observable.just(AccountPurchasesIdentifier.builder()
				.withAccountId(accountIdentifier.getAccountId())
				.withPurchases(PurchasesIdentifier.builder()
						.withScope(accountIdentifier.getAccounts().getScope())
						.build())
				.build());
	}
}
