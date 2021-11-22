/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.domain.order.OrderLock;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.orderprocessing.orderhold.messaging.HoldResolutionContext;
import com.elasticpath.orderprocessing.orderhold.messaging.HoldResolutionMessageProcessor;
import com.elasticpath.orderprocessing.orderhold.messaging.UnableToLockOrderException;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderLockService;
import com.elasticpath.service.order.OrderService;

/**
 * Implements {@link HoldResolutionMessageProcessor}.
 */
public class HoldResolutionMessageProcessorImpl implements HoldResolutionMessageProcessor {

	private static final Logger LOG = LogManager.getLogger(HoldResolutionMessageProcessorImpl.class);

	private OrderLockService orderLockService;
	private OrderService orderService;
	private OrderHoldService orderHoldService;
	private CmUserService cmUserService;
	private EventOriginatorHelper eventOriginatorHelper;
	private TimeService timeService;

	@Override
	public void process(final HoldResolutionContext context) {

		if (!isResolved(context.getOrderHoldStatus())
				&& !isUnresolvable(context.getOrderHoldStatus())) {
			LOG.warn("Cannot resolve order hold [{}] with unknown status [{}].  Message will be ignored.", context.getOrderHold().getGuid(),
					context.getOrderHoldStatus());
			return;
		}

		if (!hasPermission(context.getCmUser(), context.getOrderHold())) {
			LOG.warn("User [{}] does not have permission [{}] to resolve hold for order [{}].  Message will be ignored.",
					context.getCmUser().getUsername(), context.getOrderHold().getPermission(), context.getOrder().getOrderNumber());
			return;
		}

		try {
			lockOrder(context.getOrder(), context.getCmUser());

			//make sure that this order wasn't resolved by another ORDER_HOLDS_RESOLVED message before we could grab the lock
			if (!isOnHold(context.getOrder())) {
				LOG.warn("Cannot resolve order hold [{}] as order [{}] is no longer on hold.  Message will be ignored.",
						context.getOrderHold().getGuid(), context.getOrder().getOrderNumber());
				return;
			}

			if (isUnresolvable(context.getOrderHoldStatus())) {
				rejectOrder(context);
			} else if (isResolved(context.getOrderHoldStatus())) {
				final Order updatedOrder = updateOrderHold(context);
				if (orderHoldService.isAllHoldsResolvedForOrder(updatedOrder.getUidPk())) {
					triggerPostCaptureCheckout(updatedOrder);
				}
			}
		} catch (Exception exception) {
			LOG.error("Error resolving hold [" + context.getOrderHold().getGuid()
					+ "] for order [" + context.getOrder().getOrderNumber() + "].  Order may currently be locked.  Message "
					+ "processing will be reattempted, but may end up on DLQ", exception);
		} finally {
			releaseOrderLock(context.getOrder(), context.getCmUser());
		}


	}

	/**
	 * Triggers post capture checkout actions.
	 *
	 * @param order the order to be processed.
	 */
	private void triggerPostCaptureCheckout(final Order order) {
		orderService.triggerPostCaptureCheckout(order);
	}

	/**
	 * Check if given commerce manager user has the permission to resolve it.
	 *
	 * @param cmUser        the cm user.
	 * @param orderHold 	the order hold that is being resolved.
	 * @return true if the commerce manager user has permission.
	 */
	private boolean hasPermission(final CmUser cmUser, final OrderHold orderHold) {
		return cmUser.hasPermission(orderHold.getPermission());
	}

	/**
	 * Check if all of the order holds have got resolved.
	 *
	 * @param status the status of the order hold.
	 * @return true if all of the order holds have got resolved.
	 */
	private boolean isResolved(final OrderHoldStatus status) {
		return status == OrderHoldStatus.RESOLVED;
	}

	/**
	 * Check if the order hold is unresolvable.
	 *
	 * @param status the status of order hold.
	 * @return true if it is unresolvable status.
	 */
	private boolean isUnresolvable(final OrderHoldStatus status) {
		return status == OrderHoldStatus.UNRESOLVABLE;
	}

