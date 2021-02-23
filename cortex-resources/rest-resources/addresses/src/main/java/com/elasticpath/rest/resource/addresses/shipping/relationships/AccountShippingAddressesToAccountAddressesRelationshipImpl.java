/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesToAccountAddressesRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account shipping addresses to addresses link.
 */
public class AccountShippingAddressesToAccountAddressesRelationshipImpl implements AccountShippingAddressesToAccountAddressesRelationship.LinkTo {

	private final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountShippingAddressesIdentifier accountShippingAddressesIdentifier
	 */
	@Inject
	public AccountShippingAddressesToAccountAddressesRelationshipImpl(
			@RequestIdentifier final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier) {
		this.accountShippingAddressesIdentifier = accountShippingAddressesIdentifier;
	}

	@Override
	public Observable<AccountAddressesIdentifier> onLinkTo() {
		return Observable.just(accountShippingAddressesIdentifier.getAccountAddresses());
	}
}
