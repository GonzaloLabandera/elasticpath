/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

/**
 * Defining service for post capture checkout.
 */
public interface PostCaptureCheckoutService {

	/**
	 * Checkout with context.
	 *
	 * @param context the post capture checkout action context.
	 */
	void completeCheckout(PostCaptureCheckoutActionContext context);

}
