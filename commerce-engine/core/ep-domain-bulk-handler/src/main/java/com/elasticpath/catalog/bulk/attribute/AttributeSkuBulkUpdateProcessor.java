/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import java.util.List;

/**
 * Represents an interface to provide Catalog bulk processing.
 */
public interface AttributeSkuBulkUpdateProcessor {

	/**
	 * Updates attribute display names in skus projection.
	 *
	 * @param skuList   list of source sku.
	 * @param attribute updated attribute.
	 */
	void updateSkuAttributeDisplayNameInOffers(List<String> skuList, String attribute);
}
