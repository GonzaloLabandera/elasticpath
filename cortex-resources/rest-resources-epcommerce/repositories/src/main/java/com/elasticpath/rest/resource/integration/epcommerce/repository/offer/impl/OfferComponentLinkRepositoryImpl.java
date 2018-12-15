/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Repository that implements reading the offer components link for an offer.
 *
 * @param <I>  extends OfferIdentifier
 * @param <LI> extends OfferComponentsIdentifier
 */
@Component
public class OfferComponentLinkRepositoryImpl<I extends OfferIdentifier, LI extends OfferComponentsIdentifier>
		implements LinksRepository<OfferIdentifier, OfferComponentsIdentifier> {

	private StoreProductRepository storeProductRepository;

	@Override
	public Observable<OfferComponentsIdentifier> getElements(final OfferIdentifier identifier) {
		final Map<String, String> offerIdMap = identifier.getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		return storeProductRepository.findByGuid(productCode)
				.map(product -> product instanceof ProductBundle)
				.flatMapObservable(isBundle -> isBundle
						? Observable.just(OfferComponentsIdentifier.builder().withOffer(identifier).build())
						: Observable.empty());
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}

}
