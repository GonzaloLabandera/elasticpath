/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsForItemDefinitionComponentRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition component to item definition nest components link.
 */
public class DefinitionComponentToNestedComponentsRelationshipImpl
		implements ItemDefinitionNestedComponentsForItemDefinitionComponentRelationship.LinkTo {

	private final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier;

	private final LinksRepository<ItemDefinitionComponentIdentifier, ItemDefinitionNestedComponentsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentIdentifier itemDefinitionComponentIdentifier
	 * @param repository                        repository
	 */
	@Inject
	public DefinitionComponentToNestedComponentsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionComponentIdentifier, ItemDefinitionNestedComponentsIdentifier> repository) {
		this.itemDefinitionComponentIdentifier = itemDefinitionComponentIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionNestedComponentsIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionComponentIdentifier);
	}
}
