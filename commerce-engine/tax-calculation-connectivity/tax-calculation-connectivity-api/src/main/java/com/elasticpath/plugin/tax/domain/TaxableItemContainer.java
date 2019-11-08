/**
 * Copyright (c) Elastic Path Software Inc., 2013-2014
 */
package com.elasticpath.plugin.tax.domain;

import com.elasticpath.plugin.tax.common.TaxableCacheKeyHash;

/**
 * Interface defining a container for {@link TaxableItem}s and associated context information.
 */
public interface TaxableItemContainer extends TaxItemContainer, TaxableCacheKeyHash {
	
	/**
	 * Gets the {@link TaxOperationContext}.
	 * 
	 * @return the tax operation context
	 */
	TaxOperationContext getTaxOperationContext();

}
