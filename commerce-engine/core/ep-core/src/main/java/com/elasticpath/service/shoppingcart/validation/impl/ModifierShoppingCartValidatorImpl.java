/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.validation.service.ModifierFieldValidationService;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFCartType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;
import com.elasticpath.xpf.converters.XPFStructuredErrorMessageConverter;

/**
 * Validator to check that the cart modifiers are correct for a CartType.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CART_CREATE_OR_UPDATE, priority = 1010)
public class ModifierShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "cart.missing.data";

	@Autowired
	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;

	@Autowired
	@Named("cachedModifierFieldValidationService")
	private ModifierFieldValidationService modifierFieldValidationService;

	@Autowired
	private XPFStructuredErrorMessageConverter converter;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		final XPFShoppingCart xpfShoppingCart = context.getShoppingCart();

		final XPFStore store = xpfShoppingCart.getShopper().getStore();

		final Optional<XPFCartType> cartTypeOptional = store.getCartTypes().stream()
				.filter(cartType -> multicartItemListTypeLocationProvider
						.getMulticartItemListTypeForStore(xpfShoppingCart.getShopper().getStore().getCode()).equals(cartType.getName()))
				.findFirst();

		if (!cartTypeOptional.isPresent()) {
			return Collections.emptySet();
		}

		final XPFCartType cartType = cartTypeOptional.get();
		final Set<XPFModifierGroup> modifierGroups = cartType.getModifierGroups();

		final Set<XPFModifierField> referentFields = modifierGroups.stream().map(XPFModifierGroup::getModifierFields)
				.flatMap(List::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		final String cartOrderGuid = context.getShoppingCart().getCartOrderGuid();

		final Map<String, String> itemsToValidate = xpfShoppingCart.getModifierFields();

		return modifierFieldValidationService.validate(itemsToValidate, referentFields,
						new StructuredErrorResolution(CartOrder.class, cartOrderGuid)).stream()
				.map(structuredErrorMessage -> converter.convert(structuredErrorMessage))
				.collect(Collectors.toList());
	}
}
