/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountFromAccountAddressesRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account addresses to account link.
 */
public class AccountFromAccountAddressesRelationshipImpl implements AccountFromAccountAddressesRelationship.LinkTo {

	private final AccountAddressesIdentifier accountAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAddressesIdentifier addressesIdentifier
	 */
	@Inject
	public AccountFromAccountAddressesRelationshipImpl(@RequestIdentifier final AccountAddressesIdentifier accountAddressesIdentifier) {
		this.accountAddressesIdentifier = accountAddressesIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(AccountIdentifier.builder()
				.withAccountId(accountAddressesIdentifier.getAccountId())
				.withAccounts(AccountsIdentifier.builder()
						.withScope(accountAddressesIdentifier.getAddresses().getScope()).build())
				.build());
	}
}
