/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountFromAccountAddressRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account address to account link.
 */
public class AccountFromAccountAddressRelationshipImpl implements AccountFromAccountAddressRelationship.LinkTo {

	private final AccountAddressIdentifier accountAddressIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAddressIdentifier addressIdentifier
	 */
	@Inject
	public AccountFromAccountAddressRelationshipImpl(@RequestIdentifier final AccountAddressIdentifier accountAddressIdentifier) {
		this.accountAddressIdentifier = accountAddressIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(AccountIdentifier.builder()
				.withAccountId(accountAddressIdentifier.getAccountAddresses().getAccountId())
				.withAccounts(AccountsIdentifier.builder()
						.withScope(accountAddressIdentifier.getAccountAddresses().getAddresses().getScope()).build())
				.build());
	}
}
