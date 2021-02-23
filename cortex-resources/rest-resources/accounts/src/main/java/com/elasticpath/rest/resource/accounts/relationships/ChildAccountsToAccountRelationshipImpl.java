/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.ChildAccountsToAccountRelationship;
import com.elasticpath.rest.definition.accounts.PaginatedChildAccountsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Child accounts to account link.
 */
public class ChildAccountsToAccountRelationshipImpl implements ChildAccountsToAccountRelationship.LinkTo {
	private final PaginatedChildAccountsIdentifier childAccountsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param childAccountsIdentifier childAccountsIdentifier
	 */
	@Inject
	public ChildAccountsToAccountRelationshipImpl(@RequestIdentifier final PaginatedChildAccountsIdentifier childAccountsIdentifier) {
		this.childAccountsIdentifier = childAccountsIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(childAccountsIdentifier.getChildAccounts().getAccount());
	}
}
