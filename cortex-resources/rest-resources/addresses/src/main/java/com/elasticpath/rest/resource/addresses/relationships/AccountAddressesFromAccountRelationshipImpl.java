/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesFromAccountRelationship;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account addresses from account link.
 */
public class AccountAddressesFromAccountRelationshipImpl implements AccountAddressesFromAccountRelationship.LinkTo {


	private final AccountIdentifier accountIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier account identifier
	 */
	@Inject
	public AccountAddressesFromAccountRelationshipImpl(@RequestIdentifier final AccountIdentifier accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	@Override
	public Observable<AccountAddressesIdentifier> onLinkTo() {
		return Observable.just(AccountAddressesIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(accountIdentifier.getAccounts().getScope())
						.build())
				.withAccountId(accountIdentifier.getAccountId()).build());
	}
}
