/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Set;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemDtoValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemDtoValidator;

/**
 * Validator to check that the cart modifiers are correct for a ShoppingItemDto.
 */
public class CartItemModifierShoppingItemDtoValidatorImpl extends CartModifierValidator implements ShoppingItemDtoValidator {

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingItemDtoValidationContext context) {
		ProductSku productSku = context.getProductSku();

		Set<ModifierGroup> modifierGroups = productSku.getProduct().getProductType().getModifierGroups();
		return baseValidate(context.getShoppingItemDto().getItemFields(), modifierGroups);
	}

}
