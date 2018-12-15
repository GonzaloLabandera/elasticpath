/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsForItemDefinitionOptionRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition option to item definition options link.
 */
public class DefinitionOptionToOptionsRelationshipImpl implements ItemDefinitionOptionsForItemDefinitionOptionRelationship.LinkTo {

	private final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionIdentifier itemDefinitionOptionIdentifier
	 */
	@Inject
	public DefinitionOptionToOptionsRelationshipImpl(@RequestIdentifier final ItemDefinitionOptionIdentifier itemDefinitionOptionIdentifier) {
		this.itemDefinitionOptionIdentifier = itemDefinitionOptionIdentifier;
	}

	@Override
	public Observable<ItemDefinitionOptionsIdentifier> onLinkTo() {
		return Observable.just(itemDefinitionOptionIdentifier.getItemDefinitionOptions());
	}
}
