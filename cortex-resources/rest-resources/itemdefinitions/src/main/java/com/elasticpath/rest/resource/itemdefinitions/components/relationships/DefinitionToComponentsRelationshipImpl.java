/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ComponentsForItemDefinitionRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition to item definition components link.
 */
public class DefinitionToComponentsRelationshipImpl implements ComponentsForItemDefinitionRelationship.LinkTo {

	private final ItemDefinitionIdentifier itemDefinitionIdentifier;

	private final LinksRepository<ItemDefinitionIdentifier, ItemDefinitionComponentsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionIdentifier itemDefinitionIdentifier
	 * @param repository               repository
	 */
	@Inject
	public DefinitionToComponentsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionIdentifier itemDefinitionIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionIdentifier, ItemDefinitionComponentsIdentifier> repository) {
		this.itemDefinitionIdentifier = itemDefinitionIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionComponentsIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionIdentifier);
	}
}
