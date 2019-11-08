/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.capabilities;

import java.util.List;

import com.elasticpath.catalog.update.processor.CatalogUpdateProcessorCapability;
import com.elasticpath.domain.catalog.Category;

/**
 * Catalog Update Service capability for processing {@link Category} update notifications.
 */
public interface CategoryUpdateProcessor extends CatalogUpdateProcessorCapability {

	/**
	 * Process a {@link Category} creation event.
	 *
	 * @param category the Category that was created.
	 */
	void processCategoryCreated(Category category);

	/**
	 * Process a {@link Category} update event.
	 *
	 * @param category the Category that was updated.
	 */
	void processCategoryUpdated(Category category);

	/**
	 * Process a {@link Category} deletion event.
	 *
	 * @param guid the Category guid that was deleted.
	 */
	void processCategoryDeleted(String guid);

	/**
	 * Process a {@link Category} linked event.
	 *
	 * @param category the Category that was linked.
	 */
	void processCategoryLinked(Category category);

	/**
	 * Process a linked {@link Category} update event.
	 *
	 * @param category the Category that was updated.
	 * @param stores   {@link Category} stores.
	 */
	void processCategoryIncludedExcluded(Category category, List<String> stores);

	/**
	 * Process a {@link Category} unlinked event.
	 *
	 * @param code   a {@link Category} code.
	 * @param stores {@link Category} stores.
	 */
	void processCategoryUnlinked(String code, List<String> stores);

}
