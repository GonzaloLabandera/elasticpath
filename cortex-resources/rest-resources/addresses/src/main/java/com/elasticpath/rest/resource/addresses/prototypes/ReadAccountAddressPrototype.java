/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressResource;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Address prototype for Read operation.
 */
public class ReadAccountAddressPrototype implements AccountAddressResource.Read {

	private final AccountAddressIdentifier accountAddressIdentifier;

	private final Repository<AddressEntity, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountAddressIdentifier account address identifier
	 * @param repository               the repository
	 */
	@Inject
	public ReadAccountAddressPrototype(@RequestIdentifier final AccountAddressIdentifier accountAddressIdentifier,
									   @ResourceRepository final Repository<AddressEntity, AccountAddressIdentifier> repository) {
		this.accountAddressIdentifier = accountAddressIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<AddressEntity> onRead() {
		return repository.findOne(accountAddressIdentifier);
	}
}
