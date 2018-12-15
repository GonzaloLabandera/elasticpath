/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.prices.OfferPriceRangeEntity;
import com.elasticpath.rest.definition.prices.OfferPriceRangeIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Repository that implements retrieving price range for an offer.
 *
 * @param <I>  extends OfferPriceRangeEntity
 * @param <LI> extends OfferPriceRangeIdentifier
 */
@Component
public class OfferPriceRangeRepositoryImpl<I extends OfferPriceRangeEntity, LI extends OfferPriceRangeIdentifier>
		implements Repository<OfferPriceRangeEntity, OfferPriceRangeIdentifier> {

	private PriceRepository priceRepository;

	@Override
	public Single<OfferPriceRangeEntity> findOne(final OfferPriceRangeIdentifier identifier) {
		String guid = identifier.getOffer().getOfferId().getValue().get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		String scope = identifier.getOffer().getScope().getValue();
		return priceRepository.getPriceRange(scope, guid);
	}

	@Reference
	public void setPriceRepository(final PriceRepository priceRepository) {
		this.priceRepository = priceRepository;
	}
}
