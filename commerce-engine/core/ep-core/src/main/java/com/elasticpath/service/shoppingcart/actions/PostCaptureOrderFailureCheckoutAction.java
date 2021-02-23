/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.actions;

/**
 * An interface for checkout actions that should be invoked as part of post capture checkout rollback processing.
 */
public interface PostCaptureOrderFailureCheckoutAction {

	/**
	 * This method will be invoked as part of post capture rollback processing.
	 * @param context - the context to use when cleaning up post capture checkout processing
	 * @param causeForFailure - the exception that triggered the rollback/cleanup processing
	 */
	void postCaptureRollback(PostCaptureCheckoutActionContext context, Exception causeForFailure);

}
