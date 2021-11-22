/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.HashSet;
import java.util.Set;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;
import com.elasticpath.xpf.bridges.OrderHoldStrategyXPFBridge;

/**
 * Implements {@link ReversibleCheckoutAction} to handle the order hold strategies evaluation and take the following up actions.
 */
public class EvaluateOrderHoldStrategiesCheckoutAction implements ReversibleCheckoutAction {

	private OrderService orderService;

	private OrderHoldService orderHoldService;

	private OrderHoldStrategyXPFBridge orderHoldStrategyXPFBridge;

	/**
	 * Evaluates all order hold strategies and takes corresponding actions regarding evaluation result.
	 *
	 * @param context object containing data required for execution
	 */
	@Override
	public void execute(final PreCaptureCheckoutActionContext context) {

		final Order order = extractOrder(context);
		final Set<OrderHold> orderHolds = evaluateAllStrategies(context);

		if (orderHolds.isEmpty()) {
			triggerPostCaptureCheckout(order);
			return;
		}

		holdOrder(order, orderHolds);
	}

	/**
	 * Determines if the order should be evaluated for order holds or if all order hold strategy evaluations should be skipped.
	 *
	 * @param context the context to use when determining holdability
	 * @return false if the order is an exchange order and should not be evaluated for order holds
	 */
	protected boolean isOrderHoldable(final PreCaptureCheckoutActionContext context) {
		return !extractOrder(context).isExchangeOrder();
	}

	/**
	 * Evaluates each order hold strategy.
	 *
	 * @param context the shopping cart checkout action context.
	 * @return true if all evaluation results are true.
	 */
	protected Set<OrderHold> evaluateAllStrategies(final PreCaptureCheckoutActionContext context) {
		return new HashSet<>(orderHoldStrategyXPFBridge.evaluateOrderHolds(context));
	}

	/**
	 * Extract the order from context.
	 *
	 * @param context the shopping cart checkout action context.
	 * @return the extracted order.
	 */
	protected Order extractOrder(final PreCaptureCheckoutActionContext context) {
		return context.getOrder();
	}

	/**
	 * No op rollback - no actions to perform.
	 *
	 * @param context object containing data required for execution
	 * @throws EpSystemException
	 */

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		//do nothing - the create order action rollback should handle this situation by marking the order as failed
	}

	/**
	 * Update order since it should already have been added corresponding order hold record(s).
	 * Sets status of order to be on hold and publish order hold event.
	 *
	 * @param order      the order that needs held
	 * @param orderHolds the set of OrderHold entities to attach to the order
	 */
	protected void holdOrder(final Order order, final Set<OrderHold> orderHolds) {
		orderHoldService.holdOrder(order, orderHolds);
	}

	/**
	 * Triggers post capture checkout actions.
	 *
	 * @param order the order to be processed.
	 */
	protected void triggerPostCaptureCheckout(final Order order) {
		orderService.triggerPostCaptureCheckout(order);
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	protected OrderHoldService getOrderHoldService() {
		return orderHoldService;
	}

	public void setOrderHoldService(final OrderHoldService orderHoldService) {
		this.orderHoldService = orderHoldService;
	}

	protected OrderHoldStrategyXPFBridge getOrderHoldStrategyXPFBridge() {
		return orderHoldStrategyXPFBridge;
	}

	public void setOrderHoldStrategyXPFBridge(final OrderHoldStrategyXPFBridge orderHoldStrategyXPFBridge) {
		this.orderHoldStrategyXPFBridge = orderHoldStrategyXPFBridge;
	}
}
