/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.relationships;

import static com.elasticpath.rest.resource.pagination.constant.PaginationResourceConstants.FIRST_PAGE;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.searches.FacetsIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultToFacetsRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;

/**
 * Link from facets to offer search result
 */
public class FacetsToOfferSearchResultRelationshipImpl implements OfferSearchResultToFacetsRelationship.LinkFrom {

	private final FacetsIdentifier facetsIdentifier;

	/**
	 * Constructor.
	 * @param facetsIdentifier identifier
	 */
	@Inject
	public FacetsToOfferSearchResultRelationshipImpl(@RequestIdentifier final FacetsIdentifier facetsIdentifier) {
		this.facetsIdentifier = facetsIdentifier;
	}

	@Override
	public Observable<OfferSearchResultIdentifier> onLinkFrom() {
		return Observable.just(OfferSearchResultIdentifier.builder()
				.withSearchId(facetsIdentifier.getSearchId())
				.withSearches(facetsIdentifier.getSearches())
				.withPageId(IntegerIdentifier.of(FIRST_PAGE))
				.withAppliedFacets(facetsIdentifier.getAppliedFacets())
				.build());
	}
}
