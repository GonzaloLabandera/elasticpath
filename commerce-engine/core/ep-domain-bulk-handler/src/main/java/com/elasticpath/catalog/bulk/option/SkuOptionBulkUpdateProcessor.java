/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.option;

import java.util.List;

/**
 * Represents an interface to provide Catalog bulk processing.
 */
public interface SkuOptionBulkUpdateProcessor {

	/**
	 * Updates brand display names in offers projection.
	 *
	 * @param offers    list of source offers.
	 * @param skuOption updated skuOption.
	 */
	void updateSkuOptionDisplayNamesInOffers(List<String> offers, String skuOption);
}
