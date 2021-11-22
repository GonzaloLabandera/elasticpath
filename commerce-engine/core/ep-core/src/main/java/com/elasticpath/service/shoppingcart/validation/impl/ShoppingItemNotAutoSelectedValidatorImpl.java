/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;

/**
 * Ensure that item is not added as a result of auto-selected bundle constituents.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_REMOVE_FROM_CART, priority = 1010)
public class ShoppingItemNotAutoSelectedValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	private static final String MESSAGE_ID = "cart.item.auto.selected.in.bundle";

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		XPFShoppingItem parentShoppingItem = context.getParentShoppingItem();
		if (parentShoppingItem != null && parentShoppingItem.getProductSku() != null
				&& parentShoppingItem.getProductSku().getProduct() instanceof XPFProductBundle) {
			XPFProductBundle productBundle = (XPFProductBundle) parentShoppingItem.getProductSku().getProduct();
			if (isValidatedItemAutoSelectableConstituent(context.getShoppingItem().getProductSku(), productBundle)) {
				return Collections.singletonList(createErrorMessage(context.getShoppingItem().getProductSku().getCode()));
			}
		}
		return Collections.emptyList();
	}

	private boolean isValidatedItemAutoSelectableConstituent(final XPFProductSku productSku, final XPFProductBundle productBundle) {
		for (XPFBundleConstituent constituent : productBundle.getConstituents()) {
			if (constituent.getProductSku() != null && productSku.getCode().equals(constituent.getProductSku().getCode())) {
				return true;
			}

			if (constituent.getProduct() != null && productSku.getProduct().getCode().equals(constituent.getProduct().getCode())) {
				return true;
			}
		}
		return false;
	}

	private XPFStructuredErrorMessage createErrorMessage(final String skuCode) {
		return new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR, MESSAGE_ID,
				String.format("Item '%s' is a bundle constituent that was automatically selected.", skuCode),
				ImmutableMap.of("item-code", skuCode));
	}

}
