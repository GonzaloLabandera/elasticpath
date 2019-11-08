/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.repository.AliasRepository;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.DefaultBillingAddressFromBillingAddressesRelationship;
import com.elasticpath.rest.definition.addresses.DefaultBillingAddressIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Billing addresses to default billing address link.
 */
public class BillingAddressToDefaultBillingAddressRelationshipImpl implements DefaultBillingAddressFromBillingAddressesRelationship.LinkTo {

	private final BillingAddressesIdentifier billingAddressesIdentifier;

	private final AliasRepository<DefaultBillingAddressIdentifier, AddressIdentifier> repository;

	private static final Logger LOG = LoggerFactory.getLogger(BillingAddressToDefaultBillingAddressRelationshipImpl.class);

	/**
	 * Constructor.
	 *
	 * @param billingAddressesIdentifier billingAddressesIdentifier
	 * @param repository                 repository
	 */
	@Inject
	public BillingAddressToDefaultBillingAddressRelationshipImpl(@RequestIdentifier final BillingAddressesIdentifier billingAddressesIdentifier,
			@ResourceRepository final AliasRepository<DefaultBillingAddressIdentifier, AddressIdentifier> repository) {
		this.billingAddressesIdentifier = billingAddressesIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<DefaultBillingAddressIdentifier> onLinkTo() {
		DefaultBillingAddressIdentifier defaultBillingAddressIdentifier = DefaultBillingAddressIdentifier.builder()
				.withBillingAddresses(billingAddressesIdentifier)
				.build();

		return repository.resolve(defaultBillingAddressIdentifier)
				.toObservable()
				.map(addressIdentifier -> defaultBillingAddressIdentifier)
				.doOnError(throwable -> LOG.info("Could not find a default billing address."))
				.onErrorResumeNext(Observable.empty());
	}
}
