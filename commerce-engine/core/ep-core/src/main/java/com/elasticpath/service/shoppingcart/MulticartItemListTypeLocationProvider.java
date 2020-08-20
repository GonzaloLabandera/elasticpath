/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.shoppingcart;

/**
 * Provides cart types based on stores and other contextual information.
 */
public interface MulticartItemListTypeLocationProvider {

	/**
	 * Get the multicart item list type for a store.
	 *
	 * @param storeCode the store code
	 * @return the multicart item list type for store
	 */
	String getMulticartItemListTypeForStore(String storeCode);
}
