/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.GenericSelectorRepository;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceResource;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * READ operation on the {@link ItemOptionSelectorChoiceResource} returning the description and the action link.
 */
public class ReadItemOptionSelectorChoicePrototype implements ItemOptionSelectorChoiceResource.Read {

	private final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier;

	private final GenericSelectorRepository<ItemOptionSelectorIdentifier, ItemOptionSelectorChoiceIdentifier, ItemIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier
	 * @param repository                         repository
	 */
	@Inject
	public ReadItemOptionSelectorChoicePrototype(
			@RequestIdentifier final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier,
			@ResourceRepository final GenericSelectorRepository<ItemOptionSelectorIdentifier, ItemOptionSelectorChoiceIdentifier,
					ItemIdentifier> repository) {
		this.itemOptionSelectorChoiceIdentifier = itemOptionSelectorChoiceIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<Choice> onRead() {
		return repository.getChoice(itemOptionSelectorChoiceIdentifier);
	}
}
