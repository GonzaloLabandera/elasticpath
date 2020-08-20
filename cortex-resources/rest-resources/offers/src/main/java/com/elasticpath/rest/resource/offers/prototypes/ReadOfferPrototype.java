/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import java.util.Map;
import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offers.OfferResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Offer prototype for Read operation.
 */
public class ReadOfferPrototype implements OfferResource.Read {

	private final OfferIdentifier identifier;

	private final StoreProductRepository storeProductRepository;

	/**
	 * Constructor.
	 *
	 * @param identifier offer identifier
	 * @param storeProductRepository the store product repository
	 */
	@Inject
	public ReadOfferPrototype(@RequestIdentifier final OfferIdentifier identifier,
							  @ResourceRepository final StoreProductRepository storeProductRepository) {
		this.identifier = identifier;
		this.storeProductRepository = storeProductRepository;
	}

	@Override
	public Completable onRead() {
		final Map<String, String> offerIdMap = identifier.getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		final String scope = identifier.getScope().getValue();
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(scope, productCode)
				.ignoreElement();
	}
}
