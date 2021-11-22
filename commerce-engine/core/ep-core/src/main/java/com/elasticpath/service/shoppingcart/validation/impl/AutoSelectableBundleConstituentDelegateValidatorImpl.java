/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Delegates the validation of auto-selectable bundle constituents.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, priority = 1070)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART, priority = 1060)
public class AutoSelectableBundleConstituentDelegateValidatorImpl extends XPFExtensionPointImpl implements ProductSkuValidator {

	@Autowired
	private XPFExtensionLookup extensionLookup;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFProductSkuValidationContext context) {
		return context.getChildren()
				.stream()
				.map(this::validateInternal)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private Collection<XPFStructuredErrorMessage> validateInternal(final XPFProductSkuValidationContext context) {
		XPFExtensionSelectorByStoreCode selector = new XPFExtensionSelectorByStoreCode(context.getShopper().getStore().getCode());
		List<ProductSkuValidator> extensions = extensionLookup.getMultipleExtensions(ProductSkuValidator.class,
				XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, selector);

		return extensions.stream()
				.map(strategy -> strategy.validate(context))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}
