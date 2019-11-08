/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import java.util.List;

/**
 * Represents an interface to provide Catalog bulk processing.
 */
public interface AttributeCategoryBulkUpdateProcessor {

	/**
	 * Updates attribute display names in categories projection.
	 *
	 * @param categoryList list of source categories.
	 * @param attribute    updated attribute.
	 */
	void updateCategoryAttributeDisplayNameInCategories(List<String> categoryList, String attribute);
}
