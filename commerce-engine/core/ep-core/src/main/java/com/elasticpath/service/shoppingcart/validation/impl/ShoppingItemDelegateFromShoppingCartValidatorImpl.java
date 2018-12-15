/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidator;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemValidator;

/**
 * Delegates shopping item validators from shopping cart validator.
 */
public class ShoppingItemDelegateFromShoppingCartValidatorImpl implements ShoppingCartValidator {

	private Collection<ShoppingItemValidator> shoppingItemValidators;
	private ProductSkuLookup productSkuLookup;
	private BeanFactory beanFactory;
	private PriceLookupFacade priceLookupFacade;

	@Override
	public Collection<StructuredErrorMessage> validate(final ShoppingCartValidationContext shoppingCartValidationContext) {
		final ShoppingCart shoppingCart = shoppingCartValidationContext.getShoppingCart();
		Map<ShoppingItem, ProductSku> shoppingItemProductSkuMap = shoppingCart.getShoppingItemProductSkuMap();

		return shoppingItemProductSkuMap.entrySet().stream()
				.flatMap(entry -> {
					ShoppingItem shoppingItem = entry.getKey();
					ProductSku productSku = entry.getValue();
					ProductSku parentProductSku = shoppingCart.getParentProductSku(shoppingItem);

					ShoppingItemValidationContext context = beanFactory.getBean(ContextIdNames.SHOPPING_ITEM_VALIDATION_CONTEXT);
					context.setProductSku(productSku);
					context.setParentProductSku(parentProductSku);
					context.setShopper(shoppingCart.getShopper());
					context.setStore(shoppingCart.getStore());
					context.setShoppingCart(shoppingCart);
					context.setShoppingItem(shoppingItem);
					context.setPromotedPrice(priceLookupFacade.getPromotedPriceForSku(productSku, context.getStore(), context.getShopper()));
					return shoppingItemValidators.stream().flatMap(validator -> validator.validate(context).stream());
				})
				.collect(Collectors.toSet());
	}


	protected Collection<ShoppingItemValidator> getShoppingItemValidators() {
		return shoppingItemValidators;
	}

	public void setShoppingItemValidators(final Collection<ShoppingItemValidator> shoppingItemValidators) {
		this.shoppingItemValidators = shoppingItemValidators;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected PriceLookupFacade getPriceLookupFacade() {
		return priceLookupFacade;
	}

	public void setPriceLookupFacade(final PriceLookupFacade priceLookupFacade) {
		this.priceLookupFacade = priceLookupFacade;
	}
}
