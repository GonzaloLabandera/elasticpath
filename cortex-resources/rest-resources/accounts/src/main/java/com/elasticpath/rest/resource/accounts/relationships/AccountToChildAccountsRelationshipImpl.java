/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountToChildAccountsRelationship;
import com.elasticpath.rest.definition.accounts.ChildAccountsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account to child accounts link.
 */
public class AccountToChildAccountsRelationshipImpl implements AccountToChildAccountsRelationship.LinkTo {
	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 */
	@Inject
	public AccountToChildAccountsRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}


	@Override
	public Observable<ChildAccountsIdentifier> onLinkTo() {
		return Observable.just(ChildAccountsIdentifier.builder().withAccount(accountIdentifier).build());

	}
}
