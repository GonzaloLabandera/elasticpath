/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Option Value prototype for Read operation.
 */
public class ReadItemDefinitionOptionValuePrototype implements ItemDefinitionOptionValueResource.Read {

	private final ItemDefinitionOptionValueIdentifier itemDefinitionOptionValueIdentifier;

	private final Repository<ItemDefinitionOptionValueEntity, ItemDefinitionOptionValueIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionValueIdentifier itemDefinitionOptionValueIdentifier
	 * @param repository                          repository
	 */
	@Inject
	public ReadItemDefinitionOptionValuePrototype(
			@RequestIdentifier final ItemDefinitionOptionValueIdentifier itemDefinitionOptionValueIdentifier,
			@ResourceRepository final Repository<ItemDefinitionOptionValueEntity, ItemDefinitionOptionValueIdentifier> repository) {
		this.itemDefinitionOptionValueIdentifier = itemDefinitionOptionValueIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ItemDefinitionOptionValueEntity> onRead() {
		return repository.findOne(itemDefinitionOptionValueIdentifier);
	}
}
