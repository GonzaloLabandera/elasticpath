/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountFromAccountBillingAddressesRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account billing addresses to profile link.
 */
public class AccountFromAccountBillingAddressesRelationshipImpl implements AccountFromAccountBillingAddressesRelationship.LinkTo {

	private final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountBillingAddressesIdentifier accountBillingAddressesIdentifier
	 */
	@Inject
	public AccountFromAccountBillingAddressesRelationshipImpl(@RequestIdentifier final AccountBillingAddressesIdentifier
																		  accountBillingAddressesIdentifier) {
		this.accountBillingAddressesIdentifier = accountBillingAddressesIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(AccountIdentifier.builder()
				.withAccountId(accountBillingAddressesIdentifier.getAccountAddresses().getAccountId())
				.withAccounts(AccountsIdentifier.builder()
						.withScope(accountBillingAddressesIdentifier
								.getAccountAddresses().getAddresses().getScope()).build())
				.build());
	}
}
