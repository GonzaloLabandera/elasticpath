/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesForAccountRelationship;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;


/**
 * Link from offer search result to facets.
 */
public class AccountToAssociatesRelationshipImpl implements AssociatesForAccountRelationship.LinkTo {

	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 * @param accountIdentifier identifier
	 */
	@Inject
	public AccountToAssociatesRelationshipImpl(
			@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	@Override
	public Observable<AssociatesIdentifier> onLinkTo() {
		return Observable.just(AssociatesIdentifier.builder()
				.withAccount(accountIdentifier).build());
	}
}
