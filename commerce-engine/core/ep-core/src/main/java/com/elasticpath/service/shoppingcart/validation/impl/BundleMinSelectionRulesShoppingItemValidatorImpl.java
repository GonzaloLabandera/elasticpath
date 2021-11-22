/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.pf4j.Extension;

import com.elasticpath.domain.shoppingcart.ShoppingItem;
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
 * Determines if the bundle in the cart meets the minimum requirements for constituents.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT, priority = 1030)
public class BundleMinSelectionRulesShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "bundle.does.not.contain.min.constituents";

	/**
	 * Validates the object.
	 *
	 * @param context object to be validated.
	 * @return a collection of Structured Error Messages containing validation errors, or an
	 * empty collection if the validation is successful.
	 */
	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {

		XPFProduct product = context.getShoppingItem().getProductSku().getProduct();
		XPFShoppingItem shoppingItem = context.getShoppingItem();

		long selectedQuantity = 0;
		long minQuantity = 0;
		if (product instanceof XPFProductBundle) {
			selectedQuantity = shoppingItem.getChildren().size();
			minQuantity = ((XPFProductBundle) product).getMinConstituentSelections();
		}

		if (selectedQuantity < minQuantity) {
			return Collections.singletonList(new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID,
					"Bundle does not contain the minimum number of required bundle constituents.", ImmutableMap
					.of("item-code", context.getShoppingItem().getProductSku().getCode(),
							"min-quantity", String.format("%d", minQuantity),
							"current-quantity", String.format("%d", selectedQuantity)),
					new XPFStructuredErrorResolution(ShoppingItem.class, shoppingItem.getGuid())));
		}
		return Collections.emptyList();
	}
}
