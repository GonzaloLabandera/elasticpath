/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionForItemRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition to item link.
 */
public class ItemDefinitionToItemRelationshipImpl implements ItemDefinitionForItemRelationship.LinkFrom {

	private final ItemDefinitionIdentifier itemDefinitionIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionIdentifier itemDefinitionIdentifier
	 */
	@Inject
	public ItemDefinitionToItemRelationshipImpl(@RequestIdentifier final ItemDefinitionIdentifier itemDefinitionIdentifier) {
		this.itemDefinitionIdentifier = itemDefinitionIdentifier;
	}

	@Override
	public Observable<ItemIdentifier> onLinkFrom() {
		return Observable.just(ItemIdentifier.builder()
				.withItemId(itemDefinitionIdentifier.getItemId())
				.withScope(itemDefinitionIdentifier.getScope())
				.build());
	}
}
