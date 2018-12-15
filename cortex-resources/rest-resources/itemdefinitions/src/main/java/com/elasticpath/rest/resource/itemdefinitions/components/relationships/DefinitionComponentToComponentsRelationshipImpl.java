/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.relationships;

import java.util.List;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsForItemDefinitionComponentRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.PathIdentifier;

/**
 * Item definition component to item definition components link.
 */
public class DefinitionComponentToComponentsRelationshipImpl implements ItemDefinitionComponentsForItemDefinitionComponentRelationship.LinkTo {

	private final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentIdentifier itemDefinitionComponentIdentifier
	 */
	@Inject
	public DefinitionComponentToComponentsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentIdentifier itemDefinitionComponentIdentifier) {
		this.itemDefinitionComponentIdentifier = itemDefinitionComponentIdentifier;
	}

	@Override
	public Observable<ItemDefinitionComponentsIdentifier> onLinkTo() {
		IdentifierPart<List<String>> componentParentId = ((PathIdentifier) itemDefinitionComponentIdentifier.getComponentId()).extractParentId();
		if (componentParentId.getValue().isEmpty()) {
			return Observable.just(itemDefinitionComponentIdentifier.getItemDefinitionComponents());
		} else {
			return Observable.empty();
		}
	}
}
