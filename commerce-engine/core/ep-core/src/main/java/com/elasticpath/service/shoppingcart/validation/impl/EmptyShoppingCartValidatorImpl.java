/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Validates that shopping is not empty.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT, priority = 1010)
public class EmptyShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	private static final String MESSAGE_ID = "cart.empty";

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		if (context.getShoppingCart().getLineItems().isEmpty()) {
			XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID,
					"Shopping cart is empty.", Collections.emptyMap());
			return Collections.singletonList(errorMessage);
		}
		return Collections.emptyList();
	}

}
