/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.capabilities;

import com.elasticpath.catalog.update.processor.CatalogUpdateProcessorCapability;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;

/**
 * Catalog Update Service capability for processing {@link Product} update notifications.
 */
public interface ProductUpdateProcessor extends CatalogUpdateProcessorCapability {

	/**
	 * Process a {@link Product} creation event.
	 *
	 * @param product                  the product that was created
	 * @param bundlesContainingProduct all bundles containing the created {@link Product}
	 */
	void processProductCreated(Product product, ProductBundle... bundlesContainingProduct);

	/**
	 * Process a {@link Product} update event.
	 *
	 * @param product                       the product that was updated
	 * @param bundlesContainingProductCodes all codes of bundles containing the updated {@link Product}
	 */
	void processProductUpdated(Product product, String... bundlesContainingProductCodes);

	/**
	 * Process a {@link Product} deletion event.
	 *
	 * @param guid                     the guid of the product that was deleted
	 * @param bundlesContainingProduct all bundles containing the deleted {@link Product}
	 */
	void processProductDeleted(String guid, ProductBundle... bundlesContainingProduct);

}
