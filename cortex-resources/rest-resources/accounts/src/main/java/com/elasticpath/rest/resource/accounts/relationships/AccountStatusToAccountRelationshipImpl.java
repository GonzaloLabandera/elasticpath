/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountStatusIdentifier;
import com.elasticpath.rest.definition.accounts.AccountStatusToAccountRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account status to account link.
 */
public class AccountStatusToAccountRelationshipImpl implements AccountStatusToAccountRelationship.LinkTo {
	private final AccountStatusIdentifier accountStatusIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountStatusIdentifier accountStatusIdentifier
	 */
	@Inject
	public AccountStatusToAccountRelationshipImpl(@RequestIdentifier final AccountStatusIdentifier accountStatusIdentifier) {
		this.accountStatusIdentifier = accountStatusIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(accountStatusIdentifier.getAccount());
	}
}
