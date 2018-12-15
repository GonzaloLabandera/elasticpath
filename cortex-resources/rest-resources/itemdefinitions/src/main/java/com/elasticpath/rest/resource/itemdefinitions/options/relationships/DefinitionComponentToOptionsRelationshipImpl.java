/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.OptionsForItemDefinitionComponentRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition component to item definition component options link.
 */
public class DefinitionComponentToOptionsRelationshipImpl implements OptionsForItemDefinitionComponentRelationship.LinkTo {

	private final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier;

	private final LinksRepository<ItemDefinitionComponentIdentifier, ItemDefinitionComponentOptionsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentIdentifier itemDefinitionComponentIdentifier
	 * @param repository                        repository
	 */
	@Inject
	public DefinitionComponentToOptionsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionComponentIdentifier, ItemDefinitionComponentOptionsIdentifier> repository) {
		this.itemDefinitionComponentIdentifier = itemDefinitionComponentIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionComponentOptionsIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionComponentIdentifier);
	}
}
