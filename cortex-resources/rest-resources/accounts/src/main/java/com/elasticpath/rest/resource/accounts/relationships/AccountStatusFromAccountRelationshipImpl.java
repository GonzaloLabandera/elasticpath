/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountStatusFromAccountRelationship;
import com.elasticpath.rest.definition.accounts.AccountStatusIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Status From Account Relationship.
 */
public class AccountStatusFromAccountRelationshipImpl implements AccountStatusFromAccountRelationship.LinkTo {
	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 */
	@Inject
	public AccountStatusFromAccountRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	@Override
	public Observable<AccountStatusIdentifier> onLinkTo() {
		return Observable.just(AccountStatusIdentifier.builder()
				.withAccount(accountIdentifier)
				.build());
	}
}
