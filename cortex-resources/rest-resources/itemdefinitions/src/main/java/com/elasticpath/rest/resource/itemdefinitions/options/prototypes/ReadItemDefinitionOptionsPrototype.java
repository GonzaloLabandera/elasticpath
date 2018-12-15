/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Options prototype for Read operation.
 */
public class ReadItemDefinitionOptionsPrototype implements ItemDefinitionOptionsResource.Read {

	private final ItemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier;

	private final LinksRepository<ItemDefinitionOptionsIdentifier, ItemDefinitionOptionIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier
	 * @param repository                      repository
	 */
	@Inject
	public ReadItemDefinitionOptionsPrototype(
			@RequestIdentifier final ItemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionOptionsIdentifier, ItemDefinitionOptionIdentifier> repository) {
		this.itemDefinitionOptionsIdentifier = itemDefinitionOptionsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionOptionIdentifier> onRead() {
		return repository.getElements(itemDefinitionOptionsIdentifier);
	}
}
