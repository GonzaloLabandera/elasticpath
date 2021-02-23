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
 * Account billing addresses to addresses link.
 */
public class AccountBillingAddressesFromAccountAddressesRelationshipImpl implements AccountBillingAddressesFromAccountAddressesRelationship.LinkFrom {

	private final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountBillingAddressesIdentifier billingAddressesIdentifier
	 */
	@Inject
	public AccountBillingAddressesFromAccountAddressesRelationshipImpl(
			@RequestIdentifier final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier) {
		this.accountBillingAddressesIdentifier = accountBillingAddressesIdentifier;
	}

	@Override
	public Observable<AccountAddressesIdentifier> onLinkFrom() {
		return Observable.just(accountBillingAddressesIdentifier.getAccountAddresses());
	}
}
