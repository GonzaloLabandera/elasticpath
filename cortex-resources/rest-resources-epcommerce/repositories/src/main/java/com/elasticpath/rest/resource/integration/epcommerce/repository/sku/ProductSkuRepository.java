/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.sku;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;

/**
 * Product SKU repository.
 */
public interface ProductSkuRepository {

	/**
	 * Gets the SKU with attributes based on the given sku code.
	 *
	 * @param skuCode the sku Code.
	 * @return the product sku.
	 */
	Single<ProductSku> getProductSkuWithAttributesByCode(String skuCode);

	/**
	 * Gets the product sku with attributes by the sku guid.
	 *
	 * @param skuGuid the sku guid
	 * @return the product sku with attributes by guid
	 */
	Single<ProductSku> getProductSkuWithAttributesByGuid(String skuGuid);

	/**
	 * Determines if a product associated to a product sku (identified by the skuGuid) is a product bundle.
	 *
	 * @param skuGuid product sku guid
	 * @return product is a product bundle
	 */
	Single<Boolean> isProductBundleByGuid(String skuGuid);

	/**
	 * Determines if a product associated to a product sku (identified by the skuCode) is a product bundle.
	 *
	 * @param skuCode product sku code
	 * @return product is a product bundle
	 */
	Single<Boolean> isProductBundleByCode(String skuCode);

	/**
	 * Verifies whether SKU exists for given sku code.
	 *
	 * @param skuCode sku code
	 * @return true if product sku exists
	 */
	Single<Boolean> isProductSkuExistByCode(String skuCode);

	/**
	 * Verifies whether SKU is displayable for the given store.
	 *
	 * @param productSkuCode product sku code
	 * @param storeCode store code
	 * @return true if product sku is displayable in this context
	 */
	Single<Boolean> isDisplayableProductSkuForStore(String productSkuCode, String storeCode);

	/**
	 * Gets the SKU options based on the given sku code.
	 *
	 * @param skuCode skuCode
	 * @return SKU options
	 */
	Observable<SkuOption> getProductSkuOptionsByCode(String skuCode);
}