	/**
	 * Rejects order to be cancel and with updated order hold details.
	 *
	 * @param context the context.
	 */
	private void rejectOrder(final HoldResolutionContext context) {

		final Order updatedOrder = updateOrderHold(context);

		orderService.cancelOrder(updatedOrder);

	}

	/**
	 * Updates the order hold in order with details in context.
	 *
	 * @param context the context with details.
	 * @return the updated order.
	 */
	private Order updateOrderHold(final HoldResolutionContext context) {

		populateOrderHold(context.getOrderHold(), context.getOrderHoldStatus(), context.getComment(), context.getCmUser());

		return updateOrder(context.getOrder(), context.getCmUser());

	}

	/**
	 * Updates order and populates its modified by.
	 *
	 * @param order  the order to be updated.
	 * @param cmUser the cm user who modifies the order.
	 * @return the updated order.
	 */
	private Order updateOrder(final Order order, final CmUser cmUser) {

		final Order updatedOrder = orderService.update(order);
		updatedOrder.setModifiedBy(eventOriginatorHelper.getCmUserOriginator(cmUser));

		return updatedOrder;

	}

	/**
	 * Populates the order hold with details.
	 *
	 * @param orderHold   the order hold.
	 * @param status      the status.
	 * @param reviewNotes the review notes.
	 * @param resolvedBy  the cm user who resolved.
	 */
	private void populateOrderHold(final OrderHold orderHold, final OrderHoldStatus status, final String reviewNotes, final CmUser resolvedBy) {
		orderHold.setStatus(status);
		orderHold.setReviewerNotes(reviewNotes);
		orderHold.setResolvedDate(timeService.getCurrentTime());
		orderHold.setResolvedBy(resolvedBy.getUserName());

		orderHoldService.update(orderHold);
	}

	/**
	 * Check if an order is in on hold status.
	 *
	 * @param order the order
	 * @return true if the order is on hold.
	 */
	private boolean isOnHold(final Order order) {
		return order.getStatus().equals(OrderStatus.ONHOLD);
	}

	/**
	 * Lock the order to perform further action.
	 *
	 * @param order  the order needs to be locked.
	 * @param cmUser the commerce manager user intends to lock the order.
	 */
	private void lockOrder(final Order order, final CmUser cmUser) {
		try {
			final OrderLock orderLock = orderLockService.writeOrderLock(order.getOrderNumber(), cmUser.getUidPk());
			if (orderLock == null) {
				throw new UnableToLockOrderException("Lock order [" + order.getOrderNumber() + "] failed");
			}
		} catch (EpPersistenceException epPersistenceException) {
			LOG.warn("Unable to lock order [" + order.getOrderNumber()
					+ "] to resolve hold.  Up to 6 attempts will be made before giving up.", epPersistenceException);
			throw new UnableToLockOrderException("Lock order [" + order.getOrderNumber() + "] failed due to pre-existing lock.",
					epPersistenceException);
		}
	}

	/**
	 * Release the order lock by order and cmuser.
	 *
	 * @param order  the order had been locked.
	 * @param cmUser the user of commerce manager who locked the order.
	 */
	private void releaseOrderLock(final Order order, final CmUser cmUser) {
		final OrderLock orderLock = orderLockService.getOrderLock(order);
		if (orderLock == null) {
			return;
		}
		orderLockService.releaseOrderLock(orderLock, cmUser);
	}

	public void setOrderLockService(final OrderLockService orderLockService) {
		this.orderLockService = orderLockService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setOrderHoldService(final OrderHoldService orderHoldService) {
		this.orderHoldService = orderHoldService;
	}

	public CmUserService getCmUserService() {
		return cmUserService;
	}

	public void setCmUserService(final CmUserService cmUserService) {
		this.cmUserService = cmUserService;
	}

	public void setEventOriginatorHelper(final EventOriginatorHelper eventOriginatorHelper) {
		this.eventOriginatorHelper = eventOriginatorHelper;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}
}


