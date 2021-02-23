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
 * Create the relationship from the Billing Address Default Selector to Billing Addresses.
 * Implements {@link AccountBillingAddressesToAccountBillingAddressSelectorRelationship.LinkFrom}.
 */
public class AccountBillingAddressDefaultSelectorToAccountBillingAddressesRelationship
		implements AccountBillingAddressesToAccountBillingAddressSelectorRelationship.LinkFrom {

	private final AccountBillingAddressSelectorIdentifier addressSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountBillingAddressSelectorIdentifier Billing address selector Identifier
	 */
	@Inject
	public AccountBillingAddressDefaultSelectorToAccountBillingAddressesRelationship(
			@RequestIdentifier final AccountBillingAddressSelectorIdentifier accountBillingAddressSelectorIdentifier) {
		this.addressSelectorIdentifier = accountBillingAddressSelectorIdentifier;
	}

	@Override
	public Observable<AccountBillingAddressesIdentifier> onLinkFrom() {
		return Observable.just(addressSelectorIdentifier.getAccountBillingAddresses());
	}

}
