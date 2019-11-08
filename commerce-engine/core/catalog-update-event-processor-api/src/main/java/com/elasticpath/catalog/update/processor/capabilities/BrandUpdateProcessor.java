/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.capabilities;

import com.elasticpath.catalog.update.processor.CatalogUpdateProcessorCapability;
import com.elasticpath.domain.catalog.Brand;

/**
 * Catalog Update Service capability for processing {@link Brand} update notifications.
 */
public interface BrandUpdateProcessor extends CatalogUpdateProcessorCapability {

	/**
	 * Process a {@link Brand} creation event.
	 *
	 * @param brand the Brand that was created
	 */
	void processBrandCreated(Brand brand);

	/**
	 * Process a {@link Brand} update event.
	 *
	 * @param brand the Brand that was updated
	 */
	void processBrandUpdated(Brand brand);

	/**
	 * Process a {@link Brand} deletion event.
	 *
	 * @param guid the Brand guid that was deleted
	 */
	void processBrandDeleted(String guid);

}
