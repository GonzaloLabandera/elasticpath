/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.importexport.common.caching.CachePopulator;
import com.elasticpath.importexport.common.dto.products.ProductSkuDTO;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Product sku cache populator.
 */
public class ProductSkuCachePopulatorImpl implements CachePopulator<ProductSkuDTO> {

	private ProductSkuLookup cachingProductSkuLookup;

	@Override
	public void populate(final List<ProductSkuDTO> dtos) {
		final int dtoSize = dtos.size();
		final List<String> guids = new ArrayList<>(dtoSize);
		final List<String> codes = new ArrayList<>(dtoSize);
		for (ProductSkuDTO dto : dtos) {
			guids.add(dto.getGuid());
			codes.add(dto.getSkuCode());
		}

		cachingProductSkuLookup.findByGuids(guids);
		cachingProductSkuLookup.findBySkuCodes(codes);
	}

	public void setCachingProductSkuLookup(final ProductSkuLookup cachingProductSkuLookup) {
		this.cachingProductSkuLookup = cachingProductSkuLookup;
	}
}
