/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static com.elasticpath.domain.catalog.AvailabilityCriteria.ALWAYS_AVAILABLE;
import static com.elasticpath.domain.catalog.AvailabilityCriteria.AVAILABLE_FOR_BACK_ORDER;
import static com.elasticpath.domain.catalog.AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER;
import static com.elasticpath.domain.catalog.AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK;
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
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.UnusedPrivateField")
public class InventoryShoppingItemValidatorImplTest {

	private static final String SKU_CODE = "sku_code";

	private static final String SKU_GUID = "sku_guid";

	private static final String STORE_CODE = "store_code";

	private static final long REQUESTED_QUANTITY = 10L;

	private static final int ENOUGH_AVAILABLE = 15;

	private static final int NOT_ENOUGH_AVAILABLE = 5;

	private static final String MESSAGE_ID = "item.insufficient.inventory";

	@InjectMocks
	private InventoryShoppingItemValidatorImpl validator;

	@Mock
	private ProductInventoryShoppingService productInventoryShoppingService;

	@Mock
	private Store store;

	@Mock
	private Product product;

	@Mock
	private ProductSku productSku;

	@Mock
	private XPFStore xpfStore;

	@Mock
	private XPFProductSku xpfProductSku;

	@Mock
	private XPFShoppingCart shoppingCart;

	@Mock
	private XPFShopper xpfShopper;

	@Mock
	private XPFShoppingItem xpfShoppingItem;

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private SkuInventoryDetails inventoryDetails;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private StoreService storeService;

	@Spy
	private SuperInventoryValidator superInventoryValidator;

	@Mock
	private BeanFactory beanFactory;

	@Before
	public void setUp() throws Exception {
		given(productSkuLookup.findBySkuCode(SKU_CODE)).willReturn(productSku);

		given(context.getShoppingItem()).willReturn(xpfShoppingItem);
		given(xpfShoppingItem.getProductSku()).willReturn(xpfProductSku);
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(context.getShoppingCart().getShopper()).willReturn(xpfShopper);
		given(xpfShopper.getStore()).willReturn(xpfStore);
		given(xpfStore.getCode()).willReturn(STORE_CODE);

		given(storeService.findStoreWithCode(STORE_CODE)).willReturn(store);

		given(xpfShoppingItem.getQuantity()).willReturn(REQUESTED_QUANTITY);
		given(xpfProductSku.getCode()).willReturn(SKU_CODE);

		given(productSku.isShippable()).willReturn(true);
		given(productSku.getSkuCode()).willReturn(SKU_GUID);
		given(productInventoryShoppingService.getSkuInventoryDetails(productSku, store)).willReturn(inventoryDetails);

		given(productSku.getProduct()).willReturn(product);

		given(product.getAvailabilityCriteria()).willReturn(AVAILABLE_WHEN_IN_STOCK);
		given(beanFactory.getSingletonBean(ContextIdNames.PRODUCT_INVENTORY_SHOPPING_SERVICE, ProductInventoryShoppingService.class))
				.willReturn(productInventoryShoppingService);
		given(beanFactory.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class))
				.willReturn(storeService);
	}

	@Test
	public void testHasEnoughInventory() {
		// Given
		given(inventoryDetails.getAvailableQuantityInStock()).willReturn(ENOUGH_AVAILABLE);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testNotEnoughInventory() {
		XPFStructuredErrorMessage errorMessage =
				new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR, MESSAGE_ID,
						String.format("Item '%s' only has %d available but %d were requested.",
								productSku.getSkuCode(), NOT_ENOUGH_AVAILABLE, REQUESTED_QUANTITY),
						ImmutableMap.of("item-code", productSku.getSkuCode(),
								"quantity-requested", String.format("%d", REQUESTED_QUANTITY),
								"inventory-available", String.format("%d", NOT_ENOUGH_AVAILABLE)));

		// Given
		given(inventoryDetails.getAvailableQuantityInStock()).willReturn(NOT_ENOUGH_AVAILABLE);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void testAlwaysAvailable() {

		// Given
		given(product.getAvailabilityCriteria()).willReturn(ALWAYS_AVAILABLE);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testAvailableForPreOrder() {

		// Given
		given(product.getAvailabilityCriteria()).willReturn(AVAILABLE_FOR_PRE_ORDER);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testAvailableForBackOrder() {

		// Given
		given(product.getAvailabilityCriteria()).willReturn(AVAILABLE_FOR_BACK_ORDER);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

}