/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.repository.AliasRepository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultShippingAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.DefaultAccountShippingAddressFromAccountShippingAddressesRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account shipping addresses to default shipping address link.
 */
public class AccountDefaultShippingAddressFromAccountShippingAddressesRelationshipImpl
		implements DefaultAccountShippingAddressFromAccountShippingAddressesRelationship.LinkTo {

	private static final Logger LOG = LoggerFactory.getLogger(AccountDefaultShippingAddressFromAccountShippingAddressesRelationshipImpl.class);
	private final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier;
	private final AliasRepository<AccountDefaultShippingAddressIdentifier, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountShippingAddressesIdentifier shippingAddressesIdentifier
	 * @param repository                         repository
	 */
	@Inject
	public AccountDefaultShippingAddressFromAccountShippingAddressesRelationshipImpl(
			@RequestIdentifier final AccountShippingAddressesIdentifier accountShippingAddressesIdentifier,
			@ResourceRepository final AliasRepository<AccountDefaultShippingAddressIdentifier, AccountAddressIdentifier> repository) {
		this.accountShippingAddressesIdentifier = accountShippingAddressesIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountDefaultShippingAddressIdentifier> onLinkTo() {
		AccountDefaultShippingAddressIdentifier defaultShippingAddressIdentifier = AccountDefaultShippingAddressIdentifier.builder()
				.withAccountShippingAddresses(accountShippingAddressesIdentifier)
				.build();

		return repository.resolve(defaultShippingAddressIdentifier)
				.toObservable()
				.map(addressIdentifier -> defaultShippingAddressIdentifier)
				.doOnError(throwable -> LOG.info("Could not find a default shipping address."))
				.onErrorResumeNext(Observable.empty());
	}
}
