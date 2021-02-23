/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountAttributesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountAttributesToAccountRelationship;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account attributes to account link.
 */
public class AccountAttributesToAccountRelationshipImpl implements AccountAttributesToAccountRelationship.LinkTo {

	private final AccountAttributesIdentifier accountAttributesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAttributesIdentifier accountAttributesIdentifier
	 */
	@Inject
	public AccountAttributesToAccountRelationshipImpl(@RequestIdentifier final AccountAttributesIdentifier accountAttributesIdentifier) {
		this.accountAttributesIdentifier = accountAttributesIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(accountAttributesIdentifier.getAccount());
	}


}
