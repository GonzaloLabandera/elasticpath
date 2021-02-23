/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;

/**
 * Define a POJO class to hold shared info for processing.
 */
public class HoldResolutionContextImpl implements HoldResolutionContext {

	private Order order;
	private CmUser cmUser;
	private String comment;
	private OrderHoldStatus orderHoldStatus;
	private OrderHold orderHold;

	@Override
	public Order getOrder() {
		return order;
	}

	public void setOrder(final Order order) {
		this.order = order;
	}

	@Override
	public CmUser getCmUser() {
		return cmUser;
	}

	public void setCmUser(final CmUser cmUser) {
		this.cmUser = cmUser;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	@Override
	public OrderHoldStatus getOrderHoldStatus() {
		return orderHoldStatus;
	}

	public void setOrderHoldStatus(final OrderHoldStatus orderHoldStatus) {
		this.orderHoldStatus = orderHoldStatus;
	}

	@Override
	public OrderHold getOrderHold() {
		return orderHold;
	}

	public void setOrderHold(final OrderHold orderHold) {
		this.orderHold = orderHold;
	}
}
