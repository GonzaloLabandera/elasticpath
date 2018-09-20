/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.beanframework.MessageSource;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.AvailabilityException;
import com.elasticpath.domain.catalog.InsufficientInventoryException;
import com.elasticpath.domain.catalog.MinOrderQtyException;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.OrderMessageIds;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.inventory.InventoryDto;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.AllocationService;
import com.elasticpath.service.shoppingcart.ShoppingCartEmptyException;

/**
 * Test class for {@link StockCheckerCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StockCheckerCheckoutActionTest {

	private static final String SHOPPING_ITEM_SKU_GUID = "Item_guid";
	private static final String ITEM_CODE = "item-code";
	private static final long WAREHOUSE_UID = 1;
	private static final String STRUCTURED_ERROR_MESSAGES = "structuredErrorMessages";

	@Spy
	@InjectMocks
	private StockCheckerCheckoutAction checkoutAction;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Store store;

	@Mock
	private Warehouse warehouse;

	@Mock
	private CustomerSession customerSession;

	@Mock
	private ShoppingItem shoppingItem;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private ProductSku productSku;

	@Mock
	private AllocationService allocationService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private Product product;

	@Mock
	private ProductInventoryShoppingService productInventoryShoppingService;

	@Mock
	private InventoryDto inventoryDto;

	@Before
	public void setUp() {
		Mockito.<Collection<? extends ShoppingItem>>when(shoppingCart.getLeafShoppingItems()).thenReturn(Collections.singletonList(shoppingItem));

		when(shoppingCart.getStore()).thenReturn(store);
		when(store.getWarehouse()).thenReturn(warehouse);
		when(customerSession.getLocale()).thenReturn(Locale.CANADA);
		when(shoppingItem.getSkuGuid()).thenReturn(SHOPPING_ITEM_SKU_GUID);
		when(productSkuLookup.findByGuid(SHOPPING_ITEM_SKU_GUID)).thenReturn(productSku);
		when(productSku.getSkuCode()).thenReturn(SHOPPING_ITEM_SKU_GUID);

		when(shoppingItem.getQuantity()).thenReturn(1);
		when(warehouse.getUidPk()).thenReturn(WAREHOUSE_UID);

		when(messageSource.getMessage(StockCheckerCheckoutAction.ErrorMessage.INVENTORY.message(), null,
				StockCheckerCheckoutAction.ErrorMessage.INVENTORY.message(), Locale.CANADA)).thenReturn("Inventory error");

		when(productSku.getProduct()).thenReturn(product);
		when(product.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);

		Mockito.<Collection<? extends ShoppingItem>>when(shoppingCart.getRootShoppingItems()).thenReturn(Collections.singletonList(shoppingItem));

		when(shoppingItem.isBundle(productSkuLookup)).thenReturn(false);

		when(productInventoryShoppingService.getInventory(productSku, 1)).thenReturn(inventoryDto);
	}


	/**
	 * Verifies the structured error message received when checking out with an empty shopping cart.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenCheckingOutWithEmptyCart() {

		when(shoppingCart.getNumItems()).thenReturn(0);

		final StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.CART_IS_EMPTY,
				"Shopping cart must not be empty during checkout.", null);

		assertThatThrownBy(() -> checkoutAction.execute(new CheckoutActionContextImpl(shoppingCart, null, null,
				null, false, false, null)))
				.isInstanceOf(ShoppingCartEmptyException.class)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Verifies the structured error message received when there is not sufficient unallocated quantity.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenThereIsNotSufficientUnallocatedQuantity() {

		when(shoppingCart.getNumItems()).thenReturn(1);
		when(allocationService.hasSufficientUnallocatedQty(productSku, WAREHOUSE_UID, 1)).thenReturn(false);

		final StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.INSUFFICIENT_INVENTORY,
				"Insufficient inventory available for SKU: " + SHOPPING_ITEM_SKU_GUID,
				ImmutableMap.of(ITEM_CODE, SHOPPING_ITEM_SKU_GUID));

		assertThatThrownBy(() -> checkoutAction.execute(new CheckoutActionContextImpl(shoppingCart, null,
				customerSession, null, false, false, null)))
				.isInstanceOf(InsufficientInventoryException.class)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Verifies the structured error message received when the item is not available -- either does not have a sku or
	 * is not within the available date range {@link com.elasticpath.service.catalogview.ProductAvailabilityService}.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenItemIsUnavailable() {

		when(shoppingCart.getNumItems()).thenReturn(1);
		when(allocationService.hasSufficientUnallocatedQty(productSku, WAREHOUSE_UID, 1)).thenReturn(true);
		when(productSku.isWithinDateRange()).thenReturn(false);

		final StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.ITEM_NOT_AVAILABLE,
				"Item is not available for purchase: " + SHOPPING_ITEM_SKU_GUID,
				ImmutableMap.of(ITEM_CODE, SHOPPING_ITEM_SKU_GUID));

		assertThatThrownBy(() -> checkoutAction.execute(new CheckoutActionContextImpl(shoppingCart, null,
				customerSession, null, false, false, null)))
				.isInstanceOf(AvailabilityException.class)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Verifies the structured error message received when there is insufficient inventory for the item.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenThereIsInsufficientInventory() {

		when(shoppingCart.getNumItems()).thenReturn(1);
		when(allocationService.hasSufficientUnallocatedQty(productSku, WAREHOUSE_UID, 1)).thenReturn(true);
		when(productSku.isWithinDateRange()).thenReturn(true);

		final StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.INSUFFICIENT_INVENTORY,
				"Insufficient inventory available for SKU: " + SHOPPING_ITEM_SKU_GUID,
				ImmutableMap.of(ITEM_CODE, SHOPPING_ITEM_SKU_GUID));

		assertThatThrownBy(() -> checkoutAction.execute(new CheckoutActionContextImpl(shoppingCart, null,
				customerSession, null, false, false, null)))
				.isInstanceOf(InsufficientInventoryException.class)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Verifies the structured error message received when a cart item quantity is less than the minimum order quantity for that product.
	 */
	@Test
	public void verifyStructuredErrorMessageWhenOrderQuantityIsLessThanMinimumOrderQuantity() {

		when(shoppingCart.getNumItems()).thenReturn(1);
		when(allocationService.hasSufficientUnallocatedQty(productSku, WAREHOUSE_UID, 1)).thenReturn(true);
		when(productSku.isWithinDateRange()).thenReturn(true);
		when(product.getMinOrderQty()).thenReturn(2);

		when(inventoryDto.getAvailableQuantityInStock()).thenReturn(1);

		final StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.MINIMUM_QUANTITY_REQUIRED,
				"SKU: " + SHOPPING_ITEM_SKU_GUID,
				ImmutableMap.of(ITEM_CODE, SHOPPING_ITEM_SKU_GUID, "minimum-quantity", "2"));

		assertThatThrownBy(() -> checkoutAction.execute(new CheckoutActionContextImpl(shoppingCart, null,
				customerSession, null, false, false, null)))
				.isInstanceOf(MinOrderQtyException.class)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(structuredErrorMessage));
	}

	/**
	 * Verifies no structured error messages received when an order meets all requirements for checkout.
	 */
	@Test
	public void verifyNoStructuredErrorMessagesWhenOrderMeetsCheckoutRequirements() {

		when(shoppingCart.getNumItems()).thenReturn(1);
		when(allocationService.hasSufficientUnallocatedQty(productSku, WAREHOUSE_UID, 1)).thenReturn(true);
		when(productSku.isWithinDateRange()).thenReturn(true);
		when(product.getMinOrderQty()).thenReturn(1);

		when(inventoryDto.getAvailableQuantityInStock()).thenReturn(1);

		checkoutAction.execute(new CheckoutActionContextImpl(shoppingCart, null,
				customerSession, null, false, false, null));

		verify(checkoutAction).verifyAvailability(shoppingItem, productSku, Locale.CANADA);
		verify(checkoutAction).verifyCartItemInventory(eq(shoppingItem), eq(productSku), any(TemporaryInventory.class), eq(Locale.CANADA));
		verify(checkoutAction).verifyInventory(shoppingItem, productSku, warehouse, Locale.CANADA);
		verify(checkoutAction).verifyMinOrderQty(shoppingItem);
	}

}