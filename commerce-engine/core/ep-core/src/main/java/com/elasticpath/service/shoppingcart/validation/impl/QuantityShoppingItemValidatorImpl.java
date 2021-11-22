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
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;

/**
 * Validator to check that at least minimum quantity of product is added.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, priority = 1020)
public class QuantityShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	/**
	 * minimum quantity that should be present.
	 */
	private static final int MINIMUM_QUANTITY = 1;

	/**
	 * minimum quantity that should be present for an UPDATE.
	 */
	private static final int MINIMUM_QUANTITY_FOR_UPDATE = 0;

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "field.invalid.minimum.value";

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		final long quantity = context.getShoppingItem().getQuantity();
		boolean thereIsError = false;
		int minQuantity = 0;
		if (XPFOperationEnum.UPDATE == context.getOperation() && quantity < MINIMUM_QUANTITY_FOR_UPDATE) {
			minQuantity = MINIMUM_QUANTITY_FOR_UPDATE;
			thereIsError = true;
		} else if (XPFOperationEnum.UPDATE != context.getOperation() && quantity < MINIMUM_QUANTITY) {
			minQuantity = MINIMUM_QUANTITY;
			thereIsError = true;
		}
		if (thereIsError) {
			return Collections.singletonList(new XPFStructuredErrorMessage(MESSAGE_ID,
					String.format("'quantity' value '%d' must be greater than or equal to '%d'.", quantity, minQuantity),
					ImmutableMap.of("item-code", context.getShoppingItem().getProductSku().getCode(),
							"field-name", "quantity",
							"min-value", String.format("%d", minQuantity),
							"invalid-value", String.format("%d", quantity))));
		}
		return Collections.emptyList();
	}
}
