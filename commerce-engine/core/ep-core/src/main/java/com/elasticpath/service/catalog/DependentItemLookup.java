/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.catalog;

import java.util.Map;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;

/**
 * Service which retrieves Dependent Items for a given SKU. Depending on the implementation, methods may be retrieved from cache.
 */
public interface DependentItemLookup {
	/**
	 * Gets all Dependent Items SKU Codes, and the quantities to add for the given store and parent sku.
	 *
	 * @param store the {@link Store}
	 * @param parentSku the {@link ProductSku} of parent item
	 * @return a map of Dependent Item SKU codes to their respective quantities; never {@code null}.
	 */
	Map<String, Integer> findDependentItemsForSku(Store store, ProductSku parentSku);
}
