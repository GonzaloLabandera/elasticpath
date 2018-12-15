/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.repositories;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.availabilities.AvailabilityEntity;
import com.elasticpath.rest.definition.availabilities.AvailabilityForOfferIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Offer Availability Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class OfferAvailabilityEntityRepositoryImpl<E extends AvailabilityEntity, I extends AvailabilityForOfferIdentifier>
		implements Repository<AvailabilityEntity, AvailabilityForOfferIdentifier> {

	private ProductLookup productLookup;
	private StoreProductRepository storeProductRepository;
	private ConversionService conversionService;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Single<AvailabilityEntity> findOne(final AvailabilityForOfferIdentifier identifier) {
		final Map<String, String> offerIdMap = identifier.getOffer().getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		return reactiveAdapter.<Product>fromNullableAsSingle(() -> productLookup.findByGuid(productCode), "Product not found")
				.map(product -> product.getProductSkus().values())
				.flatMapObservable(Observable::fromIterable)
				.flatMapSingle(productSku -> getStoreProduct(identifier.getOffer().getScope().getValue(), productSku)
						.map(storeProduct -> convertStoreProductSkuToAvailabilityEntity(storeProduct, productSku)))
				.map(AvailabilityEntity::getState)
				.any("AVAILABLE"::equals)
				.map(availability -> AvailabilityEntity.builder()
						.withState(availability ? Availability.AVAILABLE.getName() : Availability.NOT_AVAILABLE.getName())
						.build());
	}

	/**
	 * Get the StoreProduct from the given ProductSku and scope.
	 *
	 * @param scope      scope
	 * @param productSku productSku
	 * @return the store product
	 */
	protected Single<StoreProduct> getStoreProduct(final String scope, final ProductSku productSku) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(scope, productSku.getProduct().getGuid());
	}

	/**
	 * Converts StoreProduct and ProductSku to an AvailabilityEntity.
	 *
	 * @param storeProduct storeProduct
	 * @param productSku   productSku
	 * @return AvailabilityEntity
	 */
	protected AvailabilityEntity convertStoreProductSkuToAvailabilityEntity(final StoreProduct storeProduct, final ProductSku productSku) {
		return conversionService.convert(new Pair<>(storeProduct, productSku), AvailabilityEntity.class);
	}


	@Reference
	public void setProductLookup(final ProductLookup productLookup) {
		this.productLookup = productLookup;
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}


	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
