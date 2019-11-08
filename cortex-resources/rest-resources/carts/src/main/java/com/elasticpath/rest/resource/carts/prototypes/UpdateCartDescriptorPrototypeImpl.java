/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartDescriptorResource;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read multi cart choice prototype.
 */
public class UpdateCartDescriptorPrototypeImpl implements CartDescriptorResource.Update {

	private final CartDescriptorIdentifier identifier;
	private final CartDescriptorEntity cartDescriptorEntity;
	private final Repository<CartDescriptorEntity, CartDescriptorIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier the identifier.
	 * @param cartDescriptorEntity the entity.
	 * @param  repository the repository.
	 */
	@Inject
	public UpdateCartDescriptorPrototypeImpl(@RequestIdentifier final CartDescriptorIdentifier identifier,
											 @RequestForm final CartDescriptorEntity cartDescriptorEntity,
											 @ResourceRepository final Repository<CartDescriptorEntity, CartDescriptorIdentifier> repository) {

		this.identifier = identifier;
		this.cartDescriptorEntity = cartDescriptorEntity;
		this.repository = repository;
	}


	@Override
	public Completable onUpdate() {
		return repository.update(cartDescriptorEntity, identifier);
	}
}