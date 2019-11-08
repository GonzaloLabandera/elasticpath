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
 * Create the relationship from the Shipping Address Default Selector to Shipping Addresses.
 * Implements {@link ShippingaddressesToAddressesShippingaddressSelectorRelationship.LinkFrom}.
 */
public class ShippingAddressDefaultSelectorToShippingAddressesRelationship
		implements ShippingaddressesToAddressesShippingaddressSelectorRelationship.LinkFrom {

	private final ShippingAddressSelectorIdentifier addressSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param addressSelectorIdentifier Shipping address selector Identifier
	 */
	@Inject
	public ShippingAddressDefaultSelectorToShippingAddressesRelationship(
			@RequestIdentifier final ShippingAddressSelectorIdentifier addressSelectorIdentifier) {
		this.addressSelectorIdentifier = addressSelectorIdentifier;
	}

	@Override
	public Observable<ShippingAddressesIdentifier> onLinkFrom() {
		return Observable.just(addressSelectorIdentifier.getShippingAddresses());
	}

}
