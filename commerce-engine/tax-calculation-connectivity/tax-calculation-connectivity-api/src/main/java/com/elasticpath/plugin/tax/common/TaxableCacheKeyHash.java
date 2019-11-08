/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.plugin.tax.common;

/**
 * Interface marker indicating that the object contains a cache key hash method for tax lookups.
 */
public interface TaxableCacheKeyHash {
	/**
	 * Returns a hash code that can be used to identify cached tax results.
	 * @return a hash code that can be used to identify cached tax results
	 */
	int getTaxCacheKeyHash();
}
