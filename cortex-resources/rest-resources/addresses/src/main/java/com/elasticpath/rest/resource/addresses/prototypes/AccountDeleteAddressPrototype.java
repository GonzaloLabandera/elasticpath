/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.prototypes;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressResource;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Address prototype for Delete operation.
 */
public class AccountDeleteAddressPrototype implements AccountAddressResource.Delete {

	private final AccountAddressIdentifier accountAddressIdentifier;

	private final Repository<AddressEntity, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountAddressIdentifier addressIdentifier
	 * @param repository               repository
	 */
	@Inject
	public AccountDeleteAddressPrototype(@RequestIdentifier final AccountAddressIdentifier accountAddressIdentifier,
										 @ResourceRepository final Repository<AddressEntity, AccountAddressIdentifier> repository) {
		this.accountAddressIdentifier = accountAddressIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onDelete() {
		return repository.delete(accountAddressIdentifier);
	}
}
