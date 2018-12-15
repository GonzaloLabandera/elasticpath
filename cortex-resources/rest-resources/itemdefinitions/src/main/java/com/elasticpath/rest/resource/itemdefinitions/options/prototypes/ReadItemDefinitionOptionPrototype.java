/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Option prototype for Read operation.
 */
public class ReadItemDefinitionOptionPrototype implements ItemDefinitionOptionResource.Read {

	private final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier;

	private final Repository<ItemDefinitionOptionEntity, ItemDefinitionOptionIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionIdentifier itemDefinitionOptionIdentifier
	 * @param repository                     repository
	 */
	@Inject
	public ReadItemDefinitionOptionPrototype(
			@RequestIdentifier final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier,
			@ResourceRepository final Repository<ItemDefinitionOptionEntity, ItemDefinitionOptionIdentifier> repository) {
		this.itemDefinitionOptionIdentifier = itemDefinitionOptionIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemDefinitionOptionEntity> onRead() {
		return repository.findOne(itemDefinitionOptionIdentifier);
	}
}
