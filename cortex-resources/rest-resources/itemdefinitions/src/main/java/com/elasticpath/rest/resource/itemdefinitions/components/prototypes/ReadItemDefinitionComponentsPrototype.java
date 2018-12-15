/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Item Definition Components prototype for Read operation.
 */
public class ReadItemDefinitionComponentsPrototype implements ItemDefinitionComponentsResource.Read {

	private final ItemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier;

	private final LinksRepository<ItemDefinitionComponentsIdentifier, ItemDefinitionComponentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier
	 * @param repository                         repository
	 */
	@Inject
	public ReadItemDefinitionComponentsPrototype(
			@RequestIdentifier final ItemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier,
			@ResourceRepository final LinksRepository<ItemDefinitionComponentsIdentifier, ItemDefinitionComponentIdentifier> repository) {
		this.itemDefinitionComponentsIdentifier = itemDefinitionComponentsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemDefinitionComponentIdentifier> onRead() {
		return repository.getElements(itemDefinitionComponentsIdentifier);
	}
}
