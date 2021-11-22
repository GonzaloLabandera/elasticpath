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
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;

/**
 * Determines if the shopping item dto being added to cart has constituents that exceed the max limits.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, priority = 1050)
public class BundleMaxSelectionRulesShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "bundle.exceeds.max.constituents";

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {

		XPFShoppingItem shoppingItem = context.getShoppingItem();
		XPFProduct product = shoppingItem.getProductSku().getProduct();
		String skuCode = shoppingItem.getProductSku().getCode();

		long maxQuantity = 0;
		long selectedQuantity = 0;

		if (product instanceof XPFProductBundle) {
			maxQuantity = ((XPFProductBundle) product).getMaxConstituentSelections();
			selectedQuantity = shoppingItem.getChildren().size();
		}

		if (selectedQuantity > maxQuantity) {
			return Collections.singletonList(new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID,
					"Bundle contains more than the maximum number of allowed bundle constituents.", ImmutableMap
					.of("item-code", skuCode, "max-quantity",
							String.format("%d", maxQuantity), "current-quantity",
							String.format("%d", selectedQuantity)),

					new XPFStructuredErrorResolution(XPFShoppingItem.class, skuCode)));
		}
		return Collections.emptyList();
	}
}
