/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.pf4j.Extension;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Determines if a valid shipping option has been specified (if cart contains physical SKUs).
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT, priority = 1020)
public class ShippingOptionShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	private static final String NEED_SHIPPING_OPTION_MESSAGE_ID = "need.shipping.option";

	private static final String INVALID_SHIPPING_OPTION_MESSAGE_ID = "invalid.shipping.option";

	private static final String SHIPPING_OPTIONS_UNAVAILABLE_MESSAGE_ID = "shipping.options.unavailable";

	/**
	 * Message ids for this validation.
	 */
	public static final Set<String> MESSAGE_IDS = ImmutableSet
			.of(NEED_SHIPPING_OPTION_MESSAGE_ID, INVALID_SHIPPING_OPTION_MESSAGE_ID, SHIPPING_OPTIONS_UNAVAILABLE_MESSAGE_ID);

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		final XPFShoppingCart xpfShoppingCart = context.getShoppingCart();

		if (xpfShoppingCart.isRequiresShipping()) {
			if (xpfShoppingCart.getSelectedShippingOption() == null) {
				XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO,
						NEED_SHIPPING_OPTION_MESSAGE_ID,
						"Shipping option must be specified.", Collections.emptyMap(),
						new XPFStructuredErrorResolution(ShoppingCart.class, xpfShoppingCart.getGuid()));
				return Collections.singletonList(errorMessage);
			}

			if (context.getAvailableShippingOptions() == null) {
				XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR,
						SHIPPING_OPTIONS_UNAVAILABLE_MESSAGE_ID,
						"There was a problem retrieving shipping options from the shipping service",
						Collections.emptyMap(), new XPFStructuredErrorResolution(ShoppingCart.class, xpfShoppingCart.getGuid()));
				return Collections.singletonList(errorMessage);
			}
			final XPFShippingOption selectedShippingOption = xpfShoppingCart.getSelectedShippingOption();
			checkNotNull(selectedShippingOption, "shoppingCart shipping option should have been verified and should exist.");

			if (!isValidShippingOption(context.getAvailableShippingOptions(), selectedShippingOption)) {
				XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR,
						INVALID_SHIPPING_OPTION_MESSAGE_ID, "Selected shipping option is not valid.",
						ImmutableMap.of(
								"shipping-option", selectedShippingOption.getCode()));
				return Collections.singletonList(errorMessage);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Returns whether a shipping option contained in the list of shipping options matches the given non-null one.
	 * It only searches by both shipping option code and carrier code fields for a match.
	 *
	 * @param validShippingOptions   the list of shipping options to search through.
	 * @param shippingOptionSelected the shipping option to find a match for, must not be {@code null}.
	 * @return {@code true} if a shipping option matches by both shipping option code and carrier code; {@code false} otherwise.
	 */
	private boolean isValidShippingOption(final Set<XPFShippingOption> validShippingOptions,
										  final XPFShippingOption shippingOptionSelected) {
		return isNotEmpty(validShippingOptions)
				&& validShippingOptions.stream().anyMatch(
				shippingOption -> shippingOption.getCode().equals(shippingOptionSelected.getCode())
						&& shippingOption.getCarrierCode().equals(shippingOptionSelected.getCarrierCode()));
	}
}
