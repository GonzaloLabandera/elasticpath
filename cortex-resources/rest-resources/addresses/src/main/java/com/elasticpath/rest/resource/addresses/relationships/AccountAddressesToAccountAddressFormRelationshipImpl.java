/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountAddressFormIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddAccountAddressFormFromAccountAddressesRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account addresses to add account address form link.
 */
public class AccountAddressesToAccountAddressFormRelationshipImpl implements AddAccountAddressFormFromAccountAddressesRelationship.LinkTo {

	private final AccountAddressesIdentifier addressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountAddressesIdentifier account addresses identifier
	 */
	@Inject
	public AccountAddressesToAccountAddressFormRelationshipImpl(@RequestIdentifier final AccountAddressesIdentifier accountAddressesIdentifier) {
		this.addressesIdentifier = accountAddressesIdentifier;
	}

	@Override
	public Observable<AccountAddressFormIdentifier> onLinkTo() {
		return Observable.just(AccountAddressFormIdentifier.builder()
				.withAccountAddresses(addressesIdentifier)
				.build());
	}
}
