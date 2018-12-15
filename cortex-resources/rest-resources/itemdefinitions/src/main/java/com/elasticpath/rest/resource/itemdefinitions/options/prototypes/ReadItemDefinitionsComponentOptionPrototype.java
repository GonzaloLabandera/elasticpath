/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionResource;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Component Option prototype for Read operation.
 */
public class ReadItemDefinitionsComponentOptionPrototype implements ItemDefinitionComponentOptionResource.Read {

	private final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier;

	private final Repository<ItemDefinitionOptionEntity, ItemDefinitionComponentOptionIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier
	 * @param repository                              repository
	 */
	@Inject
	public ReadItemDefinitionsComponentOptionPrototype(
			@RequestIdentifier final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier,
			@ResourceRepository final Repository<ItemDefinitionOptionEntity, ItemDefinitionComponentOptionIdentifier> repository) {
		this.itemDefinitionComponentOptionIdentifier = itemDefinitionComponentOptionIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemDefinitionOptionEntity> onRead() {
		return repository.findOne(itemDefinitionComponentOptionIdentifier);
	}
}
