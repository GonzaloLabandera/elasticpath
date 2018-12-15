/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorForItemOptionSelectorChoiceRelationship;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Item option selector choice to item definition option selector link.
 */
public class OptionSelectorChoiceToSelectorRelationshipImpl implements ItemOptionSelectorForItemOptionSelectorChoiceRelationship.LinkTo {

	private final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier;

	/**
	 * Constructor.
	 *
	 * @param itemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier
	 */
	@Inject
	public OptionSelectorChoiceToSelectorRelationshipImpl(
			@RequestIdentifier final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier) {
		this.itemOptionSelectorChoiceIdentifier = itemOptionSelectorChoiceIdentifier;
	}

	@Override
	public Observable<ItemOptionSelectorIdentifier> onLinkTo() {
		return Observable.just(itemOptionSelectorChoiceIdentifier.getItemOptionSelector());
	}
}
