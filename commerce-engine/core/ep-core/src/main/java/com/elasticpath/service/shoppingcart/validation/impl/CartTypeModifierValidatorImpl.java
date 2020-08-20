/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidator;

/**
 * Validator to check that the cart modifiers are correct for a CartType.
 */
public class CartTypeModifierValidatorImpl extends CartModifierValidator implements ShoppingCartValidator {

	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingCartValidationContext context) {
		ShoppingCart shoppingCart = context.getShoppingCart();
		Collection<CartType> shoppingCartTypes = shoppingCart.getStore().getShoppingCartTypes();
		Optional<CartType> cartTypeOptional = shoppingCartTypes.stream()
				.filter(carttype -> carttype.getName()
						.equals(multicartItemListTypeLocationProvider.getMulticartItemListTypeForStore(shoppingCart.getStore().getCode())))
				.findFirst();
		if (!cartTypeOptional.isPresent()) {
			return Collections.emptyList();
		}

		Map<String, String> shoppingCartFieldValues = new HashMap<>();
		Collection<CartData> values = shoppingCart.getCartData().values();
		values.forEach(cartData -> shoppingCartFieldValues.put(cartData.getKey(), cartData.getValue()));
		return baseValidate(shoppingCartFieldValues, new HashSet<>(cartTypeOptional.get().getModifiers()));
	}

	public void setMulticartItemListTypeLocationProvider(final MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider) {
		this.multicartItemListTypeLocationProvider = multicartItemListTypeLocationProvider;
	}
}
