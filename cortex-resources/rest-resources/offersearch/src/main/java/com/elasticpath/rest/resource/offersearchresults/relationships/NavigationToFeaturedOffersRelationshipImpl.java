/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersIdentifier;
import com.elasticpath.rest.definition.offersearches.NavigationToFeaturedOffersRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Navigation to featured offers link.
 */
public class NavigationToFeaturedOffersRelationshipImpl implements NavigationToFeaturedOffersRelationship.LinkTo {

	private final NavigationIdentifier navigationIdentifier;
	private final LinksRepository<NavigationIdentifier, FeaturedOffersIdentifier> repository;

	/**
	 * Constructor.
	 * @param navigationIdentifier identifier
	 * @param repository repository
	 */
	@Inject
	public NavigationToFeaturedOffersRelationshipImpl(
			@RequestIdentifier final NavigationIdentifier navigationIdentifier,
			@ResourceRepository final LinksRepository<NavigationIdentifier, FeaturedOffersIdentifier> repository) {
		this.navigationIdentifier = navigationIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<FeaturedOffersIdentifier> onLinkTo() {
		return repository.getElements(navigationIdentifier);
	}
}
