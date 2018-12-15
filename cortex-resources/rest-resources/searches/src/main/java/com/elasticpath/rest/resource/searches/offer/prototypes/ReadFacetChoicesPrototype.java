/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.searches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.searches.FacetSelectorIdentifier;
import com.elasticpath.rest.definition.searches.FacetSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Read facet values.
 */
public class ReadFacetChoicesPrototype implements FacetSelectorResource.Choices {

	private final FacetSelectorIdentifier facetSelectorIdentifier;
	private final SelectorRepository<FacetSelectorIdentifier, FacetSelectorChoiceIdentifier> repository;

	/**
	 * Constructor.
	 * @param facetSelectorIdentifier identifier
	 * @param repository repository
	 */
	@Inject
	public ReadFacetChoicesPrototype(@RequestIdentifier final FacetSelectorIdentifier facetSelectorIdentifier,
									 @ResourceRepository final SelectorRepository<FacetSelectorIdentifier,
			FacetSelectorChoiceIdentifier> repository) {
		this.facetSelectorIdentifier = facetSelectorIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return repository.getChoices(facetSelectorIdentifier);
	}
}
