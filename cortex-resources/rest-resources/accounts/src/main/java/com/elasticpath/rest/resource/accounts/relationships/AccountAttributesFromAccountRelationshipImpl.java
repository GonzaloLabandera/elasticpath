/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountAttributesFromAccountRelationship;
import com.elasticpath.rest.definition.accounts.AccountAttributesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;


/**
 * Account Attributes from Account link.
 */
public class AccountAttributesFromAccountRelationshipImpl implements AccountAttributesFromAccountRelationship.LinkTo {

	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 */
	@Inject
	public AccountAttributesFromAccountRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	@Override
	public Observable<AccountAttributesIdentifier> onLinkTo() {
		return Observable.just(AccountAttributesIdentifier.builder()
				.withAccount(accountIdentifier)
				.build());
	}


}
