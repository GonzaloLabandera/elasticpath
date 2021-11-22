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
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;

/**
 * Validator to check that the product is visible.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, priority = 1030)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_CHECKOUT, priority = 1020)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART, priority = 1020)
public class VisibleProductSkuValidatorImpl extends XPFExtensionPointImpl implements ProductSkuValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "item.not.visible";

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFProductSkuValidationContext context) {

		if (context.getProductSku().getProduct().isHidden()) {
			return Collections.singletonList(new XPFStructuredErrorMessage(MESSAGE_ID,
					String.format("Item '%s' is not visible.", context.getProductSku().getCode()),
					ImmutableMap.of("item-code", context.getProductSku().getCode())));
		}
		return Collections.emptyList();

	}
}
