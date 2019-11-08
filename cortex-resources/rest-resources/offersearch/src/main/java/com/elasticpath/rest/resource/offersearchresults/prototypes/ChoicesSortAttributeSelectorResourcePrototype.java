/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Choices for sort attribute selector.
 */
public class ChoicesSortAttributeSelectorResourcePrototype implements SortAttributeSelectorResource.Choices {

	private final SortAttributeSelectorIdentifier identifier;

	private final SelectorRepository<SortAttributeSelectorIdentifier, SortAttributeSelectorChoiceIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier identifier
	 * @param repository repository
	 */
	@Inject
	public ChoicesSortAttributeSelectorResourcePrototype(
			@RequestIdentifier final SortAttributeSelectorIdentifier identifier,
			@ResourceRepository final SelectorRepository<SortAttributeSelectorIdentifier, SortAttributeSelectorChoiceIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return repository.getChoices(identifier);
	}
}
