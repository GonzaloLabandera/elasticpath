/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;

/**
 * Builder for {@code com.elasticpath.xpf.connectivity.context.ProductSkuValidationContext}.
 */
public interface ProductSkuValidationContextBuilder {

	/**
	 * Builds {@code com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext} using inputs provide.
	 *
	 * @param productSku the product sku
	 * @param parentProductSku the parent product sku of the product sku specified
	 * @param shopper the shopper
	 * @param store the store
	 *
	 * @return ProductSkuValidationContext built using the inputs provided.
	 */
	XPFProductSkuValidationContext build(ProductSku productSku, ProductSku parentProductSku, Shopper shopper, Store store);

	/**
	 * Builds {@code com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext} based on
	 * {@code com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext}.
	 *
	 * @param context shopping item validation context
	 *
	 * @return ProductSkuValidationContext built using the inputs provided.
	 */
	XPFProductSkuValidationContext build(XPFShoppingItemValidationContext context);
}
