/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.prices.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.prices.OfferPriceRangeEntity;
import com.elasticpath.rest.definition.prices.OfferPriceRangeIdentifier;
import com.elasticpath.rest.definition.prices.OfferPriceRangeResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for reading a price range for an offer.
 */
public class OfferPriceRangePrototype implements OfferPriceRangeResource.Read {

	private final Repository<OfferPriceRangeEntity, OfferPriceRangeIdentifier> repository;
	private final OfferPriceRangeIdentifier identifier;

	/**
	 * Constructor.
	 *
	 * @param repository             repository
	 * @param priceForItemIdentifier identifier
	 */
	@Inject
	public OfferPriceRangePrototype(@ResourceRepository final Repository<OfferPriceRangeEntity, OfferPriceRangeIdentifier> repository,
									@RequestIdentifier final OfferPriceRangeIdentifier priceForItemIdentifier) {
		this.repository = repository;
		this.identifier = priceForItemIdentifier;
	}


	@Override
	public Single<OfferPriceRangeEntity> onRead() {
		return repository.findOne(identifier);
	}
}
