/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.OptionsForItemDefinitionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition to item definition options link.
 */
public class DefinitionToOptionsRelationshipImpl implements OptionsForItemDefinitionRelationship.LinkTo {

	private final ItemDefinitionIdentifier itemDefinitionIdentifier;

	private final LinksRepository<ItemDefinitionIdentifier, ItemDefinitionOptionsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionIdentifier itemDefinitionIdentifier
	 * @param repository               repository
	 */
	@Inject
	public DefinitionToOptionsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionIdentifier itemDefinitionIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionIdentifier, ItemDefinitionOptionsIdentifier> repository) {
		this.itemDefinitionIdentifier = itemDefinitionIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionOptionsIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionIdentifier);
	}
}
