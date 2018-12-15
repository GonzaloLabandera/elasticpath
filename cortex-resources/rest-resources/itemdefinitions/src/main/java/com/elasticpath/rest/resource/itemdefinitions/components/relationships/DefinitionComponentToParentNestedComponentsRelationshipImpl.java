/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import java.util.List;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ParentNestedComponentsForItemDefinitionComponentRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.PathIdentifier;

/**
 * Item definition component to the parent item definition nest components link.
 */
public class DefinitionComponentToParentNestedComponentsRelationshipImpl
		implements ParentNestedComponentsForItemDefinitionComponentRelationship.LinkTo {

	private final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentIdentifier itemDefinitionComponentIdentifier
	 */
	@Inject
	public DefinitionComponentToParentNestedComponentsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier) {
		this.itemDefinitionComponentIdentifier = itemDefinitionComponentIdentifier;
	}

	@Override
	public Observable<ItemDefinitionNestedComponentsIdentifier> onLinkTo() {
		IdentifierPart<List<String>> componentParentId = ((PathIdentifier) itemDefinitionComponentIdentifier.getComponentId()).extractParentId();
		if (componentParentId.getValue().isEmpty()) {
			return Observable.empty();
		} else {
			return Observable.just(ItemDefinitionNestedComponentsIdentifier.builder()
					.withItemDefinitionComponent(ItemDefinitionComponentIdentifier.builder()
							.withItemDefinitionComponents(itemDefinitionComponentIdentifier.getItemDefinitionComponents())
							.withComponentId(componentParentId)
							.build())
					.build());
		}
	}
}
