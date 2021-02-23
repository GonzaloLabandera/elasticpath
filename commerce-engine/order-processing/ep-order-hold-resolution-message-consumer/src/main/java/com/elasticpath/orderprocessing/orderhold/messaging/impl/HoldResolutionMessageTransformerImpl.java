/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging.impl;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.orderprocessing.orderhold.messaging.HoldResolutionContext;
import com.elasticpath.orderprocessing.orderhold.messaging.HoldResolutionContextImpl;
import com.elasticpath.orderprocessing.orderhold.messaging.HoldResolutionMessageTransformer;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderService;

/**
 * Implements {@link HoldResolutionMessageTransformer}.
 */
public class HoldResolutionMessageTransformerImpl implements HoldResolutionMessageTransformer {

	private static final String CM_USERNAME = "cm_username";
	private static final String STATUS = "status";
	private static final String COMMENT = "comment";
	private static final String ORDERHOLD_GUID = "orderhold_guid";

	private OrderHoldService orderHoldService;
	private OrderService orderService;
	private CmUserService cmUserService;

	@Override
	public HoldResolutionContext transform(final EventMessage eventMessage) {

		final String comment = (String) eventMessage.getData().get(COMMENT);
		final String orderHoldGuid = (String) eventMessage.getData().get(ORDERHOLD_GUID);

		final HoldResolutionContextImpl context = new HoldResolutionContextImpl();

		context.setCmUser(getCmUser(eventMessage));
		context.setOrder(getOrder(eventMessage));
		context.setOrderHoldStatus(getStatus(eventMessage));
		context.setComment(comment);
		context.setOrderHold(getOrderHold(orderHoldGuid));

		return context;
	}

	private OrderHold getOrderHold(final String orderHoldGuid) {
		return orderHoldService.getByGuid(orderHoldGuid);
	}

	private Order getOrder(final EventMessage eventMessage) {
		final String orderNumber = eventMessage.getGuid();
		final Order order = orderService.findOrderByOrderNumber(orderNumber);
		if (order == null) {
			throw new EpServiceException("Cannot find order with order number " + orderNumber);
		}
		return order;
	}

	private OrderHoldStatus getStatus(final EventMessage eventMessage) {
		final String status = (String) eventMessage.getData().get(STATUS);
		return OrderHoldStatus.valueOf(status);
	}

	private CmUser getCmUser(final EventMessage eventMessage) {
		final String cmUserName = (String) eventMessage.getData().get(CM_USERNAME);
		CmUser holdResolvedBy = null;
		if (StringUtils.isNotEmpty(cmUserName)) {
			holdResolvedBy =  cmUserService.findByUserName(cmUserName);
		}

		if (holdResolvedBy == null) {
			throw new EpServiceException("Cannot find CM user with username " + cmUserName);
		}
		return holdResolvedBy;
	}

	public void setOrderHoldService(final OrderHoldService orderHoldService) {
		this.orderHoldService = orderHoldService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setCmUserService(final CmUserService cmUserService) {
		this.cmUserService = cmUserService;
	}
}
