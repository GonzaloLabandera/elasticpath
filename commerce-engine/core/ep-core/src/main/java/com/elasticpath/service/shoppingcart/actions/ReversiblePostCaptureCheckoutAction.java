/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

import com.elasticpath.base.exception.EpSystemException;

/**
 * Interface for defining a roll back checkout action.
 */
public interface ReversiblePostCaptureCheckoutAction extends PostCaptureCheckoutAction {

	/**
	 * Rollback procedure to execute if later checkout actions fail.
	 *
	 * @param context object containing data required for execution
	 * @throws EpSystemException exception object which could be thrown by execution
	 */
	void rollback(PostCaptureCheckoutActionContext context) throws EpSystemException;

}
