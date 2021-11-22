/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.sellingchannel.director.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.impl.CartItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.ProductUnavailableException;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.ShoppingItemService;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.context.builders.ShoppingItemValidationContextBuilder;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;

@RunWith(MockitoJUnitRunner.class)
public class CartDirectorServiceImplTest {

	private static final String ITEM_GUID_1 = "itemGuid1";
	private static final String ITEM_GUID_2 = "itemGuid2";
	private static final String SKU_CODE = "skuCode";
	private static final String SKU_CODE_2 = "skuCode2";
	private static final String WISHLIST_LINE_ITEM_GUID = "wishlistLineItemGuid";
	private static final String CART_LINE_ITEM_GUID = "cartLineItemGuid";

	@Mock private CartDirector cartDirector;
	@Mock private ShoppingCartService shoppingCartService;
	@Mock private WishListService wishListService;
	@Mock private ShoppingItemService shoppingItemService;
	@Mock private ShoppingCart shoppingCart;
	@Mock private ShoppingItem shoppingItem;
	@Mock private Shopper shopper;
	@Mock private Store store;
	@Mock private WishList wishList;
	@Mock private PricingSnapshotService pricingSnapshotService;
	@Mock private XPFExtensionLookup extensionLookup;
	@Mock private ShoppingItemValidationContextBuilder shoppingItemValidationContextBuilder;
	@Mock private StructuredErrorMessageConverter structuredErrorMessageConverter;
	@Mock private XPFShoppingItemValidationContext shoppingItemValidationContext;

	@InjectMocks
	private CartDirectorServiceImpl service;

	private void setupSaveShoppingCartAction() {
		when(shoppingCartService.saveOrUpdate(shoppingCart)).thenReturn(shoppingCart);
	}

	private ShoppingItem givenCartDirectorPersistsShoppingItem(final ShoppingItemDto dto) {
		final ShoppingItem addedShoppingItem = mock(ShoppingItem.class);
		final ShoppingItem persistedShoppingItem = mock(ShoppingItem.class);

		final String shoppingItemGuid = UUID.randomUUID().toString();
		when(addedShoppingItem.getGuid()).thenAnswer(invocation -> shoppingItemGuid);

		when(cartDirector.addItemToCart(shoppingCart, dto)).thenReturn(addedShoppingItem);
		when(shoppingCart.getCartItemByGuid(shoppingItemGuid))
				.thenReturn(persistedShoppingItem);

		return persistedShoppingItem;
	}

	@Test
	public void testAddItemToCartHappyPath() {
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 1);

		setupSaveShoppingCartAction();
		final ShoppingItem expectedShoppingItem = givenCartDirectorPersistsShoppingItem(dto);

		final ShoppingItem actualShoppingItem = service.addItemToCart(shoppingCart, dto);

		assertThat(actualShoppingItem)
				.isSameAs(expectedShoppingItem);

