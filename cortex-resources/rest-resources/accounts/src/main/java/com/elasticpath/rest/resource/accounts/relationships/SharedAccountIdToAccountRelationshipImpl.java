/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.SharedAccountIdIdentifier;
import com.elasticpath.rest.definition.accounts.SharedIdentifierToAccountRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account shared id to account link.
 */
public class SharedAccountIdToAccountRelationshipImpl implements SharedIdentifierToAccountRelationship.LinkTo {
	private final SharedAccountIdIdentifier sharedAccountIdIdentifier;

	/**
	 * Constructor.
	 *
	 * @param sharedAccountIdIdentifier sharedAccountIdIdentifier
	 */
	@Inject
	public SharedAccountIdToAccountRelationshipImpl(@RequestIdentifier final SharedAccountIdIdentifier sharedAccountIdIdentifier) {
		this.sharedAccountIdIdentifier = sharedAccountIdIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(sharedAccountIdIdentifier.getAccount());
	}
}
