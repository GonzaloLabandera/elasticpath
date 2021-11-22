/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.validation.PurchaseCartValidationService;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.context.builders.ShoppingCartValidationContextBuilder;
import com.elasticpath.xpf.context.builders.ShoppingItemValidationContextBuilder;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Service for validating a shopping cart before a purchase.
 */
public class PurchaseCartValidationServiceImpl implements PurchaseCartValidationService {
	private XPFExtensionLookup extensionLookup;
	private ShoppingCartValidationContextBuilder shoppingCartValidationContextBuilder;
	private ShoppingItemValidationContextBuilder shoppingItemValidationContextBuilder;
	private StructuredErrorMessageConverter structuredErrorMessageConverter;

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingCart shoppingCart, final Shopper shopper, final Store store) {
		XPFShoppingCartValidationContext shoppingCartValidationContext = shoppingCartValidationContextBuilder.build(shoppingCart);
		XPFExtensionSelector xpfExtensionSelector = new XPFExtensionSelectorByStoreCode(store.getCode());

		Collection<StructuredErrorMessage> errorMessages = new ArrayList<>();
		errorMessages.addAll(executeShoppingCartValidators(shoppingCartValidationContext, xpfExtensionSelector));
		errorMessages.addAll(executeShoppingCartItemValidators(shoppingCartValidationContext, shoppingCart, xpfExtensionSelector, shopper, store));
		return errorMessages;
	}

	private Collection<StructuredErrorMessage> executeShoppingCartValidators(final XPFShoppingCartValidationContext shoppingCartValidationContext,
																			 final XPFExtensionSelector xpfExtensionSelector) {
		return extensionLookup.getMultipleExtensions(ShoppingCartValidator.class,
				XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT,
				xpfExtensionSelector).stream()
				.map(strategy -> strategy.validate(shoppingCartValidationContext))
				.flatMap(Collection::stream)
				.map(xpfStructuredErrorMessage -> structuredErrorMessageConverter.convert(xpfStructuredErrorMessage))
				.collect(Collectors.toList());
	}

	private Collection<StructuredErrorMessage> executeShoppingCartItemValidators(final XPFShoppingCartValidationContext shoppingCartValidationContext,
																				 final ShoppingCart shoppingCart,
																				 final XPFExtensionSelector xpfExtensionSelector,
																				 final Shopper shopper,
																				 final Store store) {
		List<XPFShoppingItemValidationContext> contexts = new ArrayList<>();
		for (ShoppingItem shoppingItem : shoppingCart.getRootShoppingItems()) {
			ShoppingItem parentShoppingItem = null;
			if (shoppingItem.getParentItemUid() != null) {
				parentShoppingItem = shoppingCart.getCartItemById(shoppingItem.getParentItemUid());
			}
			XPFShoppingItemValidationContext shoppingItemValidationContext = shoppingItemValidationContextBuilder.build(
					shoppingCartValidationContext.getShoppingCart(),
					shoppingItem,
					parentShoppingItem,
					XPFOperationEnum.NOOP,
					shopper,
					store);
			contexts.addAll(shoppingItemValidationContextBuilder.getAllContextsStream(shoppingItemValidationContext).collect(Collectors.toList()));
		}

		List<ShoppingItemValidator> shoppingItemValidators = extensionLookup.getMultipleExtensions(ShoppingItemValidator.class,
				XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT,
				xpfExtensionSelector);
		return contexts.stream()
				.flatMap(context -> shoppingItemValidators.stream()
						.map(strategy -> strategy.validate(context))
						.flatMap(Collection::stream))
				.map(xpfStructuredErrorMessage -> structuredErrorMessageConverter.convert(xpfStructuredErrorMessage))
				.collect(Collectors.toList());
	}

	protected XPFExtensionLookup getExtensionLookup() {
		return extensionLookup;
	}

	public void setExtensionLookup(final XPFExtensionLookup extensionLookup) {
		this.extensionLookup = extensionLookup;
	}

	protected ShoppingCartValidationContextBuilder getShoppingCartValidationContextBuilder() {
		return shoppingCartValidationContextBuilder;
	}

	public void setShoppingCartValidationContextBuilder(final ShoppingCartValidationContextBuilder shoppingCartValidationContextBuilder) {
		this.shoppingCartValidationContextBuilder = shoppingCartValidationContextBuilder;
	}

	protected ShoppingItemValidationContextBuilder getShoppingItemValidationContextBuilder() {
		return shoppingItemValidationContextBuilder;
	}

	public void setShoppingItemValidationContextBuilder(final ShoppingItemValidationContextBuilder shoppingItemValidationContextBuilder) {
		this.shoppingItemValidationContextBuilder = shoppingItemValidationContextBuilder;
	}

	protected StructuredErrorMessageConverter getStructuredErrorMessageConverter() {
		return structuredErrorMessageConverter;
	}

	public void setStructuredErrorMessageConverter(final StructuredErrorMessageConverter structuredErrorMessageConverter) {
		this.structuredErrorMessageConverter = structuredErrorMessageConverter;
	}
}
