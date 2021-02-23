/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.StoreProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.service.catalogview.IndexProduct;
import com.elasticpath.service.catalogview.StoreProductService;

/**
 * Caching version of StoreProductService.
 */
public class CachingStoreProductServiceImpl implements StoreProductService {

	private StoreProductService fallbackService;

	private Cache<String, StoreProduct> productForStoreCache;

	@Override
	public StoreProduct getProductForStore(final long uidPk, final Store store, final boolean loadProductAssociations) {
		return fallbackService.getProductForStore(uidPk, store, loadProductAssociations);
	}

	@Override
	public List<StoreProduct> getProductsForStore(final List<Long> uidPks, final Store store, final boolean loadProductAssociations) {
		return fallbackService.getProductsForStore(uidPks, store, loadProductAssociations);
	}

	@Override
	public StoreProduct getProductForStore(final Product product, final Store store) {
		String cacheKey = store.getCode() + ":" + product.getGuid();
		return productForStoreCache.get(cacheKey, key -> fallbackService.getProductForStore(product, store));
	}


	@Override
	public Optional<StoreProductSku> getProductSkuForStore(final ProductSku productSku, final Store store) {
		return fallbackService.getProductSkuForStore(productSku, store);
	}

	@Override
	public Collection<IndexProduct> getIndexProducts(final Collection<Long> productUids,
													 final Collection<Store> stores, final FetchGroupLoadTuner fetchGroupLoadTuner) {
		return fallbackService.getIndexProducts(productUids, stores, fetchGroupLoadTuner);
	}

	@Override
	public IndexProduct getIndexProduct(final long uidPk, final FetchGroupLoadTuner loadTuner, final Collection<Store> stores) {
		return fallbackService.getIndexProduct(uidPk, loadTuner, stores);
	}

	@Override
	public IndexProduct createIndexProduct(final Product product, final Collection<Store> stores) {
		return fallbackService.createIndexProduct(product, stores);
	}

	public void setFallbackService(final StoreProductService fallbackService) {
		this.fallbackService = fallbackService;
	}

	public void setProductForStoreCache(final Cache<String, StoreProduct> productForStoreCache) {
		this.productForStoreCache = productForStoreCache;
	}
}
