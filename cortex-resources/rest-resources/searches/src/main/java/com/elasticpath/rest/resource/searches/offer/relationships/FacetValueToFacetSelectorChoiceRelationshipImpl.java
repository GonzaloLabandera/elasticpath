/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.searches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.searches.FacetValueIdentifier;
import com.elasticpath.rest.definition.searches.FacetValueToFacetSelectorChoiceRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from facet value to facet selector choice.
 */
public class FacetValueToFacetSelectorChoiceRelationshipImpl implements FacetValueToFacetSelectorChoiceRelationship.LinkTo {

	private final FacetValueIdentifier facetValueIdentifier;

	/**
	 * Constructor.
	 * @param facetValueIdentifier identifier
	 */
	@Inject
	public FacetValueToFacetSelectorChoiceRelationshipImpl(@RequestIdentifier final FacetValueIdentifier facetValueIdentifier) {
		this.facetValueIdentifier = facetValueIdentifier;
	}

	@Override
	public Observable<FacetSelectorChoiceIdentifier> onLinkTo() {
		return Observable.just(FacetSelectorChoiceIdentifier.builder()
				.withFacetValue(facetValueIdentifier)
				.build());
	}
}