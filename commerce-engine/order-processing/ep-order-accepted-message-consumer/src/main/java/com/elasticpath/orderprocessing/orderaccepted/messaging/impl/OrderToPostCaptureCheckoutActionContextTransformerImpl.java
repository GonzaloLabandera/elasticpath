/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderaccepted.messaging.impl;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.orderprocessing.orderaccepted.messaging.OrderToPostCaptureCheckoutActionContextTransformer;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.impl.PostCaptureCheckoutActionContextImpl;

/**
 * Implements {@link OrderToPostCaptureCheckoutActionContextTransformer}.
 */
public class OrderToPostCaptureCheckoutActionContextTransformerImpl implements OrderToPostCaptureCheckoutActionContextTransformer {

	private static final String CM_USERNAME = "cm_username";
	private EventOriginatorHelper eventOriginatorHelper;

	private OrderService orderService;
	private CmUserService cmUserService;

	@Override
	public PostCaptureCheckoutActionContext transform(final EventMessage eventMessage) {

		final Order order = orderService.findOrderByOrderNumber(eventMessage.getGuid());

		if (order == null) {
			throw new EpServiceException("Cannot find order by order number " + eventMessage.getGuid());
		}
		order.setModifiedBy(extractEventOriginator(eventMessage));

		return new PostCaptureCheckoutActionContextImpl(order);
	}

	/**
	 * Extracts event originator from order event message.
	 *
	 * @param eventMessage the event message.
	 * @return the event originator.
	 */
	private EventOriginator extractEventOriginator(final EventMessage eventMessage) {

		final String cmUserName = (String) eventMessage.getData().get(CM_USERNAME);

		CmUser cmUser = null;

		if (cmUserName != null) {
			cmUser = getCmUserService().findByUserName(cmUserName);
		}

		if (cmUser == null) {
			return eventOriginatorHelper.getSystemOriginator();
		}

		return eventOriginatorHelper.getCmUserOriginator(cmUser);

	}

	public void setEventOriginatorHelper(final EventOriginatorHelper eventOriginatorHelper) {
		this.eventOriginatorHelper = eventOriginatorHelper;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setCmUserService(final CmUserService cmUserService) {
		this.cmUserService = cmUserService;
	}

	private CmUserService getCmUserService() {
		return cmUserService;
	}
}
