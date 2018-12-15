/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.definition.itemselections.SelectorForItemDefinitionOptionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item option selector to item definition option link.
 */
public class SelectorToItemDefinitionOptionRelationshipImpl implements SelectorForItemDefinitionOptionRelationship.LinkFrom {

	private final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemOptionSelectorIdentifier itemOptionSelectorIdentifier
	 */
	@Inject
	public SelectorToItemDefinitionOptionRelationshipImpl(
			@RequestIdentifier final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier) {
		this.itemOptionSelectorIdentifier = itemOptionSelectorIdentifier;
	}

	@Override
	public Observable<ItemDefinitionOptionIdentifier> onLinkFrom() {
		return Observable.just(ItemDefinitionOptionIdentifier.builder()
				.withOptionId(itemOptionSelectorIdentifier.getOptionId())
				.withItemDefinitionOptions(ItemDefinitionOptionsIdentifier.builder()
						.withItemDefinition(ItemDefinitionIdentifier.builder()
								.withItemId(itemOptionSelectorIdentifier.getItemId())
								.withScope(itemOptionSelectorIdentifier.getScope())
								.build())
						.build())
				.build());
	}
}
