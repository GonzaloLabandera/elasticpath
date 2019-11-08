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
 * Create the relationship from the Billing Addresses to Billing Address Default Selector.
 * Implements {@link BillingaddressesToAddressesBillingaddressSelectorRelationship.LinkTo}.
 */
public class BillingAddressesToBillingAddressDefaultSelectorRelationship
		implements BillingaddressesToAddressesBillingaddressSelectorRelationship.LinkTo {

	private final BillingAddressesIdentifier addressIdentifier;

	/**
	 * Constructor.
	 *
	 * @param addressIdentifier Billing address Identifier
	 */
	@Inject
	public BillingAddressesToBillingAddressDefaultSelectorRelationship(@RequestIdentifier final BillingAddressesIdentifier addressIdentifier) {
		this.addressIdentifier = addressIdentifier;
	}

	@Override
	public Observable<BillingAddressSelectorIdentifier> onLinkTo() {
		return Observable.just(BillingAddressSelectorIdentifier.builder()
				.withBillingAddresses(addressIdentifier)
				.build());
	}

}
