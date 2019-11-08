/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultToSortAttributeSelectorRelationship;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Link from offer search result to sort attributes selector.
 */
public class OfferSearchResultToSortAttributeSelectorRelationshipImpl implements OfferSearchResultToSortAttributeSelectorRelationship.LinkTo {

	private final OfferSearchResultIdentifier identifier;

	private final LinksRepository<OfferSearchResultIdentifier, SortAttributeSelectorIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier identifier
	 * @param repository repository
	 */
	@Inject
	public OfferSearchResultToSortAttributeSelectorRelationshipImpl(
			@RequestIdentifier final OfferSearchResultIdentifier identifier,
			@ResourceRepository final LinksRepository<OfferSearchResultIdentifier, SortAttributeSelectorIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<SortAttributeSelectorIdentifier> onLinkTo() {
		return repository.getElements(identifier);
	}
}
