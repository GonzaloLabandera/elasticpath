/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

@RunWith(MockitoJUnitRunner.class)
public class CreateShoppingCartValidationServiceImplTest {

	@InjectMocks
	private final CreateShoppingCartValidationServiceImpl service = new
			CreateShoppingCartValidationServiceImpl();

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private ShoppingCart shoppingCart;


	@Before
	public void setUp() {
		when(beanFactory.getPrototypeBean(ContextIdNames.SHOPPING_CART_VALIDATION_CONTEXT, ShoppingCartValidationContext.class))
				.thenReturn(new ShoppingCartValidationContextImpl());
	}

	@Test
	public void testBuildContext() {
		ShoppingCartValidationContext validationContext =
				service.buildContext(shoppingCart);

		assertThat(validationContext.getShoppingCart()).isEqualTo(shoppingCart);
	}
}