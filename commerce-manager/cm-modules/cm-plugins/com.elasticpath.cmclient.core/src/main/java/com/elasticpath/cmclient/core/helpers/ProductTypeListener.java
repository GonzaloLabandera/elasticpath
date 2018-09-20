/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.helpers;

import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.domain.catalog.ProductType;

/**
 * Listener interface used to manage product type changes.
 */
public interface ProductTypeListener {

	/**
	 * Handle changed product type event.
	 *
	 * @param event the event
	 */
	void productTypeChange(ItemChangeEvent<ProductType> event);
}
