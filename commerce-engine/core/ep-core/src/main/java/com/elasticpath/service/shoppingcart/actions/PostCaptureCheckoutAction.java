/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

/**
 * Defining execution for post capture checkout action.
 */
public interface PostCaptureCheckoutAction {

	/**
	 * Execute.
	 *
	 * @param context the post capture checkout action context.
	 */
	void execute(PostCaptureCheckoutActionContext context);

}