		verify(cartDirector).addItemToCart(shoppingCart, dto);
		expectThatShoppingCartWillBePersisted();
	}

	@Test
	public void testAddItemsToCartHappyPath() {
		final ShoppingItemDto dto1 = new ShoppingItemDto(SKU_CODE, 1);
		final ShoppingItemDto dto2 = new ShoppingItemDto(SKU_CODE_2, 1);

		List<ShoppingItemDto> dtos = new ArrayList<>();
		dtos.add(dto1);
		dtos.add(dto2);

		final ShoppingItem addedShoppingItem1 = mock(ShoppingItem.class);
		final ShoppingItem addedShoppingItem2 = mock(ShoppingItem.class);

		when(cartDirector.addItemToCart(shoppingCart, dto1, null)).thenReturn(addedShoppingItem1);
		when(cartDirector.addItemToCart(shoppingCart, dto2, null)).thenReturn(addedShoppingItem2);

		setupSaveShoppingCartAction();

		service.addItemsToCart(shoppingCart, dtos);

		verify(cartDirector).addItemToCart(shoppingCart, dto1, null);
		verify(cartDirector).addItemToCart(shoppingCart, dto2, null);
		expectThatShoppingCartWillBePersisted();
	}

	@Test(expected = ProductUnavailableException.class)
	public void testAddItemsToCartOneItemCannotBeAdded() {
		final ShoppingItemDto dto1 = new ShoppingItemDto(SKU_CODE, 1);
		final ShoppingItemDto dto2 = new ShoppingItemDto(SKU_CODE_2, 1);

		List<ShoppingItemDto> dtos = new ArrayList<>();
		dtos.add(dto1);
		dtos.add(dto2);

		final ShoppingItem addedShoppingItem1 = mock(ShoppingItem.class);

		when(cartDirector.addItemToCart(shoppingCart, dto1, null)).thenReturn(addedShoppingItem1);
		when(cartDirector.addItemToCart(shoppingCart, dto2, null)).thenReturn(null);

		setupSaveShoppingCartAction();

		service.addItemsToCart(shoppingCart, dtos);
	}

	@Test
	public void testUpdateCartItemHappyPath() {
		final long itemId = 12345L;
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 99);

		setupSaveShoppingCartAction();

		service.updateCartItem(shoppingCart, itemId, dto);

		verify(cartDirector).updateCartItem(shoppingCart, itemId, dto);
		expectThatShoppingCartWillBePersisted();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testUpdateCompoundCartItemHappyPath() {
		final long rootItemId = 123L;

		final ShoppingItemDto rootDto = new ShoppingItemDto(SKU_CODE, 1);
		final ShoppingItemDto dependentDto = new ShoppingItemDto(SKU_CODE + "dependent", 1);
		final ShoppingItemDto associatedDto = new ShoppingItemDto(SKU_CODE + "associated", 1);

		final CartItem updatedCartItem = mock(CartItem.class, "updatedRootItem");
		final CartItem previousDependentItem = mock(CartItem.class, "previousDependentItem");
		final List<ShoppingItem> childItems = spy(Lists.newArrayList(previousDependentItem));

		setupThatRootWillBeUpdatedAndDependentAndAssociatedItemsWillBeAdded(
				rootDto, rootItemId, updatedCartItem, dependentDto, associatedDto);

		final String dependentItemGuid = UUID.randomUUID().toString();
		when(previousDependentItem.getGuid()).thenReturn(dependentItemGuid);
		when(updatedCartItem.getChildren()).thenReturn(childItems);

		// When
		service.updateCompoundCartItem(
				shoppingCart, rootItemId, rootDto,
				Collections.singletonList(dependentDto), Collections.singletonList(associatedDto));


		//  Note that previous <em>Associated</em> items are not deleted.
		//  This is pre-existing behaviour, I have no idea whether or not it is a bug or a feature or neither.
		verify(childItems).removeAll(Collections.singletonList(previousDependentItem));
		verify(shoppingCart).removeCartItems(Collections.singleton(dependentItemGuid));

		expectThatShoppingCartWillBePersisted();
	}

	private void setupThatRootWillBeUpdatedAndDependentAndAssociatedItemsWillBeAdded(
			final ShoppingItemDto rootDto, final long rootItemId, final CartItem updatedRootItem,
			final ShoppingItemDto dependentDto, final ShoppingItemDto associatedDto) {
		final ShoppingItem newDependentItem = mock(ShoppingItem.class, "newDependentItem");
		final ShoppingItem newAssociatedItem = mock(ShoppingItem.class, "newAssociatedItem");

		when(cartDirector.updateCartItem(shoppingCart, rootItemId, rootDto)).thenReturn(updatedRootItem);
		when(cartDirector.addItemToCart(shoppingCart, dependentDto, updatedRootItem)).thenReturn(newDependentItem);
		when(cartDirector.addItemToCart(shoppingCart, associatedDto, null)).thenReturn(newAssociatedItem);
	}

	@Test
	public void testRemoveItemFromCartHappyPath() {
		when(shoppingCart.getCartItemByGuid(ITEM_GUID_1)).thenReturn(shoppingItem);
		when(shoppingCart.getCartItemByGuid(ITEM_GUID_2)).thenReturn(shoppingItem);
		when(shoppingCart.getStore()).thenReturn(store);
		when(shoppingItem.getParentItemUid()).thenReturn(1L);

		// When
		service.removeItemsFromCart(shoppingCart, ITEM_GUID_1, ITEM_GUID_2);

		verify(shoppingItemService).deleteItemsByGuids(ITEM_GUID_1, ITEM_GUID_2);
		verify(shoppingItemValidationContextBuilder, times(2)).build(any(ShoppingCart.class), any(ShoppingItem.class), eq(null),
				eq(XPFOperationEnum.DELETE));
	}

	@Test
	public void testRemoveItemFromCartValidationFails() {
		when(shoppingCart.getCartItemByGuid(any(String.class))).thenReturn(mock(ShoppingItem.class));
		when(shoppingCart.getStore()).thenReturn(store);
		when(shoppingItemValidationContextBuilder.build(any(ShoppingCart.class), any(ShoppingItem.class), eq(null), eq(XPFOperationEnum.DELETE)))
				.thenReturn(shoppingItemValidationContext);
		when(extensionLookup.getMultipleExtensions(eq(ShoppingItemValidator.class), any(), any()))
				.thenReturn(Collections.singletonList(validationContext ->
						ImmutableList.of(new XPFStructuredErrorMessage("error", "message", Collections.emptyMap()))));
		when(structuredErrorMessageConverter.convert(any())).thenReturn(new StructuredErrorMessage("error", "message", Collections.emptyMap()));

		assertThatThrownBy(()->service.removeItemsFromCart(shoppingCart, ""))
				.isInstanceOf(EpValidationException.class)
				.hasMessage("Remove item from cart validation failure: [error,error,message,{}]");
	}

	@Test
	public void testAddSkuToWishListHappyPath() {
		final ShoppingItem shoppingItem = mock(ShoppingItem.class);
		final ShoppingItem expectedShoppingItem = mock(ShoppingItem.class);
		final String shoppingItemGuid = UUID.randomUUID().toString();
		final AddToWishlistResult addToWishListResult = new AddToWishlistResult(expectedShoppingItem, true);

		when(expectedShoppingItem.getGuid()).thenReturn(shoppingItemGuid);
		when(expectedShoppingItem.getGuid()).thenReturn(shoppingItemGuid);
		when(cartDirector.createShoppingItem(SKU_CODE, store, 1)).thenReturn(shoppingItem);
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);
		when(wishListService.addItem(wishList, shoppingItem)).thenReturn(addToWishListResult);
		when(wishListService.save(wishList)).thenReturn(wishList);
		when(wishList.getAllItems()).thenReturn(Collections.singletonList(expectedShoppingItem));

		final ShoppingItem actualShoppingItem = service.addSkuToWishList(SKU_CODE, shopper, store);

		assertThat(actualShoppingItem)
				.isSameAs(expectedShoppingItem);

		verify(wishListService).addItem(wishList, shoppingItem);
		expectThatWishListWillBePersisted();
	}

	/**
	 * test moving item from wish list to shopping cart.
	 */
	@Test
	public void testMoveItemFromWishListToCart() {
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 1);
		final ShoppingCart persistedShoppingCart = mock(ShoppingCart.class);
		final ShoppingItem expectedShoppingItem = mock(ShoppingItem.class);
		final ShoppingItem persistedShoppingItem = new ShoppingItemImpl();
		persistedShoppingItem.setGuid(WISHLIST_LINE_ITEM_GUID);

		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(cartDirector.addItemToCart(shoppingCart, dto)).thenReturn(persistedShoppingItem);
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);
		when(shoppingCartService.saveOrUpdate(shoppingCart)).thenReturn(persistedShoppingCart);
		when(persistedShoppingCart.getCartItemByGuid(WISHLIST_LINE_ITEM_GUID)).thenReturn(expectedShoppingItem);

		final ShoppingItem actualShoppingItem = service.moveItemFromWishListToCart(shoppingCart, dto, WISHLIST_LINE_ITEM_GUID);

		assertThat(actualShoppingItem)
				.isSameAs(expectedShoppingItem);

		verify(wishList).removeItem(WISHLIST_LINE_ITEM_GUID);
		expectThatShoppingCartWillBePersisted();
		expectThatWishListWillBePersisted();
	}

	/**
	 * test moving item with quantity >1 from wish list to shopping cart.
	 */
	@Test
	public void testMoveItemWithQuantityFromWishListToCart() {
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 2);
		final ShoppingCart persistedShoppingCart = mock(ShoppingCart.class);
		final ShoppingItem expectedShoppingItem = mock(ShoppingItem.class);
		final ShoppingItem persistedShoppingItem = new ShoppingItemImpl();
		persistedShoppingItem.setGuid(WISHLIST_LINE_ITEM_GUID);

		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(cartDirector.addItemToCart(shoppingCart, dto)).thenReturn(persistedShoppingItem);
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);
		when(shoppingCartService.saveOrUpdate(shoppingCart)).thenReturn(persistedShoppingCart);
		when(persistedShoppingCart.getCartItemByGuid(WISHLIST_LINE_ITEM_GUID)).thenReturn(expectedShoppingItem);

		final ShoppingItem actualShoppingItem = service.moveItemFromWishListToCart(shoppingCart, dto, WISHLIST_LINE_ITEM_GUID);

		assertThat(actualShoppingItem)
				.isSameAs(expectedShoppingItem);

		expectThatShoppingCartWillBePersisted();
		verify(wishList).removeItem(WISHLIST_LINE_ITEM_GUID);
		verify(wishListService).save(wishList);
		verify(shoppingCart).setItemWithNoTierOneFromWishList(true);
	}

	/**
	 * test moving item from cart to wish list.
	 */
	@Test
	public void testMoveItemFromCartToWishList() {
		ShoppingItemImpl shoppingItem = new ShoppingItemImpl();
		shoppingItem.setGuid(CART_LINE_ITEM_GUID);
		when(shoppingCart.getCartItemByGuid(any())).thenReturn(mock(ShoppingItem.class));
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shoppingCart.getShoppingItemByGuid(CART_LINE_ITEM_GUID)).thenReturn(shoppingItem);
		when(shoppingCart.getStore()).thenReturn(store);
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);
		when(wishListService.addItem(wishList, shoppingItem)).thenReturn(new AddToWishlistResult(shoppingItem, true));

		service.moveItemFromCartToWishList(shoppingCart, CART_LINE_ITEM_GUID);

		expectThatWishListWillBePersisted();
		verify(shoppingItemService).deleteItemsByGuids(any());
	}

	/**
	 * Tests that the {@code saveShoppingCart} method uses the shoppingCartService to persist.
	 */
	@Test
	public void testSaveShoppingCart() {
		final ShoppingCart updatedShoppingCart = mock(ShoppingCart.class, "updatedShoppingCart");
		when(shoppingCartService.saveOrUpdate(shoppingCart)).thenReturn(updatedShoppingCart);

		ShoppingCart actualShoppingCart = service.saveShoppingCart(shoppingCart);

		assertEquals("The persisted shopping cart should be returned to the client", updatedShoppingCart, actualShoppingCart);
		expectThatShoppingCartWillBePersisted();
	}

	@Test
	public void verifyClearItemsClearsItems() throws Exception {
		service.clearItems(shoppingCart);

		verify(cartDirector).clearItems(shoppingCart);
		verify(cartDirector).reorderItems(shoppingCart);
		verify(pricingSnapshotService).getPricingSnapshotForCart(shoppingCart);
		verify(shoppingCartService).saveOrUpdate(shoppingCart);
	}

	private void expectThatShoppingCartWillBePersisted() {
		verify(cartDirector).reorderItems(shoppingCart);
		verify(pricingSnapshotService).getPricingSnapshotForCart(shoppingCart);
		verify(shoppingCartService).saveOrUpdate(shoppingCart);
	}

	private void expectThatWishListWillBePersisted() {
		verify(wishListService).save(wishList);
	}
}
