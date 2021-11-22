/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.shopper.impl;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.service.customer.CustomerSessionShopperUpdateHandler;
import com.elasticpath.service.shoppingcart.WishListService;

/**
 * Handles what happens to the {@link WishList} when a {@link Shopper} is switched out.
 */
public final class WishListMergerForShopperUpdates implements CustomerSessionShopperUpdateHandler {

	private final WishListService wishListService;

	/**
	 * @param wishListService wishlist service
	 */
	public WishListMergerForShopperUpdates(final WishListService wishListService) {
		this.wishListService = wishListService;
	}

	@Override
	public void invalidateShopper(final Shopper invalidShopper, final Shopper newShopper) {
		
		final WishList newWishList = wishListService.findOrCreateWishListByShopper(newShopper);
		final WishList invalidatedWishList = wishListService.findOrCreateWishListByShopper(invalidShopper);
		
		if (invalidatedWishList.equals(newWishList)) {
			return;
		}
		
		wishListService.addAllItems(newWishList, invalidatedWishList.getAllItems());
		wishListService.save(newWishList);

		wishListService.remove(invalidatedWishList);
	}
}
