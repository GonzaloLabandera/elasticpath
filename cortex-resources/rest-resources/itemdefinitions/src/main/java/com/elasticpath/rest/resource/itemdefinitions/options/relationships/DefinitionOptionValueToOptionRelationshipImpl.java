/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ValueForItemDefinitionOptionRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item definition option value to item definition option link.
 */
public class DefinitionOptionValueToOptionRelationshipImpl implements ValueForItemDefinitionOptionRelationship.LinkFrom {

	private final ItemDefinitionOptionValueIdentifier itemDefinitionOptionValueIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemDefinitionOptionValueIdentifier itemDefinitionOptionValueIdentifier
	 */
	@Inject
	public DefinitionOptionValueToOptionRelationshipImpl(
			@RequestIdentifier final ItemDefinitionOptionValueIdentifier itemDefinitionOptionValueIdentifier) {
		this.itemDefinitionOptionValueIdentifier = itemDefinitionOptionValueIdentifier;
	}

	@Override
	public Observable<ItemDefinitionOptionIdentifier> onLinkFrom() {
		return Observable.just(itemDefinitionOptionValueIdentifier.getItemDefinitionOption());
	}
}
