/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.addresses.AccountFromAccountShippingAddressesRelationship;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Acount billing addresses to profile link.
 */
public class AccountFromAccountShippingAddressesRelationshipImpl implements AccountFromAccountShippingAddressesRelationship.LinkTo {

	private final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountShippingAddressesIdentifier billingAddressesIdentifier
	 */
	@Inject
	public AccountFromAccountShippingAddressesRelationshipImpl(@RequestIdentifier final AccountShippingAddressesIdentifier
																		   accountShippingAddressesIdentifier) {
		this.accountShippingAddressesIdentifier = accountShippingAddressesIdentifier;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return Observable.just(AccountIdentifier.builder()
				.withAccountId(accountShippingAddressesIdentifier.getAccountAddresses().getAccountId())
				.withAccounts(AccountsIdentifier.builder()
						.withScope(accountShippingAddressesIdentifier
								.getAccountAddresses().getAddresses().getScope()).build())
				.build());
	}
}
