/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.common.dto.InventoryDetails;
import com.elasticpath.domain.catalog.InventoryCalculator;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.service.catalog.ProductInventoryManagementService;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemDtoValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemDtoValidator;

/**
 * Ensure that there is sufficient inventory for the quantity specified when adding to cart.
 */
public class InventoryShoppingItemDtoValidatorImpl extends SuperInventoryValidator implements ShoppingItemDtoValidator {

	private static final String MESSAGE_ID = "item.insufficient.inventory";

	private InventoryCalculator inventoryCalculator;
	private ProductInventoryManagementService productInventoryManagementService;

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingItemDtoValidationContext context) {
		final ProductSku productSku = context.getProductSku();

		if (availabilityIndependentOfInventory(productSku)) {
			return Collections.emptyList();
		}

		final ShoppingCart shoppingCart = context.getShoppingCart();
		long neededQuantity = context.getShoppingItemDto().getQuantity();

		if (!context.isUpdate()) {
			neededQuantity += shoppingCart.getCartItemsBySkuGuid(productSku.getGuid()).stream().mapToLong(ShoppingItem::getQuantity).sum();
		}

		long quantityInStock = 0;

		for (Warehouse warehouse : context.getStore().getWarehouses()) {
			final long warehouseUId = warehouse.getUidPk();

			final InventoryDetails inventoryDetails = inventoryCalculator.getInventoryDetails(productInventoryManagementService, productSku,
					warehouseUId);

			quantityInStock += inventoryDetails.getAvailableQuantityInStock();
		}

		if (quantityInStock < neededQuantity) {
			StructuredErrorMessage errorMessage = new StructuredErrorMessage(StructuredErrorMessageType.ERROR, MESSAGE_ID,
					String.format("Item '%s' only has %d available but %d were requested.", productSku.getSkuCode(),
							quantityInStock, neededQuantity),
					ImmutableMap.of("item-code", productSku.getSkuCode(),
							"quantity-requested", String.format("%d", neededQuantity),
							"inventory-available", String.format("%d", quantityInStock)));
			return Collections.singletonList(errorMessage);
		}

		return Collections.emptyList();
	}

	protected InventoryCalculator getInventoryCalculator() {
		return inventoryCalculator;
	}

	public void setInventoryCalculator(final InventoryCalculator inventoryCalculator) {
		this.inventoryCalculator = inventoryCalculator;
	}

	protected ProductInventoryManagementService getProductInventoryManagementService() {
		return productInventoryManagementService;
	}

	public void setProductInventoryManagementService(final ProductInventoryManagementService productInventoryManagementService) {
		this.productInventoryManagementService = productInventoryManagementService;
	}

}
