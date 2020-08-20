/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.SharedAccountIdIdentifier;
import com.elasticpath.rest.definition.accounts.SharedIdentifierFromAccountRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Shared Account Id From Account Relationship.
 */
public class SharedAccountIdFromAccountRelationshipImpl implements SharedIdentifierFromAccountRelationship.LinkTo {
	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 */
	@Inject
	public SharedAccountIdFromAccountRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	@Override
	public Observable<SharedAccountIdIdentifier> onLinkTo() {
		return Observable.just(SharedAccountIdIdentifier.builder()
				.withAccount(accountIdentifier)
				.build());
	}
}
