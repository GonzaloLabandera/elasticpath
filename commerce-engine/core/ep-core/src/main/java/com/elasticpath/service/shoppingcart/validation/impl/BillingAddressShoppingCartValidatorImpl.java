/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Determines if a billing address has been specified (if cart contains physical skus).
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT, priority = 1040)
public class BillingAddressShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "need.billing.address";

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {

		final XPFShoppingCart shoppingCart = context.getShoppingCart();

		if (shoppingCart.getBillingAddress() == null) {
			XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID,
					"Billing address must be specified.", Collections.emptyMap(),
					new XPFStructuredErrorResolution(ShoppingCart.class, shoppingCart.getGuid()));
			return Collections.singletonList(errorMessage);
		}

		return Collections.emptyList();
	}

}
