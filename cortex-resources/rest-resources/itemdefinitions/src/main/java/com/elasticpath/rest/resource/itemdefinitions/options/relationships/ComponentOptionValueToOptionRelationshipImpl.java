/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ValueForItemDefinitionComponentOptionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition component option value to item definition component option link.
 */
public class ComponentOptionValueToOptionRelationshipImpl implements ValueForItemDefinitionComponentOptionRelationship.LinkFrom {

	private final ItemDefinitionComponentOptionValueIdentifier itemDefinitionComponentOptionValueIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionComponentOptionValueIdentifier itemDefinitionComponentOptionValueIdentifier
	 */
	@Inject
	public ComponentOptionValueToOptionRelationshipImpl(
			@RequestIdentifier final ItemDefinitionComponentOptionValueIdentifier itemDefinitionComponentOptionValueIdentifier) {
		this.itemDefinitionComponentOptionValueIdentifier = itemDefinitionComponentOptionValueIdentifier;
	}

	@Override
	public Observable<ItemDefinitionComponentOptionIdentifier> onLinkFrom() {
		return Observable.just(itemDefinitionComponentOptionValueIdentifier.getItemDefinitionComponentOption());
	}
}
