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
public class AssociatesToAccountRelationshipImpl implements AssociatesForAccountRelationship.LinkFrom {
	private final AssociatesIdentifier associatesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associatesIdentifier associatesIdentifier
	 */
	@Inject
	public AssociatesToAccountRelationshipImpl(@RequestIdentifier final AssociatesIdentifier associatesIdentifier) {
		this.associatesIdentifier = associatesIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkFrom() {
		return Observable.just(associatesIdentifier.getAccount());
	}
}