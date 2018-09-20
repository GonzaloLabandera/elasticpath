/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.services;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.wishlists.WishlistIdentifier;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingItemValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.ItemValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistRepository;

/**
 * Item validation service.
 */
@Component
public class ItemValidationServiceImpl implements ItemValidationService {

	private WishlistRepository wishlistRepository;
	private ShoppingItemValidationService shoppingItemValidationService;

	@Override
	public Observable<Message> isItemPurchasable(final WishlistLineItemIdentifier wishlistLineItemIdentifier) {
		WishlistIdentifier wishlistIdentifier = wishlistLineItemIdentifier.getWishlistLineItems().getWishlist();
		String lineItemGuid = wishlistLineItemIdentifier.getLineItemId().getValue();
		String wishlistId = wishlistIdentifier.getWishlistId().getValue();
		
		return wishlistRepository.getWishlist(wishlistId)
				.flatMapObservable(toMessages(lineItemGuid));
	}

	/**
	 * Get structured advise messages, if any.
	 * @param lineItemGuid the line item guid
	 * @return the function
	 */
	protected Function<WishList, Observable<Message>> toMessages(final String lineItemGuid) {
		return wishList -> wishlistRepository.getProductSku(wishList, lineItemGuid)
				.flatMapObservable(productSku -> shoppingItemValidationService.validateItemPurchasable(wishList.getStoreCode(), productSku));
	}

	@Reference
	public void setWishlistRepository(final WishlistRepository wishlistRepository) {
		this.wishlistRepository = wishlistRepository;
	}

	@Reference
	public void setShoppingItemValidationService(final ShoppingItemValidationService shoppingItemValidationService) {
		this.shoppingItemValidationService = shoppingItemValidationService;
	}
}
