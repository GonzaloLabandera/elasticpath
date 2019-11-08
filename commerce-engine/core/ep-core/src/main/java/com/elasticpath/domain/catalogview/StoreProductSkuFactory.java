/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview;

import com.elasticpath.domain.catalog.ProductSku;

/**
 * Factory for producing new {@link StoreProductSku} instances.
 */
public interface StoreProductSkuFactory {

	/**
	 * Creates a {@link StoreProductSku} instance from a union of the given {@link ProductSku} and {@link PerStoreProductSkuAvailability}.
	 *
	 * @param productSku   the product SKU component of the {@link StoreProductSku}
	 * @param availability the per-store availability component of the {@link StoreProductSku}
	 * @return a new {@link StoreProductSku} instance
	 */
	StoreProductSku createStoreProductSku(ProductSku productSku, PerStoreProductSkuAvailability availability);

}
