/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.EnumSet;

import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;

/**
 * A super class collecting functionality common to all inventory validators.
 */
public class SuperInventoryValidator {
	private static final EnumSet<AvailabilityCriteria> CRITERIA_NOT_REQUIRE_INVENTORY_FOR_AVAILABILITY =
			EnumSet.of(AvailabilityCriteria.ALWAYS_AVAILABLE,
					AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER,
					AvailabilityCriteria.AVAILABLE_FOR_BACK_ORDER);

	/**
	 * Determine if the passed sku should be considered available regardless of its inventory count.
	 *
	 * @param productSku the product sku to check
	 * @return true if the sku is available regardless of inventory
	 */
	protected boolean availabilityIndependentOfInventory(final ProductSku productSku) {
		if (!productSku.isShippable() || productSku.getProduct() instanceof ProductBundle) {
			return true;
		}

		return CRITERIA_NOT_REQUIRE_INVENTORY_FOR_AVAILABILITY
				.contains(productSku.getProduct().getAvailabilityCriteria());
	}
}
