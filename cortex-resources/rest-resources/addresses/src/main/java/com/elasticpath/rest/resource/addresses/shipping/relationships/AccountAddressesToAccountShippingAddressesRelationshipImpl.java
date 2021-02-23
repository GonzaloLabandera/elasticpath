/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesFromAccountAddressesRelationship;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account Addresses to shipping addresses link.
 */
public class AccountAddressesToAccountShippingAddressesRelationshipImpl implements AccountShippingAddressesFromAccountAddressesRelationship.LinkTo {

	private final AccountAddressesIdentifier accountAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAddressesIdentifier addressesIdentifier
	 */
	@Inject
	public AccountAddressesToAccountShippingAddressesRelationshipImpl(
			@RequestIdentifier final AccountAddressesIdentifier accountAddressesIdentifier) {
		this.accountAddressesIdentifier = accountAddressesIdentifier;
	}

	@Override
	public Observable<AccountShippingAddressesIdentifier> onLinkTo() {
		return Observable.just(AccountShippingAddressesIdentifier.builder()
				.withAccountAddresses(accountAddressesIdentifier)
				.build());
	}
}
