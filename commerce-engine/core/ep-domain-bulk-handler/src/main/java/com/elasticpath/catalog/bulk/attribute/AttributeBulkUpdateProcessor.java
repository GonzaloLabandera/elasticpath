/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import java.util.List;

/**
 * Represents an interface to provide Catalog bulk processing.
 */
public interface AttributeBulkUpdateProcessor {

	/**
	 * Updates attribute display name in offers projection.
	 *
	 * @param offers    list of source offers.
	 * @param attribute updated attribute.
	 */
	void updateAttributeDisplayNameInOffers(List<String> offers, String attribute);
}
