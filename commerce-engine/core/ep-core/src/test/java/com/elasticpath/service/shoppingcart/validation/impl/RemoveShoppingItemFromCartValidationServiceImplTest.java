/**
 * Copyright (c) Elastic Path Software Inc., 2018
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
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemValidationContext;

@RunWith(MockitoJUnitRunner.class)
public class RemoveShoppingItemFromCartValidationServiceImplTest {

	private static final String PRODUCT_SKU_GUID = "product_sku_guid";

	@InjectMocks
	private final RemoveShoppingItemFromCartValidationServiceImpl removeShoppingItemFromCartValidationService = new
			RemoveShoppingItemFromCartValidationServiceImpl();

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private ShoppingItem shoppingItem;

	@Mock
	private Store store;

	@Mock
	private Shopper shopper;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private ProductSku productSku;

	@Mock
	private ProductSku parentProductSku;

	@Before
	public void setUp() {
		when(beanFactory.getBean(ContextIdNames.SHOPPING_ITEM_VALIDATION_CONTEXT)).thenReturn(new ShoppingItemValidationContextImpl());
		when(shoppingCart.getStore()).thenReturn(store);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shoppingItem.getSkuGuid()).thenReturn(PRODUCT_SKU_GUID);
		when(shoppingCart.getParentProductSku(shoppingItem)).thenReturn(parentProductSku);
		when(productSkuLookup.findByGuid(PRODUCT_SKU_GUID)).thenReturn(productSku);
	}

	@Test
	public void testBuildContext() {
		ShoppingItemValidationContext validationContext =
				removeShoppingItemFromCartValidationService.buildContext(shoppingCart, shoppingItem);

		assertThat(validationContext.getShopper()).isEqualTo(shopper);
		assertThat(validationContext.getStore()).isEqualTo(store);
		assertThat(validationContext.getShoppingCart()).isEqualTo(shoppingCart);
		assertThat(validationContext.getShoppingItem()).isEqualTo(shoppingItem);
		assertThat(validationContext.getProductSku()).isEqualTo(productSku);
		assertThat(validationContext.getParentProductSku()).isEqualTo(parentProductSku);
	}
}