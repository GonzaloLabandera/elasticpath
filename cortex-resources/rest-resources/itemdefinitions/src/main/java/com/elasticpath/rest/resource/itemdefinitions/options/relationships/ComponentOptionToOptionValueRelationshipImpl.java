/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ValueForItemDefinitionComponentOptionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item definition component option to item definition component option value link.
 */
public class ComponentOptionToOptionValueRelationshipImpl implements ValueForItemDefinitionComponentOptionRelationship.LinkTo {

	private final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier;

	private final LinksRepository<ItemDefinitionComponentOptionIdentifier, ItemDefinitionComponentOptionValueIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier
	 * @param repository                              repository
	 */
	@Inject
	public ComponentOptionToOptionValueRelationshipImpl(
			@RequestIdentifier
			final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier,
			@ResourceRepository
			final LinksRepository<ItemDefinitionComponentOptionIdentifier, ItemDefinitionComponentOptionValueIdentifier> repository) {
		this.itemDefinitionComponentOptionIdentifier = itemDefinitionComponentOptionIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionComponentOptionValueIdentifier> onLinkTo() {
		return repository.getElements(itemDefinitionComponentOptionIdentifier);
	}
}
