/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offerdefinitions.repositories;

import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionEntity;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Offer Definition Entity Repository.
 *
 * @param <E> Offer Definition entity
 * @param <I> Offer Definition identifier
 */
@Component
public class OfferDefinitionEntityRepositoryImpl<E extends OfferDefinitionEntity, I extends OfferDefinitionIdentifier>
		implements Repository<OfferDefinitionEntity, OfferDefinitionIdentifier> {

	private ConversionService conversionService;
	private StoreProductRepository storeProductRepository;

	@Override
	public Single<OfferDefinitionEntity> findOne(final OfferDefinitionIdentifier identifier) {
		final Map<String, String> offerIdMap = identifier.getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		final String scope = identifier.getScope().getValue();
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(scope, productCode)
				.map(product -> conversionService.convert(product.getWrappedProduct(), OfferDefinitionEntity.class));
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}


	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}
}
