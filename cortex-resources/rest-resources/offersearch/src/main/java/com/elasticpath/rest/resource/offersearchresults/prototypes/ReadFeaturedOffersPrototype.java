/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read featured offers.
 */
public class ReadFeaturedOffersPrototype implements FeaturedOffersResource.Read {

	private final FeaturedOffersIdentifier featuredOffersIdentifier;

	private final LinksRepository<FeaturedOffersIdentifier, OfferIdentifier> repository;

	/**
	 * Constructor.
	 * @param featuredOffersIdentifier identifier
	 * @param repository repository
	 */
	@Inject
	public ReadFeaturedOffersPrototype(@RequestIdentifier final FeaturedOffersIdentifier featuredOffersIdentifier,
									   @ResourceRepository final LinksRepository<FeaturedOffersIdentifier, OfferIdentifier> repository) {
		this.featuredOffersIdentifier = featuredOffersIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<OfferIdentifier> onRead() {
		return repository.getElements(featuredOffersIdentifier);
	}
}
