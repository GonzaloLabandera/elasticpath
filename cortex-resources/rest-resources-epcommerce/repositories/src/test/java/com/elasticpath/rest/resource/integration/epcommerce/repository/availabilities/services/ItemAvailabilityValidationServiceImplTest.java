/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.services;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.ORDER_ID;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.availabilities.AvailabilityForCartLineItemIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Test for {@link ItemAvailabilityValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemAvailabilityValidationServiceImplTest {

	private static final String ITEM_GUID_1 = "itemId1";
	private static final String ITEM_GUID_2 = "itemId2";
	private static final String ITEM_SKU_GUID_1 = "skuGuid1";
	private static final String ITEM_SKU_GUID_2 = "skuGuid2";
	private static final String ITEM_SKU_CODE_1 = "skuCode1";
	private static final String ITEM_SKU_CODE_2 = "skuCode2";

	@Mock
	private ShoppingItem shoppingItem1;

	@Mock
	private ShoppingItem shoppingItem2;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private StoreProduct storeProduct1;

	@Mock
	private StoreProduct storeProduct2;

	@Mock
	private ProductSku productSku1;

	@Mock
	private ProductSku productSku2;

	@Mock
	private List<ShoppingItem> shoppingItems;

	@Mock
	private StoreProductRepository storeProductRepository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@InjectMocks
	private ItemAvailabilityValidationServiceImpl validationService;

	@Test
	public void emptyItemsListShouldHaveNoMessages() {
		when(shoppingItems.iterator()).thenReturn(Iterators.forArray());
		validationService.getUnavailableItemMessages(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID, shoppingItems)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void listWithNoUnavailableItemsShouldHaveNoMessages() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any())).thenReturn(true);
		when(storeProduct2.isSkuAvailable(any())).thenReturn(true);

		when(shoppingItems.iterator()).thenReturn(Iterators.forArray(shoppingItem1, shoppingItem2));
		validationService.getUnavailableItemMessages(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID, shoppingItems)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void listWithOneUnavailableItemShouldHaveOneMessage() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any())).thenReturn(true);
		when(storeProduct2.isSkuAvailable(any())).thenReturn(false);

		when(shoppingItems.iterator()).thenReturn(Iterators.forArray(shoppingItem1, shoppingItem2));
		validationService.getUnavailableItemMessages(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID,
				shoppingItems)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_2, ITEM_GUID_2));
	}

	@Test
	public void listWithTwoUnavailableItemsShouldHaveTwoMessages() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any())).thenReturn(false);
		when(storeProduct2.isSkuAvailable(any())).thenReturn(false);

		when(shoppingItems.iterator()).thenReturn(Iterators.forArray(shoppingItem1, shoppingItem2));
		validationService.getUnavailableItemMessages(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID, shoppingItems)
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_1, ITEM_GUID_1))
				.assertValueAt(1, linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_2, ITEM_GUID_2));
	}

	@Test
	public void linkedMessageShouldHaveNotAvailableMessageGivenNotAvailableItem() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any())).thenReturn(false);

		validationService.getLinkedMessage(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID, shoppingItem1, storeProduct1)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_1, ITEM_GUID_1));
	}

	@Test
	public void linkedMessageShouldHaveNoMessageGivenAvailableItem() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any())).thenReturn(true);

		validationService.getLinkedMessage(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID, shoppingItem1, storeProduct1)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void validateItemUnavailabeReturnsNoValuesWhenItemsInTheOrderAreAvailable() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any(String.class))).thenReturn(true);
		when(storeProduct2.isSkuAvailable(any(String.class))).thenReturn(true);

		validationService.validateItemUnavailable(IdentifierTestFactory.buildOrderIdentifier(ResourceTestConstants.SCOPE, ORDER_ID))
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void validateItemUnavailabeReturnsLinkedMessageWhenAnItemInTheOrderIsUnavailable() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any(String.class))).thenReturn(true);
		when(storeProduct2.isSkuAvailable(any(String.class))).thenReturn(false);

		validationService.validateItemUnavailable(IdentifierTestFactory.buildOrderIdentifier(ResourceTestConstants.SCOPE, ORDER_ID))
				.test()
				.assertValueCount(1)
				.assertValue(linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_2, ITEM_GUID_2));
	}

	@Test
	public void validateItemUnavailabeReturnsTwoLinkedMessagesWhenTwoItemsInTheOrderAreUnavailable() {
		setUpMocksForAvailabilityTests();
		when(storeProduct1.isSkuAvailable(any(String.class))).thenReturn(false);
		when(storeProduct2.isSkuAvailable(any(String.class))).thenReturn(false);

		validationService.validateItemUnavailable(IdentifierTestFactory.buildOrderIdentifier(ResourceTestConstants.SCOPE, ORDER_ID))
				.test()
				.assertValueCount(2)
				.assertValueAt(0, linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_1, ITEM_GUID_1))
				.assertValueAt(1, linkedMessage -> validateLinkedMessage(linkedMessage, ITEM_SKU_CODE_2, ITEM_GUID_2));
	}

	private boolean linkedMessageIdentifierHasCorrectProperties(final LinkedMessage<AvailabilityForCartLineItemIdentifier> linkedMessage,
																final String itemGuid) {
		AvailabilityForCartLineItemIdentifier availabilityIdentifier = linkedMessage.getLinkedIdentifier().get();

		LineItemIdentifier lineItemIdentifier = availabilityIdentifier.getLineItem();
		CartIdentifier cartIdentifier = lineItemIdentifier.getLineItems().getCart();

		boolean isCorrectLineItemId = lineItemIdentifier.getLineItemId().getValue().equals(itemGuid);
		boolean isCorrectCartId = cartIdentifier.getCartId().getValue().equals(ResourceTestConstants.CART_ID);
		boolean isCorrectScope = cartIdentifier.getScope().getValue().equals(ResourceTestConstants.SCOPE);

		return isCorrectLineItemId && isCorrectCartId && isCorrectScope;
	}

	private void setUpMocksForAvailabilityTests() {
		when(shoppingItem1.getGuid()).thenReturn(ITEM_GUID_1);
		when(shoppingItem2.getGuid()).thenReturn(ITEM_GUID_2);
		when(shoppingItem1.getSkuGuid()).thenReturn(ITEM_SKU_GUID_1);
		when(shoppingItem2.getSkuGuid()).thenReturn(ITEM_SKU_GUID_2);

		when(storeProductRepository.findDisplayableStoreProductWithAttributesBySkuGuid(ResourceTestConstants.SCOPE, ITEM_SKU_GUID_1))
				.thenReturn(Single.just(storeProduct1));
		when(storeProductRepository.findDisplayableStoreProductWithAttributesBySkuGuid(ResourceTestConstants.SCOPE, ITEM_SKU_GUID_2))
				.thenReturn(Single.just(storeProduct2));
		when(storeProduct1.getSkuByGuid(any())).thenReturn(productSku1);
		when(storeProduct2.getSkuByGuid(any())).thenReturn(productSku2);
		when(productSku1.getSkuCode()).thenReturn(ITEM_SKU_CODE_1);
		when(productSku2.getSkuCode()).thenReturn(ITEM_SKU_CODE_2);

		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getGuid()).thenReturn(ResourceTestConstants.CART_ID);
		when(shoppingCart.getCartItems()).thenReturn(Arrays.asList(shoppingItem1, shoppingItem2));
	}

	private boolean validateLinkedMessage(final LinkedMessage<AvailabilityForCartLineItemIdentifier> linkedMessage, final String skuCode,
										  final String itemGuid) {
		return linkedMessage.getId().equals(StructuredErrorMessageIdConstants.CART_ITEM_NOT_AVAILABLE)
				&& linkedMessage.getType().equals(StructuredMessageTypes.ERROR)
				&& linkedMessage.getData().equals(ImmutableMap.of("item-code", skuCode))
				&& linkedMessage.getDebugMessage().equals("Item '" + skuCode + "' is not available for purchase.")
				&& linkedMessageIdentifierHasCorrectProperties(linkedMessage, itemGuid);
	}

}