/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview;

import com.elasticpath.domain.catalog.ProductSku;

/**
 * A representation of a SKU that reflects availability within a store.
 */
public interface StoreProductSku extends ProductSku, PerStoreProductSkuAvailability {

}
