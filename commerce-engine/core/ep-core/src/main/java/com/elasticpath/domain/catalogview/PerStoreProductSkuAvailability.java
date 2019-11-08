/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview;

import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.service.catalogview.impl.InventoryMessage;

/**
 * Indicates the availability of a SKU within a store.
 */
public interface PerStoreProductSkuAvailability {

	/**
	 * Returns <code>true</code> if the SKU is available for purchase.
	 *
	 * @return <code>true</code> if the SKU is available for purchase, <code>false</code> otherwise
	 */
	boolean isProductSkuAvailable();

	/**
	 * Returns <code>true</code> if the SKU can be displayed for this store.
	 *
	 * @return <code>true</code> if the SKU is visible within this store, <code>false</code> otherwise
	 */
	boolean isProductSkuDisplayable();

	/**
	 * Gets the availability of this SKU.
	 *
	 * @return the availability of this SKU
	 */
	Availability getSkuAvailability();

	/**
	 * Returns the inventory details for this SKU.
	 *
	 * @return the inventory details for this SKU
	 */
	SkuInventoryDetails getInventoryDetails();

	/**
	 * Returns <code>true</code> if the SKU can be syndicated.
	 *
	 * @return <code>true</code> if the SKU can be syndicated, <code>false</code> otherwise
	 */
	boolean canSyndicate();

	/**
	 * Gets the inventory availability message code for this SKU.
	 *
	 * @return {@link InventoryMessage}
	 */
	default InventoryMessage getMessageCode() {
		return getInventoryDetails().getMessageCode();
	}

}
