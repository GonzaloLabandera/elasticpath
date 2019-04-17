/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.offersearches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetSelectorChoiceResource;
import com.elasticpath.rest.definition.offersearches.FacetSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * Read a facet value.
 */
public class ReadFacetChoicePrototype implements FacetSelectorChoiceResource.Read {

	private final FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier;
	private final SelectorRepository<FacetSelectorIdentifier, FacetSelectorChoiceIdentifier> repository;

	/**
	 * Constructor.
	 * @param facetSelectorChoiceIdentifier identifier
	 * @param repository repository
	 */
	@Inject
	public ReadFacetChoicePrototype(@RequestIdentifier final FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier,
									@ResourceRepository final SelectorRepository<FacetSelectorIdentifier,
			FacetSelectorChoiceIdentifier> repository) {
		this.facetSelectorChoiceIdentifier = facetSelectorChoiceIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<Choice> onRead() {
		return repository.getChoice(facetSelectorChoiceIdentifier);
	}
}
