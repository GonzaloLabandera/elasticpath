/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ComponentsForItemDefinitionRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition components to item definition link.
 */
public class DefinitionComponentsToDefinitionRelationshipImpl implements ComponentsForItemDefinitionRelationship.LinkFrom {

	private final ItemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier
	 */
	@Inject
	public DefinitionComponentsToDefinitionRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier) {
		this.itemDefinitionComponentsIdentifier = itemDefinitionComponentsIdentifier;
	}

	@Override
	public Observable<ItemDefinitionIdentifier> onLinkFrom() {
		return Observable.just(itemDefinitionComponentsIdentifier.getItemDefinition());
	}
}
