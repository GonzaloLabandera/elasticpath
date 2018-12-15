/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.product.option;

import io.reactivex.Single;

import com.elasticpath.domain.skuconfiguration.SkuOptionValue;

/**
 * Repository for core sku option service.
 */
public interface SkuOptionRepository {

	/**
	 * Finds a sku option value by the name/value keys.
	 *
	 * @param skuOptionNameKey the name key (ie size).
	 * @param skuOptionValueKey the value key (ie medium).
	 * @return the sku option value
	 */
	Single<SkuOptionValue> findSkuOptionValueByKey(String skuOptionNameKey, String skuOptionValueKey);
}
