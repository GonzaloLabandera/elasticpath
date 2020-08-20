/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountsFromProfileRelationship;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Profile to accounts link.
 */
public class AccountsFromProfileRelationshipImpl implements AccountsFromProfileRelationship.LinkTo {

	private final ProfileIdentifier profileIdentifier;

	/**
	 * Constructor.
	 *
	 * @param profileIdentifier the profile identifier
	 */
	@Inject
	public AccountsFromProfileRelationshipImpl(@RequestIdentifier final ProfileIdentifier profileIdentifier) {
		this.profileIdentifier = profileIdentifier;
	}

	@Override
	public Observable<AccountsIdentifier> onLinkTo() {
		return Observable.just(AccountsIdentifier.builder().withScope(profileIdentifier.getScope())
				.build());
	}
}
