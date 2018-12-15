/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.catalogview;

import java.util.Map;

import com.elasticpath.domain.skuconfiguration.SkuOptionValue;

/**
 * The SkuOptionValueFilter represents the filter on sku option values.
 */
public interface SkuOptionValueFilter extends Filter<SkuOptionValueFilter> {

	/**
	 * The constant for sku option value property key.
	 */
	String SKU_OPTION_VALUE_PROPERTY_KEY = "skuOptionValue";

	/**
	 * Get the set of sku option value.
	 *
	 * @return the set of sku option value
	 */
	SkuOptionValue getSkuOptionValue();

	/**
	 * Initialize with the given sku option value.
	 *
	 * @param properties the properties
	 */
	@Override
	void initialize(Map<String, Object> properties);

}