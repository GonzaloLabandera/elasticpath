/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesToAccountBillingAddressSelectorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Account Billing Address Default Selector to Billing Addresses.
 * Implements {@link AccountBillingAddressesToAccountBillingAddressSelectorRelationship.LinkFrom}.
 */
public class AccountBillingAddressToAccountBillingAddressSelectorRelationship
		implements AccountBillingAddressesToAccountBillingAddressSelectorRelationship.LinkTo {

	private final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountBillingAddressesIdentifier account billing address selector Identifier
	 */
	@Inject
	public AccountBillingAddressToAccountBillingAddressSelectorRelationship(
			@RequestIdentifier final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier) {
		this.accountBillingAddressesIdentifier = accountBillingAddressesIdentifier;
	}

	@Override
	public Observable<AccountBillingAddressSelectorIdentifier> onLinkTo() {
		return Observable.just(AccountBillingAddressSelectorIdentifier.builder()
				.withAccountBillingAddresses(accountBillingAddressesIdentifier)
				.build());
	}
}
