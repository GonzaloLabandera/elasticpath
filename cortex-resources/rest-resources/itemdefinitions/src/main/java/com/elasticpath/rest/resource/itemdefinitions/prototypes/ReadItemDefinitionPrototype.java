/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition prototype for Read operation.
 */
public class ReadItemDefinitionPrototype implements ItemDefinitionResource.Read {

	private final ItemDefinitionIdentifier itemDefinitionIdentifier;
	private final Repository<ItemDefinitionEntity, ItemDefinitionIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionIdentifier itemDefinitionIdentifier
	 * @param repository               repository
	 */
	@Inject
	public ReadItemDefinitionPrototype(@RequestIdentifier final ItemDefinitionIdentifier itemDefinitionIdentifier,
									   @ResourceRepository final Repository<ItemDefinitionEntity, ItemDefinitionIdentifier> repository) {
		this.itemDefinitionIdentifier = itemDefinitionIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemDefinitionEntity> onRead() {
		return repository.findOne(itemDefinitionIdentifier);
	}
}
