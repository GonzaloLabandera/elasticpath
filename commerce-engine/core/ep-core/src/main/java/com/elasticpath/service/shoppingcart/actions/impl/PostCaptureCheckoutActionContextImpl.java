/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.domain.order.Order;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;

/**
 * Implements {@link PostCaptureCheckoutActionContext}.
 */
public class PostCaptureCheckoutActionContextImpl extends CheckoutActionContextImpl implements PostCaptureCheckoutActionContext {

	/**
	 * Constructor for creating a context from an order.
	 *
	 * @param order the order to use to construct the context
	 */
	public PostCaptureCheckoutActionContextImpl(final Order order) {
		super(false, false, null);
		setOrder(order);
	}
}
