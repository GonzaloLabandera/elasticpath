/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.OptionsForItemDefinitionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition options to item definition link.
 */
public class DefinitionOptionsToDefinitionRelationshipImpl implements OptionsForItemDefinitionRelationship.LinkFrom {

	private final ItemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier
	 */
	@Inject
	public DefinitionOptionsToDefinitionRelationshipImpl(@RequestIdentifier final ItemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier) {
		this.itemDefinitionOptionsIdentifier = itemDefinitionOptionsIdentifier;
	}

	@Override
	public Observable<ItemDefinitionIdentifier> onLinkFrom() {
		return Observable.just(itemDefinitionOptionsIdentifier.getItemDefinition());
	}
}
