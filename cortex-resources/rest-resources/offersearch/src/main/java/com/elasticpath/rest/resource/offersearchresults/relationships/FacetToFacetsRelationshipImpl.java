/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offersearches.FacetIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetToFacetsRelationship;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from facet to facets.
 */
public class FacetToFacetsRelationshipImpl implements FacetToFacetsRelationship.LinkTo {

	private final FacetIdentifier facetIdentifier;

	/**
	 * Constructor.
	 * @param facetIdentifier identifier
	 */
	@Inject
	public FacetToFacetsRelationshipImpl(@RequestIdentifier final FacetIdentifier facetIdentifier) {
		this.facetIdentifier = facetIdentifier;
	}

	@Override
	public Observable<FacetsIdentifier> onLinkTo() {
		return Observable.just(facetIdentifier.getFacets());
	}
}
