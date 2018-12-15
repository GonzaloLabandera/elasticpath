/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Repository for working with the Item domain.
 */
public interface ItemRepository {

	/**
	 * Composite ID skuCode Key.
	 */
	String SKU_CODE_KEY = "S";

	/**
	 * Returns the id for an item based on the product's default sku.
	 * The product must have a default sku.
	 *
	 * @param product the product with the sku to build an item id from
	 * @return the item id for the product's default sku
	 */
	String getDefaultItemIdForProduct(Product product);

	/**
	 * Returns the id for an item based on the sku.
	 *
	 * @param productSku product sku to encode the id from
	 * @return an id that uniquely identifies the purchasable item
	 */
	String getItemIdForSku(ProductSku productSku);

	/**
	 * Returns the identifier for an item based on the sku.
	 *
	 * @param productSku product sku to encode the id from
	 * @return an id that uniquely identifies the purchasable item
	 */
	IdentifierPart<Map<String, String>> getItemIdForProductSku(ProductSku productSku);

	/**
	 * Returns the map-formatted identifier for an item based on the sku code.
	 *
	 * @param skuCode product sku code to retrieve and encode the id from
	 * @return an id that uniquely identifies the purchasable item
	 */
	IdentifierPart<Map<String, String>> getItemIdMap(String skuCode);

	/**
	 * Returns the product sku identified by the given itemIdMap.
	 *
	 * @param itemIdMap the item id map to search with
	 * @return the product sku identified by the itemId
	 */
	Single<ProductSku> getSkuForItemId(Map<String, String> itemIdMap);

	/**
	 * Checks if item is bundle.
	 *
	 * @param itemIdMap the item id map
	 * @return true if the item is a bundle, false otherwise
	 */
	Single<Boolean> isItemBundle(Map<String, String> itemIdMap);

	/**
	 * Returns a Single of ProductBundle based on the product.
	 *
	 * @param product a product
	 * @return a product bundle
	 */
	Single<ProductBundle> asProductBundle(Product product);

	/**
	 * Verifies whether SKU exists for given sku code.
	 *
	 * @param itemIdMap the item id map
	 * @return true if product sku exists
	 */
	Single<Boolean> isProductSkuExistForItemId(Map<String, String> itemIdMap);

	/**
	 * Returns the set of SkuOptions for the given itemIdMap.
	 *
	 * @param itemIdMap the item id map.
	 * @return Observable of Sku Options
	 */
	Observable<SkuOption> getSkuOptionsForItemId(Map<String, String> itemIdMap);

	/**
	 * Get the BundleConstituent at the end of guid path from root item.
	 *
	 * @param rootItemIdMap        rootItemIdMap
	 * @param guidPathFromRootItem a list of all guids from the root item to the component item
	 * @return the component
	 */
	Single<BundleConstituent> findBundleConstituentAtPathEnd(Map<String, String> rootItemIdMap, Iterator<String> guidPathFromRootItem);

	/**
	 * Get the next nested BundleConstituent if there is one.
	 *
	 * @param currBundleConstituent currBundleConstituent
	 * @param guidPathFromRootItem  guidPathFromRootItem
	 * @return nested BundleConstituent
	 */
	Single<BundleConstituent> getNestedBundleConstituent(BundleConstituent currBundleConstituent, Iterator<String> guidPathFromRootItem);

	/**
	 * Get the product bundle for a bundle constituent.
	 *
	 * @param bundleConstituent bundleConstituent
	 * @return the product bundle
	 */
	Single<ProductBundle> getProductBundleFromConstituent(BundleConstituent bundleConstituent);

	/**
	 * Get the bundle constituent with the given guid.
	 *
	 * @param bundleConstituents bundleConstituents
	 * @param guid               guid
	 * @return bundle constituent with the given guid
	 */
	Single<BundleConstituent> findBundleConstituentWithGuid(List<BundleConstituent> bundleConstituents, String guid);
}
