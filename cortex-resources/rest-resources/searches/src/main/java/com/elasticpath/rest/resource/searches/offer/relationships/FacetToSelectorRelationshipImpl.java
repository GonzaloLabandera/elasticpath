/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.searches.FacetIdentifier;
import com.elasticpath.rest.definition.searches.FacetSelectorIdentifier;
import com.elasticpath.rest.definition.searches.FacetToSelectorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from facet to facet selector.
 */
public class FacetToSelectorRelationshipImpl implements FacetToSelectorRelationship.LinkTo {

	private final FacetIdentifier facetIdentifier;

	/**
	 * Constructor.
	 * @param facetIdentifier identifier
	 */
	@Inject
	public FacetToSelectorRelationshipImpl(@RequestIdentifier final FacetIdentifier facetIdentifier) {
		this.facetIdentifier = facetIdentifier;
	}

	@Override
	public Observable<FacetSelectorIdentifier> onLinkTo() {
		return Observable.just(FacetSelectorIdentifier.builder()
				.withFacet(facetIdentifier)
				.build());
	}
}
