/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.addresses.BillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.BillingaddressesToAddressesBillingaddressSelectorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Create the relationship from the Billing Address Default Selector to Billing Addresses.
 * Implements {@link BillingaddressesToAddressesBillingaddressSelectorRelationship.LinkFrom}.
 */
public class BillingAddressDefaultSelectorToBillingAddressesRelationship
		implements BillingaddressesToAddressesBillingaddressSelectorRelationship.LinkFrom {

	private final BillingAddressSelectorIdentifier addressSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param addressSelectorIdentifier Billing address selector Identifier
	 */
	@Inject
	public BillingAddressDefaultSelectorToBillingAddressesRelationship(
			@RequestIdentifier final BillingAddressSelectorIdentifier addressSelectorIdentifier) {
		this.addressSelectorIdentifier = addressSelectorIdentifier;
	}

	@Override
	public Observable<BillingAddressesIdentifier> onLinkFrom() {
		return Observable.just(addressSelectorIdentifier.getBillingAddresses());
	}

}
