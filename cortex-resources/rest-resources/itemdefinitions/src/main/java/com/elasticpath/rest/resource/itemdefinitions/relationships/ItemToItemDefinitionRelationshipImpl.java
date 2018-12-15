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
 * Item to item definition link.
 */
public class ItemToItemDefinitionRelationshipImpl implements ItemDefinitionForItemRelationship.LinkTo {

	private final ItemIdentifier itemIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemIdentifier itemIdentifier
	 */
	@Inject
	public ItemToItemDefinitionRelationshipImpl(@RequestIdentifier final ItemIdentifier itemIdentifier) {
		this.itemIdentifier = itemIdentifier;
	}

	@Override
	public Observable<ItemDefinitionIdentifier> onLinkTo() {
		return Observable.just(ItemDefinitionIdentifier.builder()
				.withItemId(itemIdentifier.getItemId())
				.withScope(itemIdentifier.getScope())
				.build());
	}
}
