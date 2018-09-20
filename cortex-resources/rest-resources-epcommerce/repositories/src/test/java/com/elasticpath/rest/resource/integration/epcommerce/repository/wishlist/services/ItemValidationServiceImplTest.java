/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.services;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LINE_ITEM_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.NOT_FOUND;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.WISHLIST_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistTestFactory.buildWishlistLineItemIdentifier;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingItemValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistRepository;

/**
 * Test for {@link ItemValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemValidationServiceImplTest {

	private final WishlistLineItemIdentifier wishlistLineItemIdentifier = buildWishlistLineItemIdentifier();

	@Mock
	private Message message;

	@Mock
	private ProductSku productSku;

	@Mock
	private WishList wishList;

	@InjectMocks
	private ItemValidationServiceImpl service;

	@Mock
	private WishlistRepository wishlistRepository;

	@Mock
	private ShoppingItemValidationService shoppingItemValidationService;

	@Test
	public void verifyIsItemPurchasableReturnNotFoundWhenWishlistNotFound() {
		when(wishlistRepository.getWishlist(WISHLIST_ID)).thenReturn(Single.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		service.isItemPurchasable(wishlistLineItemIdentifier)
				.test()
				.assertError(createErrorCheckPredicate(NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyIsItemPurchasableReturnNotFoundWhenProductSkuNotFound() {
		when(wishlistRepository.getWishlist(WISHLIST_ID)).thenReturn(Single.just(wishList));
		when(wishlistRepository.getProductSku(wishList, LINE_ITEM_ID)).thenReturn(Single.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		service.isItemPurchasable(wishlistLineItemIdentifier)
				.test()
				.assertError(createErrorCheckPredicate(NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyIsItemPurchasableReturnNotFoundWhenShoppingItemValidationServiceReturnNotFound() {
		when(wishlistRepository.getWishlist(WISHLIST_ID)).thenReturn(Single.just(wishList));
		when(wishlistRepository.getProductSku(wishList, LINE_ITEM_ID)).thenReturn(Single.just(productSku));
		when(wishList.getStoreCode()).thenReturn(SCOPE);
		when(shoppingItemValidationService.validateItemPurchasable(SCOPE, productSku))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		service.isItemPurchasable(wishlistLineItemIdentifier)
				.test()
				.assertError(createErrorCheckPredicate(NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyIsItemPurchasableReturnMessage() {
		when(wishlistRepository.getWishlist(WISHLIST_ID)).thenReturn(Single.just(wishList));
		when(wishlistRepository.getProductSku(wishList, LINE_ITEM_ID)).thenReturn(Single.just(productSku));
		when(wishList.getStoreCode()).thenReturn(SCOPE);
		when(shoppingItemValidationService.validateItemPurchasable(SCOPE, productSku))
				.thenReturn(Observable.just(message));

		service.isItemPurchasable(wishlistLineItemIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(message);
	}
}
