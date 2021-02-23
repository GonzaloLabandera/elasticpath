/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesAccountShippingAddressSelectorRelationship;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Account Shipping Addresses to Account Shipping Address Default Selector.
 * Implements {@link AccountShippingAddressesAccountShippingAddressSelectorRelationship.LinkTo}.
 */
public class AccountShippingAddressesToAccountShippingAddressDefaultSelectorRelationship
		implements AccountShippingAddressesAccountShippingAddressSelectorRelationship.LinkTo {

	private final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountShippingAddressesIdentifier account shipping address Identifier
	 */
	@Inject
	public AccountShippingAddressesToAccountShippingAddressDefaultSelectorRelationship(
			@RequestIdentifier final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier) {
		this.accountShippingAddressesIdentifier = accountShippingAddressesIdentifier;
	}

	@Override
	public Observable<AccountShippingAddressSelectorIdentifier> onLinkTo() {
		return Observable.just(AccountShippingAddressSelectorIdentifier.builder()
				.withAccountShippingAddresses(accountShippingAddressesIdentifier)
				.build());
	}

}
