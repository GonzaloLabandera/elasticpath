/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.OptionsForItemDefinitionComponentRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition component options to item definition component link.
 */
public class ComponentOptionsToComponentRelationshipImpl implements OptionsForItemDefinitionComponentRelationship.LinkFrom {

	private final ItemDefinitionComponentOptionsIdentifier itemDefinitionComponentOptionsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionsIdentifier itemDefinitionComponentOptionsIdentifier
	 */
	@Inject
	public ComponentOptionsToComponentRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentOptionsIdentifier itemDefinitionComponentOptionsIdentifier) {
		this.itemDefinitionComponentOptionsIdentifier = itemDefinitionComponentOptionsIdentifier;
	}

	@Override
	public Observable<ItemDefinitionComponentIdentifier> onLinkFrom() {
		return Observable.just(itemDefinitionComponentOptionsIdentifier.getItemDefinitionComponent());
	}
}
