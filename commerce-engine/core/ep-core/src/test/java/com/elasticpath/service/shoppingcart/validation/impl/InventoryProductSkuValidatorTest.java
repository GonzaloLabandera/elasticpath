/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.UnusedPrivateField")
public class InventoryProductSkuValidatorTest {

	private static final String SKU_CODE = "sku_code";

	@InjectMocks
	private InventoryProductSkuValidatorImpl validator;

	@Mock
	private ProductInventoryShoppingService productInventoryShoppingService;

	@Mock
	private XPFProductSkuValidationContext context;

	@Mock
	private Store store;

	@Mock
	private SkuInventoryDetails skuInventoryDetails;

	@Mock
	private Product product;

	@Mock
	private ProductSku productSku;

	@Mock
	private XPFProductSku productSkuContext;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private StoreService storeService;

	@Mock
	private XPFShopper shopper;

	@Mock
	private XPFStore contextStore;

	@Mock
	private BeanFactory beanFactory;

	@Spy
	private SuperInventoryValidator superInventoryValidator;

	@Before
	public void setUp() {
		given(context.getShopper()).willReturn(shopper);
		given(shopper.getStore()).willReturn(contextStore);
		given(contextStore.getCode()).willReturn("storeCode");
		given(context.getProductSku()).willReturn(productSkuContext);
		given(productSkuContext.getCode()).willReturn(SKU_CODE);
		given(storeService.findStoreWithCode("storeCode")).willReturn(store);
		given(productSkuLookup.findBySkuCode(SKU_CODE)).willReturn(productSku);
		given(productSku.getSkuCode()).willReturn(SKU_CODE);
		given(productSku.isShippable()).willReturn(true);
		given(productSku.getProduct()).willReturn(product);
		given(product.getAvailabilityCriteria()).willReturn(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		given(productInventoryShoppingService.getSkuInventoryDetails(productSku, store)).willReturn(skuInventoryDetails);
		given(beanFactory.getSingletonBean(ContextIdNames.PRODUCT_INVENTORY_SHOPPING_SERVICE, ProductInventoryShoppingService.class))
				.willReturn(productInventoryShoppingService);
		given(beanFactory.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class))
				.willReturn(storeService);
	}

	@Test
	public void testProductNotAvailable() {
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.insufficient.inventory",
				String.format("Item '%s' does not have sufficient inventory.", SKU_CODE),
				ImmutableMap.of("item-code", SKU_CODE));

		// Given
		given(skuInventoryDetails.hasSufficientUnallocatedQty()).willReturn(false);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testProductAvailable() {
		// Given
		given(skuInventoryDetails.hasSufficientUnallocatedQty()).willReturn(true);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}
}
