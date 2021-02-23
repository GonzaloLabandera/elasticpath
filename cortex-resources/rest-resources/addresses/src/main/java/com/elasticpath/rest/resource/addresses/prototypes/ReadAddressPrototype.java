/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressResource;
import com.elasticpath.rest.definition.addresses.ContextAwareAddressIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Address prototype for Read operation.
 */
public class ReadAddressPrototype implements AddressResource.Read {

	private final AddressIdentifier addressIdentifier;

	private final Repository<AddressEntity, AddressIdentifier> repository;

	private final Repository<AddressEntity, ContextAwareAddressIdentifier> contextAwareRepository;

	/**
	 * Constructor.
	 *
	 * @param addressIdentifier      addressIdentifier
	 * @param repository             repository
	 * @param contextAwareRepository address repository
	 */
	@Inject
	public ReadAddressPrototype(@RequestIdentifier final AddressIdentifier addressIdentifier,
								@ResourceRepository final Repository<AddressEntity, AddressIdentifier> repository,
								@ResourceRepository final Repository<AddressEntity, ContextAwareAddressIdentifier> contextAwareRepository) {
		this.addressIdentifier = addressIdentifier;
		this.repository = repository;
		this.contextAwareRepository = contextAwareRepository;
	}

	@Override
	public Single<AddressEntity> onRead() {
		return repository.findOne(addressIdentifier)
				.onErrorResumeNext((contextAwareRepository.findOne(new ContextAwareAddressIdentifier(addressIdentifier))));
	}
}
