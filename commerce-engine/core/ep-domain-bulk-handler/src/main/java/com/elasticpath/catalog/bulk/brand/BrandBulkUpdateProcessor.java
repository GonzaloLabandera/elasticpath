/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.brand;

import java.util.List;

/**
 * Represents an interface to provide Catalog bulk processing.
 */
public interface BrandBulkUpdateProcessor {

	/**
	 * Updates brand display names in offers projection.
	 *
	 * @param offers list of source offers.
	 * @param brand  updated brand.
	 */
	void updateBrandDisplayNamesInOffers(List<String> offers, String brand);

}
