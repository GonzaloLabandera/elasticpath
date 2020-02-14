/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;

/**
 * Validation service for wishlists.
 */
public interface ItemValidationService {

	/**
	 * Validates if the item is purchasable.
	 *
	 * @param wishlistLineItemIdentifier wishlistLineItemIdentifier
	 * @return error messages if item is not purchasable
	 */
	Observable<Message> isItemPurchasable(WishlistLineItemIdentifier wishlistLineItemIdentifier);

}
