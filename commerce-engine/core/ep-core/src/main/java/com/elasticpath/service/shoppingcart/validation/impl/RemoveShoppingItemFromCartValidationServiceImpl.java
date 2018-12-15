/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.validation.RemoveShoppingItemFromCartValidationService;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemValidationContext;

/**
 * Implementation of {@link RemoveShoppingItemFromCartValidationService}.
 */
public class RemoveShoppingItemFromCartValidationServiceImpl
		extends AbstractAggregateValidator<ShoppingItemValidationContext, ShoppingItemValidationContext>
		implements RemoveShoppingItemFromCartValidationService {

	private BeanFactory beanFactory;

	private ProductSkuLookup productSkuLookup;

	@Override
	public ShoppingItemValidationContext buildContext(final ShoppingCart shoppingCart, final ShoppingItem shoppingItem) {
		final ShoppingItemValidationContext context = beanFactory.getBean(ContextIdNames.SHOPPING_ITEM_VALIDATION_CONTEXT);
		context.setShoppingCart(shoppingCart);
		context.setShoppingItem(shoppingItem);
		context.setParentProductSku(shoppingCart.getParentProductSku(shoppingItem));
		context.setStore(shoppingCart.getStore());
		context.setShopper(shoppingCart.getShopper());
		context.setProductSku(productSkuLookup.findByGuid(shoppingItem.getSkuGuid()));
		return context;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}
}
