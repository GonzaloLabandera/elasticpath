/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemValidator;

/**
 * Ensure that item is not added as a result of auto-selected bundle constituents.
 */
public class ShoppingItemNotAutoSelectedValidatorImpl implements ShoppingItemValidator {

	private static final String MESSAGE_ID = "cart.item.auto.selected.in.bundle";

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingItemValidationContext context) {
		if (context.getParentProductSku() != null && context.getParentProductSku().getProduct() instanceof ProductBundle) {
			ProductBundle productBundle = (ProductBundle) context.getParentProductSku().getProduct();
			if (isValidatedItemAutoSelectableConstituent(context.getProductSku(), productBundle)) {
				return Collections.singletonList(createErrorMessage(context.getProductSku().getSkuCode()));
			}
		}
		return Collections.emptyList();
	}

	private boolean isValidatedItemAutoSelectableConstituent(final ProductSku productSku, final ProductBundle productBundle) {
		return productBundle.getConstituents()
				.stream()
				.filter(productBundle::isConstituentAutoSelectable)
				.anyMatch(bundleConstituent -> productSku.equals(bundleConstituent.getConstituent().getProductSku()));
	}

	private StructuredErrorMessage createErrorMessage(final String skuCode) {
		return new StructuredErrorMessage(StructuredErrorMessageType.ERROR, MESSAGE_ID,
				String.format("Item '%s' is a bundle constituent that was automatically selected.", skuCode),
				ImmutableMap.of("item-code", skuCode));
	}

}
