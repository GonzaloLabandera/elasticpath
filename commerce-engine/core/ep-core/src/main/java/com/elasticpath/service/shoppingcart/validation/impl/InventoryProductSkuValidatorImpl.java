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
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;

/**
 * Product availability validator.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, priority = 1020)
public class InventoryProductSkuValidatorImpl extends XPFExtensionPointImpl implements ProductSkuValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "item.insufficient.inventory";

	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	private ProductSkuLookup productSkuLookup;
	@Autowired
	private SuperInventoryValidator superInventoryValidator;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFProductSkuValidationContext context) {
		final ProductSku productSku = productSkuLookup.findBySkuCode(context.getProductSku().getCode());

		if (superInventoryValidator.availabilityIndependentOfInventory(productSku)) {
			return Collections.emptyList();
		}

		final ProductInventoryShoppingService productInventoryShoppingService =
				beanFactory.getSingletonBean(ContextIdNames.PRODUCT_INVENTORY_SHOPPING_SERVICE, ProductInventoryShoppingService.class);
		final StoreService storeService = beanFactory.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class);
		final Store store = storeService.findStoreWithCode(context.getShopper().getStore().getCode());
		final SkuInventoryDetails skuInventoryDetails = productInventoryShoppingService.getSkuInventoryDetails(productSku, store);

		if (skuInventoryDetails.hasSufficientUnallocatedQty()) {
			return Collections.emptyList();
		}

		return Collections.singletonList(new XPFStructuredErrorMessage(MESSAGE_ID,
				String.format("Item '%s' does not have sufficient inventory.", productSku.getSkuCode()),
				ImmutableMap.of("item-code", productSku.getSkuCode())));
	}
}
