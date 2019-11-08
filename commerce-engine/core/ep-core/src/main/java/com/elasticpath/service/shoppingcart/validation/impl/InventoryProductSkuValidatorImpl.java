/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.shoppingcart.validation.ProductSkuValidationContext;
import com.elasticpath.service.shoppingcart.validation.ProductSkuValidator;

/**
 * Product availability validator.
 */
public class InventoryProductSkuValidatorImpl extends SuperInventoryValidator implements ProductSkuValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "item.insufficient.inventory";

	private ProductInventoryShoppingService productInventoryShoppingService;

	@Override
	public Collection<StructuredErrorMessage> validate(final ProductSkuValidationContext context) {

		final ProductSku productSku = context.getProductSku();

		if (availabilityIndependentOfInventory(productSku)) {
			return Collections.emptyList();
		}

		SkuInventoryDetails skuInventoryDetails = productInventoryShoppingService.getSkuInventoryDetails(productSku,
				context.getStore());

		if (skuInventoryDetails.hasSufficientUnallocatedQty()) {
			return Collections.emptyList();
		}

		return Collections.singletonList(new StructuredErrorMessage(MESSAGE_ID,
				String.format("Item '%s' does not have sufficient inventory.", productSku.getSkuCode()),
				ImmutableMap.of("item-code", context.getProductSku().getSkuCode())));
	}

	protected ProductInventoryShoppingService getProductInventoryShoppingService() {
		return productInventoryShoppingService;
	}

	public void setProductInventoryShoppingService(final ProductInventoryShoppingService productInventoryShoppingService) {
		this.productInventoryShoppingService = productInventoryShoppingService;
	}
}
