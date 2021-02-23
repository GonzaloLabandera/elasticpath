/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

import java.util.Optional;

import com.elasticpath.domain.order.OrderHold;

/**
 * Defining interface to evaluate the specific order hold strategy.
 */
public interface OrderHoldStrategy {

	/**
	 * Evaluate the given context.
	 *
	 * @param context the shopping cart checkout context.
	 * @return the optional of order hold entity.
	 */
	Optional<OrderHold> evaluate(PreCaptureCheckoutActionContext context);

}
