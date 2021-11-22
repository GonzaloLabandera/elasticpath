/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;

/**
 * Determines if the bundle in the cart matches the bundle configuration.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, priority = 1040)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT, priority = 1050)
public class BundleStructureShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {
	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		XPFProduct product = context.getShoppingItem().getProductSku().getProduct();
		XPFShoppingItem shoppingItemDto = context.getShoppingItem();
		if (verifyDtoStructureEqualsBundleStructure(product, shoppingItemDto)) {
			return Collections.emptyList();
		}
		return Collections.singletonList(new XPFStructuredErrorMessage("item.invalid.bundle.structure",
				"Requested item configuration does not have a valid bundle structure.",
				ImmutableMap.of("item-code", context.getShoppingItem().getProductSku().getCode())));
	}

	private boolean verifyDtoStructureEqualsBundleStructure(final XPFProduct product, final XPFShoppingItem shoppingItem) {
		if (product.isBundle() && !shoppingItem.getChildren().isEmpty()) {
			XPFProductBundle bundle = (XPFProductBundle) product;
			for (XPFShoppingItem childShoppingItem : shoppingItem.getChildren()) {
				Optional<XPFBundleConstituent> correspondingBundleConstituent = getCorrespondingBundleConstituent(bundle,
						childShoppingItem.getProductSku().getProduct().getCode(), childShoppingItem.getProductSku().getCode());

				if (!correspondingBundleConstituent.isPresent()) {
					return false;
				}

				if (correspondingBundleConstituent.get().getProduct() != null
						&& !verifyDtoStructureEqualsBundleStructure(correspondingBundleConstituent.get().getProduct(), childShoppingItem)) {
					return false;
				}
			}
		}

		return true;
	}

	private Optional<XPFBundleConstituent> getCorrespondingBundleConstituent(final XPFProductBundle bundle,
																			 final String productCode,
																			 final String skuCode) {
		return bundle.getConstituents().stream()
				.filter(bundleConstituent -> {
					if (bundleConstituent.getProduct() != null) {
						return bundleConstituent.getProduct().getCode().equals(productCode);
					}
					if (bundleConstituent.getProductSku() != null) {
						return bundleConstituent.getProductSku().getCode().equals(skuCode);
					}
					return false;
				})
				.findAny();
	}
}
