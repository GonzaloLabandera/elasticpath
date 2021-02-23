/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;

/**
 * Defines the interface of context of hold resolution.
 */
public interface HoldResolutionContext {

	/**
	 * Gets order.
	 *
	 * @return the order.
	 */
	Order getOrder();

	/**
	 * Gets cm user.
	 *
	 * @return the cm user.
	 */
	CmUser getCmUser();

	/**
	 * Gets comment.
	 *
	 * @return the comment of hold resolution.
	 */
	String getComment();

	/**
	 * Gets the status of order hold.
	 *
	 * @return the status of order hold.
	 */
	OrderHoldStatus getOrderHoldStatus();

	/**
	 * Gets the order hold instance to be resolved.
	 *
	 * @return the order hold.
	 */
	OrderHold getOrderHold();
}
