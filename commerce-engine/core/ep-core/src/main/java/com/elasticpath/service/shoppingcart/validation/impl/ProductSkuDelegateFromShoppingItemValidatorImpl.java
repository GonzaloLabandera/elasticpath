/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.context.builders.ProductSkuValidationContextBuilder;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Delegates product sku validators from shopping item validators.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, priority = 1010)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT, priority = 1010)
public class ProductSkuDelegateFromShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {

	@Autowired
	private XPFExtensionLookup extensionLookup;

	@Autowired
	private BeanFactory beanFactory;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		Collection<XPFStructuredErrorMessage> errorMessages;
		if (context.getOperation() == XPFOperationEnum.NOOP) {
			errorMessages = validateInternal(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_CHECKOUT, context).stream()
					.map(structuredErrorMessage -> new XPFStructuredErrorMessage(structuredErrorMessage.getType(),
							structuredErrorMessage.getMessageId(),
							structuredErrorMessage.getDebugMessage(), structuredErrorMessage.getData(),
							new XPFStructuredErrorResolution(ShoppingItem.class, context.getShoppingItem().getProductSku().getCode())))
					.collect(Collectors.toList());
		} else {
			errorMessages = validateInternal(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART,
					context);
		}

		return errorMessages;
	}

	private Collection<XPFStructuredErrorMessage> validateInternal(final XPFExtensionPointEnum extensionPointEnum,
																   final XPFShoppingItemValidationContext context) {

		ProductSkuValidationContextBuilder productSkuValidationContextBuilder = beanFactory.getSingletonBean(
				ContextIdNames.PRODUCT_SKU_VALIDATION_CONTEXT_BUILDER, ProductSkuValidationContextBuilder.class);

		XPFProductSkuValidationContext productSkuValidationContext = productSkuValidationContextBuilder.build(context);

		XPFExtensionSelectorByStoreCode selector = new XPFExtensionSelectorByStoreCode(context.getShoppingCart().getShopper().getStore().getCode());

		return extensionLookup.getMultipleExtensions(ProductSkuValidator.class, extensionPointEnum, selector).stream()
				.map(strategy -> strategy.validate(productSkuValidationContext))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}
