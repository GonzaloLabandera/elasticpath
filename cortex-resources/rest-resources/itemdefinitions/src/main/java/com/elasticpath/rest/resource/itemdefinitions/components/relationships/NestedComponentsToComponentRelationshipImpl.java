/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsForItemDefinitionComponentRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition nested components to item definition component link.
 */
public class NestedComponentsToComponentRelationshipImpl implements ItemDefinitionNestedComponentsForItemDefinitionComponentRelationship.LinkFrom {

	private final ItemDefinitionNestedComponentsIdentifier itemDefinitionNestedComponentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionNestedComponentsIdentifier itemDefinitionNestedComponentsIdentifier
	 */
	@Inject
	public NestedComponentsToComponentRelationshipImpl(
			@RequestIdentifier final ItemDefinitionNestedComponentsIdentifier itemDefinitionNestedComponentsIdentifier) {
		this.itemDefinitionNestedComponentsIdentifier = itemDefinitionNestedComponentsIdentifier;
	}

	@Override
	public Observable<ItemDefinitionComponentIdentifier> onLinkFrom() {
		return Observable.just(itemDefinitionNestedComponentsIdentifier.getItemDefinitionComponent());
	}
}
