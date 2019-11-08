/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.offer;

import java.util.List;

/**
 * Represents an interface to provide Catalog bulk processing.
 */
public interface OfferBulkUpdateProcessor {

	/**
	 * Updates offer projections.
	 *
	 * @param offerCodes list of offer codes to update.
	 */
	void updateOffers(List<String> offerCodes);

}
