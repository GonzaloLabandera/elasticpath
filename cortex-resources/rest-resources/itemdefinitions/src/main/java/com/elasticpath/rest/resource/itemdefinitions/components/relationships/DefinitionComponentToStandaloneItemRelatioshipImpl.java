/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.StandaloneItemForItemDefinitionComponentRelationship;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition component to item link.
 */
public class DefinitionComponentToStandaloneItemRelatioshipImpl implements StandaloneItemForItemDefinitionComponentRelationship.LinkTo {

	private final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier;

	private final LinksRepository<ItemDefinitionComponentIdentifier, ItemIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentIdentifier itemDefinitionComponentIdentifier
	 * @param repository                        repository
	 */
	@Inject
	public DefinitionComponentToStandaloneItemRelatioshipImpl(
			@RequestIdentifier final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionComponentIdentifier, ItemIdentifier> repository) {
		this.itemDefinitionComponentIdentifier = itemDefinitionComponentIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionComponentIdentifier);
	}
}
