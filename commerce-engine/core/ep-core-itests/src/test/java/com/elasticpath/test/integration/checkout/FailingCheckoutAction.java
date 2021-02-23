/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.test.integration.checkout;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversiblePostCaptureCheckoutAction;

/**
 * {@link ReversiblePostCaptureCheckoutAction} to ensure a {@link ShoppingCart} with physical goods cannot have a tokenized payment.
 */
public class FailingCheckoutAction implements ReversiblePostCaptureCheckoutAction {

	@Override
	public void execute(final PostCaptureCheckoutActionContext context) {
		throw new EpServiceException(this.getClass().getName() + " Failing order : "	+ context.getOrder().getGuid());
	}

	@Override
	public void rollback(final PostCaptureCheckoutActionContext context) throws EpSystemException {
		// No-op method as class is just used to trigger error cases for testing
	}
}
