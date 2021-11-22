/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.converters.XPFStructuredErrorMessageConverter;

/**
 * Validator to check that the cart modifiers are correct for a ShoppingItem.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT, priority = 1020)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, priority = 1030)
public class ModifierShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	@Autowired
	private CartModifierValidator cartModifierValidator;

	@Autowired
	private XPFStructuredErrorMessageConverter xpfStructuredErrorMessageConverter;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		XPFProduct product = context.getShoppingItem().getProductSku().getProduct();
		XPFShoppingItem xpfShoppingItem = context.getShoppingItem();

		Set<XPFModifierGroup> cartItemModifierGroups = Optional.ofNullable(product.getProductType())
				.map(XPFProductType::getModifierGroups)
				.orElse(Collections.emptySet());

		return cartModifierValidator.baseValidate(xpfShoppingItem.getModifierFields(), cartItemModifierGroups)
				.stream().map(structuredErrorMessage -> xpfStructuredErrorMessageConverter.convert(structuredErrorMessage))
				.collect(Collectors.toList());
	}
}
