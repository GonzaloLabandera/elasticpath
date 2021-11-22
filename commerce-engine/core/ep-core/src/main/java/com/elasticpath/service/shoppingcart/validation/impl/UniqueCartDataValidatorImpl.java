/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Validator to check that the cart modifier data are unique.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CART_CREATE_OR_UPDATE, priority = 1020)
public class UniqueCartDataValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	private static final String ERROR_MESSAGE = "Cart descriptor values are already in use by another shopping cart";
	private static final String ERROR_ID = "cart.descriptor.not-unique";

	@Autowired
	private BeanFactory beanFactory;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		final ShoppingCartService shoppingCartService = beanFactory.getSingletonBean(ContextIdNames.SHOPPING_CART_SERVICE,
				ShoppingCartService.class);

		XPFShoppingCart shoppingCart = context.getShoppingCart();

		Map<String, String> newCartData = shoppingCart.getModifierFields();
		XPFShopper shopper = shoppingCart.getShopper();
		String customerGuid = Optional.ofNullable(shopper.getUser()).map(XPFCustomer::getGuid).orElse(null);
		String accountSharedId = Optional.ofNullable(shopper.getAccount()).map(XPFCustomer::getSharedId).orElse(null);
		List<String> existingCartGuids = shoppingCartService.findByCustomerAndStore(customerGuid, accountSharedId, shopper.getStore().getCode())
				.stream()
				.filter(guid -> !shoppingCart.getGuid().equals(guid))
				.collect(Collectors.toList());
		Map<String, List<Map<String, String>>> cartDataForCarts = shoppingCartService.findCartDataForCarts(existingCartGuids);
		for (List<Map<String, String>> existingCartData : cartDataForCarts.values()) {
			if (hasSameCartData(existingCartData, newCartData)) {
				XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, Collections.emptyMap());
				return Collections.singletonList(errorMessage);
			}
		}
		return Collections.emptyList();
	}

	private boolean hasSameCartData(final List<Map<String, String>> existingCartData, final Map<String, String> newCartData) {

		if (existingCartData.isEmpty()) {
			return false;
		}

		for (Map<String, String> existingCartDatum : existingCartData) {
			if (!existingCartDatum.equals(newCartData)) {
				// different identifiers between existing cart data and new cart data or
				//has same identifier key (name), but different value (cart1, cart2)
				//return false right away, one of the identifiers is different.
				return false;
			}
		}
		return true; // returns true if all identifiers same.
	}
}
