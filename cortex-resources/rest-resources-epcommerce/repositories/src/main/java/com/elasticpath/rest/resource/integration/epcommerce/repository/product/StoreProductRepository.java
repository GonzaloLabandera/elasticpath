/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.product;

import java.util.List;

import io.reactivex.Single;

import com.elasticpath.domain.catalogview.StoreProduct;


/**
 * Repository for {@link StoreProduct}.
 */
public interface StoreProductRepository {

	/**
	 * Find store product with attributes.
	 *
	 * @param storeCode   the store code
	 * @param productGuid the product guid
	 * @return the store product
	 */
	Single<StoreProduct> findDisplayableStoreProductWithAttributesByProductGuid(String storeCode, String productGuid);

	/**
	 * Find store product with attributes for given sku guid.
	 *
	 * @param storeCode the store code
	 * @param skuGuid   the sku guid
	 * @return the store product
	 */
	Single<StoreProduct> findDisplayableStoreProductWithAttributesBySkuGuid(String storeCode, String skuGuid);

	/**
	 * Find a list of products by their uids.
	 *
	 * @param storeCode   the store code
	 * @param productUids list of product uids
	 * @return a list of store products
	 */
	List<StoreProduct> findByUids(String storeCode, List<Long> productUids);

}