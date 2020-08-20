/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.product.impl;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.catalogview.StoreProductService;

/**
 * Repository for {@link StoreProduct}s.
 */
@Singleton
@Named("storeProductRepository")
public class StoreProductRepositoryImpl implements StoreProductRepository {

	private static final String ERROR_STORE_PRODUCT_NOT_FOUND = "Offer with GUID %s was not found in store %s.";

	private final StoreRepository storeRepository;
	private final ProductLookup coreProductLookup;
	private final StoreProductService coreStoreProductService;
	private final ProductSkuRepository productSkuRepository;
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Instantiates a new store product repository impl.
	 *
	 * @param storeRepository         the store repository
	 * @param coreProductLookup       the core product lookup
	 * @param coreStoreProductService the core store product service
	 * @param productSkuRepository    the product sku repository
	 * @param reactiveAdapter         reactiveAdapter
	 */
	@Inject
	StoreProductRepositoryImpl(
			@Named("storeRepository") final StoreRepository storeRepository,
			@Named("productLookup") final ProductLookup coreProductLookup,
			@Named("storeProductService") final StoreProductService coreStoreProductService,
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.storeRepository = storeRepository;
		this.coreProductLookup = coreProductLookup;
		this.coreStoreProductService = coreStoreProductService;
		this.productSkuRepository = productSkuRepository;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<StoreProduct> findDisplayableStoreProductWithAttributesByProductGuid(final String storeCode, final String productGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> (Product) coreProductLookup.findByGuid(productGuid),
				String.format(ERROR_STORE_PRODUCT_NOT_FOUND, productGuid, storeCode))
				.flatMap(product -> findDisplayableStoreProductWithAttributesForProduct(storeCode, product));
	}

	@Override
	public Single<StoreProduct> findDisplayableStoreProductWithAttributesBySkuGuid(final String storeCode, final String skuGuid) {
		return productSkuRepository.getProductSkuWithAttributesByGuid(skuGuid)
				.flatMap(productSku -> findDisplayableStoreProductWithAttributesForProduct(storeCode, productSku.getProduct()));
	}

	@Override
	public List<StoreProduct> findByUids(final String storeCode, final List<Long> productUids) {
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMap(store -> reactiveAdapter.fromService(() -> coreProductLookup.findByUids(productUids))
						.concatMapIterable(products -> products)
						.map(product -> Optional.ofNullable(findDisplayableStoreProduct(product, store)))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.toList())
				.blockingGet();
	}

	@CacheResult
	private Single<StoreProduct> findDisplayableStoreProductWithAttributesForProduct(final String storeCode, final Product product) {
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMap(store -> reactiveAdapter.fromServiceAsSingle(() -> findDisplayableStoreProduct(product, store),
						String.format(ERROR_STORE_PRODUCT_NOT_FOUND, product.getGuid(), storeCode)));
	}

	private StoreProduct findDisplayableStoreProduct(final Product product, final Store store) {
		final StoreProduct storeProduct = coreStoreProductService.getProductForStore(product, store);
		if (storeProduct != null && storeProduct.isProductDisplayable()) {
			return storeProduct;
		}
		return null;
	}

}
