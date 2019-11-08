/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CreateCartFormEntity;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read create cart form.
 */
public class ReadCreateCartFormPrototypeImpl implements CreateCartFormResource.Read {

	private final CreateCartFormIdentifier identifier;
	private final Repository<CreateCartFormEntity, CreateCartFormIdentifier> repository;


	/**
	 * Constructor.
	 *
	 * @param identifier    identifier.
	 * @param repository	the repository.
	 */
	@Inject
	public ReadCreateCartFormPrototypeImpl(@RequestIdentifier final CreateCartFormIdentifier identifier,
										   @ResourceRepository final Repository<CreateCartFormEntity,
			CreateCartFormIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Single<CreateCartFormEntity> onRead() {
		return repository.findOne(identifier);

	}
}