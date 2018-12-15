/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.prices.OfferPriceRangeIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Link repository that adds price range link if the item has a price.
 *
 * @param <I>  extends OfferIdentifier
 * @param <LI> extends OfferPriceRangeIdentifier
 */
@Component
public class PriceRangeToOfferRepositoryImpl<I extends OfferIdentifier, LI extends OfferPriceRangeIdentifier>
		implements LinksRepository<OfferIdentifier, OfferPriceRangeIdentifier> {

	private PriceRepository priceRepository;

	@Override
	public Observable<OfferPriceRangeIdentifier> getElements(final OfferIdentifier offerIdentifier) {
		String guid = offerIdentifier.getOfferId().getValue().get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		String scope = offerIdentifier.getScope().getValue();
		return priceRepository.priceExistsForProduct(scope, guid).flatMapObservable(priceExists -> priceExists
				? Observable.just(OfferPriceRangeIdentifier.builder().withOffer(offerIdentifier).build())
				: Observable.empty());
	}

	@Reference
	public void setPriceRepository(final PriceRepository priceRepository) {
		this.priceRepository = priceRepository;
	}
}
