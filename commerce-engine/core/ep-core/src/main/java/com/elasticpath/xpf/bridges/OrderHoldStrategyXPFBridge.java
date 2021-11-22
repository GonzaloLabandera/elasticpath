/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.bridges;

import java.util.List;

import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;

/**
 * Bridge between Core Entity concepts and Extension Point Entity concepts for order hold strategy.
 */
public interface OrderHoldStrategyXPFBridge {

	/**
	 * Returns list of order hold relative info.
	 *
	 * @param context object containing data required for execution
	 * @return order hold relative info
	 */
	List<OrderHold> evaluateOrderHolds(PreCaptureCheckoutActionContext context);
}
