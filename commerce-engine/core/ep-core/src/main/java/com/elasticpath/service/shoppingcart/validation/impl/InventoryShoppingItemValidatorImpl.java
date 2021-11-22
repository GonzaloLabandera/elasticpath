/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;

/**
 * Ensure that there is still sufficient inventory for the quantity specified on the item in the cart.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, priority = 1060)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT, priority = 1040)
public class InventoryShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	private static final String MESSAGE_ID = "item.insufficient.inventory";

	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	private ProductSkuLookup productSkuLookup;
	@Autowired
	private SuperInventoryValidator superInventoryValidator;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		final ProductSku productSku = productSkuLookup.findBySkuCode(context.getShoppingItem().getProductSku().getCode());

		if (superInventoryValidator.availabilityIndependentOfInventory(productSku)) {
			return Collections.emptyList();
		}

		final ProductInventoryShoppingService productInventoryShoppingService =
				beanFactory.getSingletonBean(ContextIdNames.PRODUCT_INVENTORY_SHOPPING_SERVICE, ProductInventoryShoppingService.class);
		final StoreService storeService = beanFactory.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class);
		final XPFShoppingCart shoppingCart = context.getShoppingCart();

		final Store store = storeService.findStoreWithCode(context.getShoppingCart().getShopper().getStore().getCode());
		long neededQuantity = context.getShoppingItem().getQuantity();
		if (context.getOperation() != XPFOperationEnum.UPDATE) {
			neededQuantity += shoppingCart.getLineItems()
					.stream()
					.filter(item -> !item.getGuid().equals(context.getShoppingItem().getGuid()))
					.filter(item -> item.getProductSku().getCode().equals(context.getShoppingItem().getProductSku().getCode()))
					.mapToLong(XPFShoppingItem::getQuantity)
					.sum();
		}

		long quantityInStock = 0;
		final SkuInventoryDetails skuInventoryDetails = productInventoryShoppingService.getSkuInventoryDetails(productSku, store);
		quantityInStock += skuInventoryDetails.getAvailableQuantityInStock();

		if (quantityInStock < neededQuantity) {
			XPFStructuredErrorMessage.XPFStructuredErrorMessageBuilder xpfStructuredErrorMessageBuilder = XPFStructuredErrorMessage.builder()
					.withType(XPFStructuredErrorMessageType.ERROR)
					.withMessageId(MESSAGE_ID)
					.withDebugMessage(String.format("Item '%s' only has %d available but %d were requested.", productSku.getSkuCode(),
							quantityInStock, neededQuantity))
					.withData(ImmutableMap.of("item-code", productSku.getSkuCode(),
							"inventory-available", String.format("%d", quantityInStock),
							"quantity-requested", String.format("%d", neededQuantity)
					));
			if (context.getShoppingItem().getGuid() != null) {
				xpfStructuredErrorMessageBuilder.withResolution(new XPFStructuredErrorResolution(ShoppingItem.class,
						context.getShoppingItem().getGuid()));
			}

			XPFStructuredErrorMessage errorMessage = xpfStructuredErrorMessageBuilder.build();

			return Collections.singletonList(errorMessage);
		}

		return Collections.emptyList();
	}
}
