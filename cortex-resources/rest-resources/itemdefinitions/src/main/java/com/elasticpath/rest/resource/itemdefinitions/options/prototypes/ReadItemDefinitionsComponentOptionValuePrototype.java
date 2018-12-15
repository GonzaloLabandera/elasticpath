/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueResource;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Component Option Value prototype for Read operation.
 */
public class ReadItemDefinitionsComponentOptionValuePrototype implements ItemDefinitionComponentOptionValueResource.Read {

	private final ItemDefinitionComponentOptionValueIdentifier itemDefinitionComponentOptionValueIdentifier;

	private final Repository<ItemDefinitionOptionValueEntity, ItemDefinitionComponentOptionValueIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionValueIdentifier itemDefinitionComponentOptionValueIdentifier
	 * @param repository                                   repository
	 */
	@Inject
	public ReadItemDefinitionsComponentOptionValuePrototype(
			@RequestIdentifier final ItemDefinitionComponentOptionValueIdentifier itemDefinitionComponentOptionValueIdentifier,
			@ResourceRepository final Repository<ItemDefinitionOptionValueEntity, ItemDefinitionComponentOptionValueIdentifier> repository) {
		this.itemDefinitionComponentOptionValueIdentifier = itemDefinitionComponentOptionValueIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemDefinitionOptionValueEntity> onRead() {
		return repository.findOne(itemDefinitionComponentOptionValueIdentifier);
	}
}
