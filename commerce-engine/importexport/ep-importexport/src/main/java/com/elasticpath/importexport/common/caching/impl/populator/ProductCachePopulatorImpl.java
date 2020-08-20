/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.importexport.common.caching.CachePopulator;
import com.elasticpath.importexport.common.dto.products.ProductDTO;
import com.elasticpath.importexport.common.dto.products.ProductSkuDTO;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Product cache populator.
 */
public class ProductCachePopulatorImpl implements CachePopulator<ProductDTO> {

	private ProductLookup cachingProductLookup;
	private CachePopulator<ProductSkuDTO> productSkuCachePopulator;

	@Override
	public void populate(final List<ProductDTO> dtos) {
		final List<String> productCodes = dtos.stream()
				.map(ProductDTO::getCode)
				.collect(Collectors.toList());
		cachingProductLookup.findByGuids(productCodes);
		populateProductSkuCaches(dtos);
	}

	private void populateProductSkuCaches(final List<ProductDTO> dtos) {
		final List<ProductSkuDTO> skuDtos = dtos.stream()
				.map(ProductDTO::getProductSkus)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		productSkuCachePopulator.populate(skuDtos);
	}

	public void setCachingProductLookup(final ProductLookup cachingProductLookup) {
		this.cachingProductLookup = cachingProductLookup;
	}

	public void setProductSkuCachePopulator(final CachePopulator<ProductSkuDTO> productSkuCachePopulator) {
		this.productSkuCachePopulator = productSkuCachePopulator;
	}
}
