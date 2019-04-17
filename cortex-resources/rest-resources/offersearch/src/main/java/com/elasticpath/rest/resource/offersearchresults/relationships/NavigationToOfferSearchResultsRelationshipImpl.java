/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.offersearches.NavigationToOfferSearchRelationship;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Link from navigation to offer search.
 */
public class NavigationToOfferSearchResultsRelationshipImpl implements NavigationToOfferSearchRelationship.LinkTo {

	private final LinksRepository<NavigationIdentifier, OfferSearchResultIdentifier> repository;
	private final NavigationIdentifier navigationIdentifier;

	/**
	 * Constructor.
	 * @param navigationIdentifier navigationIdentifier.
	 * @param repository repository.
	 */
	@Inject
	public NavigationToOfferSearchResultsRelationshipImpl(@RequestIdentifier final NavigationIdentifier navigationIdentifier,
														  @ResourceRepository final LinksRepository<NavigationIdentifier,
																  OfferSearchResultIdentifier> repository) {
		this.navigationIdentifier = navigationIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<OfferSearchResultIdentifier> onLinkTo() {
		return repository.getElements(navigationIdentifier);
	}
}
