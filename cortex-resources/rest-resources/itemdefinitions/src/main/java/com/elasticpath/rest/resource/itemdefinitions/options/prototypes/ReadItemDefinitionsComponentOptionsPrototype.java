/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Component Options prototype for Read operation.
 */
public class ReadItemDefinitionsComponentOptionsPrototype implements ItemDefinitionComponentOptionsResource.Read {

	private final ItemDefinitionComponentOptionsIdentifier itemDefinitionComponentOptionsIdentifier;

	private final LinksRepository<ItemDefinitionComponentOptionsIdentifier, ItemDefinitionComponentOptionIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionsIdentifier itemDefinitionComponentOptionsIdentifier
	 * @param repository                               repository
	 */
	@Inject
	public ReadItemDefinitionsComponentOptionsPrototype(
			@RequestIdentifier final ItemDefinitionComponentOptionsIdentifier itemDefinitionComponentOptionsIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionComponentOptionsIdentifier, ItemDefinitionComponentOptionIdentifier> repository) {
		this.itemDefinitionComponentOptionsIdentifier = itemDefinitionComponentOptionsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionComponentOptionIdentifier> onRead() {
		return repository.getElements(itemDefinitionComponentOptionsIdentifier);
	}
}
