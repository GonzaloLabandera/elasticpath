/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.AliasRepository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultShippingAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultShippingAddressResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Default Account Shipping Address prototype for Read operation.
 */
public class AccountReadDefaultShippingAddressPrototype implements AccountDefaultShippingAddressResource.Read {

	private final AccountDefaultShippingAddressIdentifier accountDefaultShippingAddressIdentifier;

	private final AliasRepository<AccountDefaultShippingAddressIdentifier, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountDefaultShippingAddressIdentifier defaultAccountShippingAddressIdentifier
	 * @param repository                              repository
	 */
	@Inject
	public AccountReadDefaultShippingAddressPrototype(
			@RequestIdentifier final AccountDefaultShippingAddressIdentifier accountDefaultShippingAddressIdentifier,
		  	@ResourceRepository final AliasRepository<AccountDefaultShippingAddressIdentifier, AccountAddressIdentifier> repository) {
		this.accountDefaultShippingAddressIdentifier = accountDefaultShippingAddressIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<AccountAddressIdentifier> onRead() {
		return repository.resolve(accountDefaultShippingAddressIdentifier);
	}
}
