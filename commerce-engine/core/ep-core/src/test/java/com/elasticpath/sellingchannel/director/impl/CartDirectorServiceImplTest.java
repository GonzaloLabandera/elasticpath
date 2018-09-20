/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.sellingchannel.director.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.impl.CartItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.CartItemModifierFieldValidationService;

@RunWith(MockitoJUnitRunner.class)
public class CartDirectorServiceImplTest {

	private static final String SKU_CODE = "skuCode";
	private static final String WISHLIST_LINE_ITEM_GUID = "WISHLIST_LINE_ITEM_GUID";
	private static final String CART_LINE_ITEM_GUID = "CART_LINE_ITEM_GUID";

	@Mock
	private CartDirector cartDirector;
	@Mock
	private ProductSkuLookup productSkuLookup;
	@Mock
	private ShoppingCartService shoppingCartService;
	@Mock
	private WishListService wishListService;
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private Shopper shopper;
	@Mock
	private Store store;
	@Mock
	private WishList wishList;
	@Mock
	private PricingSnapshotService pricingSnapshotService;
	@Mock
	private Validator validator;
	@Mock
	private ConstraintViolationTransformer constraintViolationTransformer;
	@Mock
	private CartItemModifierFieldValidationService cartItemModifierFieldValidationService;

	private CartDirectorServiceImpl service;

	@Before
	public void setUp() {
		service = new CartDirectorServiceImpl();
		service.setCartDirector(cartDirector);
		service.setProductSkuLookup(productSkuLookup);
		service.setShoppingCartService(shoppingCartService);
		service.setWishListService(wishListService);
		service.setPricingSnapshotService(pricingSnapshotService);
		service.setValidator(validator);
		service.setConstraintViolationTransformer(constraintViolationTransformer);
		service.setCartItemModifierFieldValidationService(cartItemModifierFieldValidationService);
	}

	private void setupAddAndUpdateActions(final ShoppingItemDto dto) {
		final ProductSku productSku = mock(ProductSku.class);
		final ProductType productType = mock(ProductType.class);
		final Product product = mock(Product.class);

		when(validator.validate(dto)).thenReturn(null);
		when(productSkuLookup.findBySkuCode(dto.getSkuCode())).thenReturn(productSku);
		when(productType.getCartItemModifierGroups()).thenReturn(Collections.emptySet());
		when(constraintViolationTransformer.transform(null)).thenReturn(Collections.emptyList());
		when(product.getProductType()).thenReturn(productType);
		when(productSku.getProduct()).thenReturn(product);

		when(cartItemModifierFieldValidationService.validate(dto.getItemFields(), Collections.emptySet())).thenReturn(
				Collections.emptyList());
	}

	private void setupSaveShoppingCartAction() {
		when(shoppingCartService.saveOrUpdate(shoppingCart)).thenReturn(shoppingCart);
	}

	@Test
	public void testAddItemToCartHappyPath() {
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 1);

		setupAddAndUpdateActions(dto);
		setupSaveShoppingCartAction();

		service.addItemToCart(shoppingCart, dto);

		verify(cartDirector).addItemToCart(shoppingCart, dto);
		expectThatShoppingCartWillBePersisted();
	}

	@Test
	public void testUpdateCartItemHappyPath() {
		final long itemId = 12345L;
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 99);

		setupAddAndUpdateActions(dto);
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
		final List<ShoppingItem> childItems = mock(List.class, "previousChildItems");
		final List<ShoppingItem> cartItems = mock(List.class, "previousCartItems");

		setupThatRootWillBeUpdatedAndDependentAndAssociatedItemsWillBeAdded(
				rootDto, rootItemId, updatedCartItem, dependentDto, associatedDto);

		when(updatedCartItem.getDependentItems()).thenReturn(Collections.singletonList(previousDependentItem));
		when(updatedCartItem.getChildren()).thenReturn(childItems);
		when(shoppingCart.getCartItems()).thenReturn(cartItems);

		// When
		service.updateCompoundCartItem(
				shoppingCart, rootItemId, rootDto,
				Collections.singletonList(dependentDto), Collections.singletonList(associatedDto));


		//  Note that previous <em>Associated</em> items are not deleted.
		//  This is pre-existing behaviour, I have no idea whether or not it is a bug or a feature or neither.
		verify(childItems).removeAll(Collections.singletonList(previousDependentItem));
		verify(cartItems).removeAll(Collections.singletonList(previousDependentItem));

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
		final String itemId1 = "12345";
		final String itemId2 = "54321";

		// When
		service.removeItemsFromCart(shoppingCart, itemId1, itemId2);

		verify(shoppingCart).removeCartItem(itemId1);
		verify(shoppingCart).removeCartItem(itemId2);

		expectThatShoppingCartWillBePersisted();
	}

	@Test
	public void testRefreshHappyPath() {
		service.refresh(shoppingCart);

		verify(cartDirector).refresh(shoppingCart);
		expectThatShoppingCartWillBePersisted();
	}

	@Test
	public void testAddSkuToWishListHappyPath() {
		final ShoppingItem shoppingItem = mock(ShoppingItem.class);

		when(cartDirector.createShoppingItem(SKU_CODE, store, 1)).thenReturn(shoppingItem);
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);

		service.addSkuToWishList(SKU_CODE, shopper, store);

		verify(wishListService).addItem(wishList, shoppingItem);
		expectThatWishListWillBePersisted();
	}

	/**
	 * test moving item from wish list to shopping cart.
	 */
	@Test
	public void testMoveItemFromWishListToCart() {
		final ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 1);

		setupAddAndUpdateActions(dto);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(cartDirector.isSkuAllowedAddToCart(SKU_CODE, shoppingCart)).thenReturn(true);
		when(cartDirector.addItemToCart(shoppingCart, dto)).thenReturn(new ShoppingItemImpl());
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);

		service.moveItemFromWishListToCart(shoppingCart, dto, WISHLIST_LINE_ITEM_GUID);

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

		setupAddAndUpdateActions(dto);

		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(cartDirector.isSkuAllowedAddToCart(SKU_CODE, shoppingCart)).thenReturn(true);
		when(cartDirector.addItemToCart(shoppingCart, dto)).thenReturn(new ShoppingItemImpl());
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);

		service.moveItemFromWishListToCart(shoppingCart, dto, WISHLIST_LINE_ITEM_GUID);

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

		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shoppingCart.getShoppingItemByGuid(CART_LINE_ITEM_GUID)).thenReturn(shoppingItem);
		when(wishListService.findOrCreateWishListByShopper(shopper)).thenReturn(wishList);
		when(wishListService.addItem(wishList, shoppingItem)).thenReturn(new AddToWishlistResult(shoppingItem, true));

		service.moveItemFromCartToWishList(shoppingCart, CART_LINE_ITEM_GUID);

		expectThatWishListWillBePersisted();
		expectThatShoppingCartWillBePersisted();
		verify(shoppingCart).removeCartItem(anyString());
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
