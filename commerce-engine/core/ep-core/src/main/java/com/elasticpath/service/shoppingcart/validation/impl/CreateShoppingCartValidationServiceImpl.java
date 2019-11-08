/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.validation.CreateShoppingCartValidationService;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

/**
 * Implementation of {@link CreateShoppingCartValidationService}.
 */
public class CreateShoppingCartValidationServiceImpl
		extends AbstractAggregateValidator<ShoppingCartValidationContext, ShoppingCartValidationContext>
		implements CreateShoppingCartValidationService {

	private BeanFactory beanFactory;


	@Override
	public ShoppingCartValidationContext buildContext(final ShoppingCart shoppingCart) {
		final ShoppingCartValidationContext context
				= beanFactory.getPrototypeBean(ContextIdNames.SHOPPING_CART_VALIDATION_CONTEXT, ShoppingCartValidationContext.class);
		context.setShoppingCart(shoppingCart);
		return context;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
