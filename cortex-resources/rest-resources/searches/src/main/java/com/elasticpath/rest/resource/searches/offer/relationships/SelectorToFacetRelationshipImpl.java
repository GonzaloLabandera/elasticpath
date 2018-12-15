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
 * Link from selector to facet.
 */
public class SelectorToFacetRelationshipImpl implements FacetToSelectorRelationship.LinkFrom {

	private final FacetSelectorIdentifier facetSelectorIdentifier;

	/**
	 * Constructor.
	 * @param facetSelectorIdentifier identifier
	 */
	@Inject
	public SelectorToFacetRelationshipImpl(@RequestIdentifier final FacetSelectorIdentifier facetSelectorIdentifier) {
		this.facetSelectorIdentifier = facetSelectorIdentifier;
	}

	@Override
	public Observable<FacetIdentifier> onLinkFrom() {
		return Observable.just(facetSelectorIdentifier.getFacet());
	}
}
