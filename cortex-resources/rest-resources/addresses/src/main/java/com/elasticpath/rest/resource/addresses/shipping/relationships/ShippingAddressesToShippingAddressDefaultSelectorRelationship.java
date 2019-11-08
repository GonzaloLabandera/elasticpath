/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingaddressesToAddressesShippingaddressSelectorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Shipping Addresses to Shipping Address Default Selector.
 * Implements {@link ShippingaddressesToAddressesShippingaddressSelectorRelationship.LinkTo}.
 */
public class ShippingAddressesToShippingAddressDefaultSelectorRelationship
		implements ShippingaddressesToAddressesShippingaddressSelectorRelationship.LinkTo {

	private final ShippingAddressesIdentifier addressIdentifier;

	/**
	 * Constructor.
	 *
	 * @param addressIdentifier Shipping address Identifier
	 */
	@Inject
	public ShippingAddressesToShippingAddressDefaultSelectorRelationship(@RequestIdentifier final ShippingAddressesIdentifier addressIdentifier) {
		this.addressIdentifier = addressIdentifier;
	}

	@Override
	public Observable<ShippingAddressSelectorIdentifier> onLinkTo() {
		return Observable.just(ShippingAddressSelectorIdentifier.builder()
				.withShippingAddresses(addressIdentifier)
				.build());
	}

}
