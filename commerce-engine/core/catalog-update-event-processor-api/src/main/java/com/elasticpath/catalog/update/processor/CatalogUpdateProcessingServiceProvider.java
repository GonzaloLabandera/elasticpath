/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor;

/**
 * Provides {@link CatalogUpdateProcessingService} instances corresponding to the given parameters.
 */
public interface CatalogUpdateProcessingServiceProvider {

	/**
	 * Returns a {@link CatalogUpdateProcessingService} instance corresponding to the given parameter.
	 *
	 * @return a {@link CatalogUpdateProcessingService}
	 */
	CatalogUpdateProcessingService getCatalogUpdateService();

}
