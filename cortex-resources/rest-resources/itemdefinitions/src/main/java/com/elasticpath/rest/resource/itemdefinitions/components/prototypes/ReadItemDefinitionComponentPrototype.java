/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Component prototype for Read operation.
 */
public class ReadItemDefinitionComponentPrototype implements ItemDefinitionComponentResource.Read {

	private final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier;

	private final Repository<ItemDefinitionComponentEntity, ItemDefinitionComponentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentIdentifier itemDefinitionComponentIdentifier
	 * @param repository                        repository
	 */
	@Inject
	public ReadItemDefinitionComponentPrototype(
			@RequestIdentifier final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier,
			@ResourceRepository final Repository<ItemDefinitionComponentEntity, ItemDefinitionComponentIdentifier> repository) {
		this.itemDefinitionComponentIdentifier = itemDefinitionComponentIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemDefinitionComponentEntity> onRead() {
		return repository.findOne(itemDefinitionComponentIdentifier);
	}
}
