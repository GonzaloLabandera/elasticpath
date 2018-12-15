/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Nested Components prototype for Read operation.
 */
public class ReadItemDefinitionNestedComponentsPrototype implements ItemDefinitionNestedComponentsResource.Read {

	private final ItemDefinitionNestedComponentsIdentifier itemDefinitionNestedComponentsIdentifier;

	private final LinksRepository<ItemDefinitionNestedComponentsIdentifier, ItemDefinitionComponentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionNestedComponentsIdentifier itemDefinitionNestedComponentsIdentifier
	 * @param repository                               repository
	 */
	@Inject
	public ReadItemDefinitionNestedComponentsPrototype(
			@RequestIdentifier final ItemDefinitionNestedComponentsIdentifier itemDefinitionNestedComponentsIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionNestedComponentsIdentifier, ItemDefinitionComponentIdentifier> repository) {
		this.itemDefinitionNestedComponentsIdentifier = itemDefinitionNestedComponentsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionComponentIdentifier> onRead() {
		return repository.getElements(itemDefinitionNestedComponentsIdentifier);
	}
}
