/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsForComponentOptionRelationship;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition component option to item definition component options link.
 */
public class ComponentOptionToOptionsRelationshipImpl implements ItemDefinitionComponentOptionsForComponentOptionRelationship.LinkTo {

	private final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier
	 */
	@Inject
	public ComponentOptionToOptionsRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier) {
		this.itemDefinitionComponentOptionIdentifier = itemDefinitionComponentOptionIdentifier;
	}

	@Override
	public Observable<ItemDefinitionComponentOptionsIdentifier> onLinkTo() {
		return Observable.just(itemDefinitionComponentOptionIdentifier.getItemDefinitionComponentOptions());
	}
}
