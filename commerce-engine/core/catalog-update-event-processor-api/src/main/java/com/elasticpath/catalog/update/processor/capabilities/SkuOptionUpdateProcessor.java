/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.capabilities;

import com.elasticpath.catalog.update.processor.CatalogUpdateProcessorCapability;
import com.elasticpath.domain.skuconfiguration.SkuOption;

/**
 * Catalog Update Service capability for processing {@link SkuOption} update notifications.
 */
public interface SkuOptionUpdateProcessor extends CatalogUpdateProcessorCapability {

	/**
	 * Process a {@link SkuOption} creation event.
	 *
	 * @param skuOption the SKU Option that was created
	 */
	void processSkuOptionCreated(SkuOption skuOption);

	/**
	 * Process a {@link SkuOption} update event.
	 *
	 * @param skuOption the SKU Option that was updated
	 */
	void processSkuOptionUpdated(SkuOption skuOption);

	/**
	 * Process a {@link SkuOption} deletion event.
	 *
	 * @param guid the SKU Option guid that was deleted
	 */
	void processSkuOptionDeleted(String guid);

}
