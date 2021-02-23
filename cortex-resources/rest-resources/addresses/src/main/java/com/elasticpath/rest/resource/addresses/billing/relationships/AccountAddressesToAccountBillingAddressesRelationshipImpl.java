/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesFromAccountAddressesRelationship;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Addresses to account billing addresses link.
 */
public class AccountAddressesToAccountBillingAddressesRelationshipImpl implements AccountBillingAddressesFromAccountAddressesRelationship.LinkTo {

	private final AccountAddressesIdentifier accountAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAddressesIdentifier addressesIdentifier
	 */
	@Inject
	public AccountAddressesToAccountBillingAddressesRelationshipImpl(@RequestIdentifier final AccountAddressesIdentifier accountAddressesIdentifier) {
		this.accountAddressesIdentifier = accountAddressesIdentifier;
	}

	@Override
	public Observable<AccountBillingAddressesIdentifier> onLinkTo() {
		return Observable.just(AccountBillingAddressesIdentifier.builder()
				.withAccountAddresses(accountAddressesIdentifier)
				.build());
	}
}
