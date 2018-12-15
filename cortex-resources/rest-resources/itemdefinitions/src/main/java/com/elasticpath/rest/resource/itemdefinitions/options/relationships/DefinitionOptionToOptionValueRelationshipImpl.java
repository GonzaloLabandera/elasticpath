/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ValueForItemDefinitionOptionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition option to item definition option value link.
 */
public class DefinitionOptionToOptionValueRelationshipImpl implements ValueForItemDefinitionOptionRelationship.LinkTo {

	private final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier;

	private final LinksRepository<ItemDefinitionOptionIdentifier, ItemDefinitionOptionValueIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionIdentifier itemDefinitionOptionIdentifier
	 * @param repository                     repository
	 */
	@Inject
	public DefinitionOptionToOptionValueRelationshipImpl(
			@RequestIdentifier final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionOptionIdentifier, ItemDefinitionOptionValueIdentifier> repository) {
		this.itemDefinitionOptionIdentifier = itemDefinitionOptionIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionOptionValueIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionOptionIdentifier);
	}
}
