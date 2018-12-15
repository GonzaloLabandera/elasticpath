/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.offer.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.searches.FacetsIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultToFacetsRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Link from offer search result to facets.
 */
public class OfferSearchResultToFacetsRelationshipImpl implements OfferSearchResultToFacetsRelationship.LinkTo {

	private final OfferSearchResultIdentifier offerSearchResultIdentifier;
	private final LinksRepository<OfferSearchResultIdentifier, FacetsIdentifier> repository;

	/**
	 * Constructor.
	 * @param offerSearchResultIdentifier identifier
	 * @param repository repository
	 */
	@Inject
	public OfferSearchResultToFacetsRelationshipImpl(
			@RequestIdentifier final OfferSearchResultIdentifier offerSearchResultIdentifier,
			@ResourceRepository final LinksRepository<OfferSearchResultIdentifier, FacetsIdentifier> repository) {
		this.offerSearchResultIdentifier = offerSearchResultIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<FacetsIdentifier> onLinkTo() {
		return repository.getElements(offerSearchResultIdentifier);
	}
}
