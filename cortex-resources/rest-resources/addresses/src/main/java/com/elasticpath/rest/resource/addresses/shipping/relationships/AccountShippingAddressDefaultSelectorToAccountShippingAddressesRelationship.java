/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesToAccountShippingAddressSelectorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Account Shipping Address Default Selector to Shipping Addresses.
 * Implements {@link AccountShippingAddressesToAccountShippingAddressSelectorRelationship.LinkFrom}.
 */
public class AccountShippingAddressDefaultSelectorToAccountShippingAddressesRelationship
		implements AccountShippingAddressesToAccountShippingAddressSelectorRelationship.LinkFrom {

	private final AccountShippingAddressSelectorIdentifier addressSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountShippingAddressSelectorIdentifier Shipping address selector Identifier
	 */
	@Inject
	public AccountShippingAddressDefaultSelectorToAccountShippingAddressesRelationship(
			@RequestIdentifier final AccountShippingAddressSelectorIdentifier accountShippingAddressSelectorIdentifier) {
		this.addressSelectorIdentifier = accountShippingAddressSelectorIdentifier;
	}

	@Override
	public Observable<AccountShippingAddressesIdentifier> onLinkFrom() {
		return Observable.just(addressSelectorIdentifier.getAccountShippingAddresses());
	}

}
