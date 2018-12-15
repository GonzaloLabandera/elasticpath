/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.GenericSelectorRepository;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * READ operation on the {@link ItemOptionSelectorResource} creating the choices links.
 */
public class ChoicesForItemOptionSelectorPrototype implements ItemOptionSelectorResource.Choices {

	private final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier;

	private final GenericSelectorRepository<ItemOptionSelectorIdentifier, ItemOptionSelectorChoiceIdentifier, ItemIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemOptionSelectorIdentifier itemOptionSelectorIdentifier
	 * @param repository                   repository
	 */
	@Inject
	public ChoicesForItemOptionSelectorPrototype(
			@RequestIdentifier final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier,
			@ResourceRepository final GenericSelectorRepository<ItemOptionSelectorIdentifier, ItemOptionSelectorChoiceIdentifier,
					ItemIdentifier> repository) {
		this.itemOptionSelectorIdentifier = itemOptionSelectorIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return repository.getChoices(itemOptionSelectorIdentifier);
	}
}
