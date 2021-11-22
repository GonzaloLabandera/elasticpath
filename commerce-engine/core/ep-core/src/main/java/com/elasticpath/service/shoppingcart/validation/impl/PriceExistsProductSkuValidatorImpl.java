/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;

/**
 * Validator to check that the price exists.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, priority = 1050)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_CHECKOUT, priority = 1040)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART, priority = 1040)
public class PriceExistsProductSkuValidatorImpl extends XPFExtensionPointImpl implements ProductSkuValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "item.missing.price";

	@Autowired
	private BundleIdentifier bundleIdentifier;
	@Autowired
	private ProductSkuLookup productSkuLookup;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFProductSkuValidationContext context) {
		final ProductSku productSku = productSkuLookup.findBySkuCode(context.getProductSku().getCode());
		final ProductSku parentProductSku = context.getParentProductSku() == null
				? null : productSkuLookup.findBySkuCode(context.getParentProductSku().getCode());

		if (isPriceRequired(productSku, parentProductSku) && context.getPromotedPrice() == null) {
			return Collections.singletonList(new XPFStructuredErrorMessage(MESSAGE_ID,
					String.format("Item '%s' does not have a price.", context.getProductSku().getCode()),
					ImmutableMap.of("item-code", context.getProductSku().getCode())));
		}
		return Collections.emptyList();
	}

	private boolean isPriceRequired(final ProductSku productSku, final ProductSku parentProductSku) {
		// If sku being validated is a calculated bundle, no price is required.
		// If parent sku is an assigned bundle, and sku being validated is a bundle, no price is required.
		return !(bundleIdentifier.isCalculatedBundle(productSku)
				|| (parentProductSku != null && bundleIdentifier.isBundle(productSku.getProduct())
				&& bundleIdentifier.isAssignedBundle(parentProductSku)));
	}

	void setBundleIdentifier(final BundleIdentifier bundleIdentifier) {
		this.bundleIdentifier = bundleIdentifier;
	}
}
