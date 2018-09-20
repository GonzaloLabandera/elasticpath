/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist;

import io.reactivex.Single;

import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;
import com.elasticpath.rest.form.SubmitResult;

/**
 * Service interface for moving an item from cart to wishlist.
 */
public interface MoveToWishlistService {

	/**
	 * Move an item from cart to wishlist.
	 *
	 * @param lineItemIdentifier the line item identifier
	 * @return the wishlist line item identifier
	 */
	Single<SubmitResult<WishlistLineItemIdentifier>> move(LineItemIdentifier lineItemIdentifier);
}
