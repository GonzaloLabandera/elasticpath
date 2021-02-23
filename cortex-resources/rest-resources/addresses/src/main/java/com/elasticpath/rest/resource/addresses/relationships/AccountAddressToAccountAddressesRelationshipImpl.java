/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesFromAccountAddressRelationship;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account address to account addresses link.
 */
public class AccountAddressToAccountAddressesRelationshipImpl implements AccountAddressesFromAccountAddressRelationship.LinkTo {

	private final AccountAddressIdentifier accountAddressIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAddressIdentifier accountAddressIdentifier
	 */
	@Inject
	public AccountAddressToAccountAddressesRelationshipImpl(@RequestIdentifier final AccountAddressIdentifier accountAddressIdentifier) {
		this.accountAddressIdentifier = accountAddressIdentifier;
	}

	@Override
	public Observable<AccountAddressesIdentifier> onLinkTo() {
		return Observable.just(accountAddressIdentifier.getAccountAddresses());
	}
}
