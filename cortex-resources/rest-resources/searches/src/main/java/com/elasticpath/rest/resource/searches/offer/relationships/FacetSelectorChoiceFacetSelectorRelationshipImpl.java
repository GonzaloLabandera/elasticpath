/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.searches.FacetSelectorChoiceFacetSelectorRelationship;
import com.elasticpath.rest.definition.searches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.searches.FacetSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 *  Link from facet selector choice to facet selector.
 */
public class FacetSelectorChoiceFacetSelectorRelationshipImpl implements FacetSelectorChoiceFacetSelectorRelationship.LinkTo {

	private final FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier;

	/**
	 * Constructor.
	 * @param facetSelectorChoiceIdentifier identifier
	 */
	@Inject
	public FacetSelectorChoiceFacetSelectorRelationshipImpl(@RequestIdentifier final FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier) {
		this.facetSelectorChoiceIdentifier = facetSelectorChoiceIdentifier;
	}

	@Override
	public Observable<FacetSelectorIdentifier> onLinkTo() {
		return Observable.just(FacetSelectorIdentifier.builder()
				.withFacet(facetSelectorChoiceIdentifier.getFacetValue().getFacet())
				.build());
	}
}
