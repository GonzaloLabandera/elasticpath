/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offersearches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetValueIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetValueToFacetSelectorChoiceRelationship;
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