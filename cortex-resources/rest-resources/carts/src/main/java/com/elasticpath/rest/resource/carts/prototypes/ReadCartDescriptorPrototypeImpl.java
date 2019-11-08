/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartDescriptorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read multi cart choice prototype.
 */
public class ReadCartDescriptorPrototypeImpl implements CartDescriptorResource.Read {

	private final CartDescriptorIdentifier identifier;
	private final Repository<CartDescriptorEntity, CartDescriptorIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier the identifier.
	 * @param  repository the repository.
	 */
	@Inject
	public ReadCartDescriptorPrototypeImpl(@RequestIdentifier final CartDescriptorIdentifier identifier,
										   @ResourceRepository final Repository<CartDescriptorEntity, CartDescriptorIdentifier> repository) {

		this.identifier = identifier;
		this.repository = repository;
	}


	@Override
	public Single<CartDescriptorEntity> onRead() {
		return repository.findOne(identifier);
	}
}