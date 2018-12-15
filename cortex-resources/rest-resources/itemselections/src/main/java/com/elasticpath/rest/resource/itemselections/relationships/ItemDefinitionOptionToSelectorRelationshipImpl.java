/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.definition.itemselections.SelectorForItemDefinitionOptionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item option to item definition option selector link.
 */
public class ItemDefinitionOptionToSelectorRelationshipImpl implements SelectorForItemDefinitionOptionRelationship.LinkTo {

	private final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionIdentifier itemDefinitionOptionIdentifier
	 */
	@Inject
	public ItemDefinitionOptionToSelectorRelationshipImpl(@RequestIdentifier final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier) {
		this.itemDefinitionOptionIdentifier = itemDefinitionOptionIdentifier;
	}

	@Override
	public Observable<ItemOptionSelectorIdentifier> onLinkTo() {
		final ItemDefinitionIdentifier itemDefinition = itemDefinitionOptionIdentifier.getItemDefinitionOptions().getItemDefinition();
		return Observable.just(ItemOptionSelectorIdentifier.builder()
				.withItemId(itemDefinition.getItemId())
				.withOptionId(itemDefinitionOptionIdentifier.getOptionId())
				.withScope(itemDefinition.getScope())
				.build());
	}
}
