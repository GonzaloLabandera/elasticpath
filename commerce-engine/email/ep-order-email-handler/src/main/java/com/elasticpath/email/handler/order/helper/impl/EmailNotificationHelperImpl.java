/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.email.handler.order.helper.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.order.helper.EmailNotificationHelper;
import com.elasticpath.email.handler.order.helper.OrderEmailPropertyHelper;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.OrderService;

/**
 * EmailNotificationHelper a notification helper class to encapsulate the
 * dependencies and functionality required to send an email.
 */
public class EmailNotificationHelperImpl implements EmailNotificationHelper {

	private BeanFactory beanFactory;
	private OrderService orderService;
	private ProductSkuLookup productSkuLookup;


	@Override
	public EmailProperties getOrderEmailProperties(final String orderNumber) {
		final Order order = orderService.findOrderByOrderNumber(orderNumber);
		final OrderEmailPropertyHelper orderEmailPropertyHelper = beanFactory.getSingletonBean(ContextIdNames.EMAIL_PROPERTY_HELPER_ORDER,
				OrderEmailPropertyHelper.class);
		return orderEmailPropertyHelper.getOrderConfirmationEmailProperties(order);
	}

	/**
	 * Set the order service.
	 *
	 * @param orderService
	 *            is the order service
	 */
	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	/**
	 * Set the bean factory.
	 *
	 * @param beanFactory
	 *            is the bean factory
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public EmailProperties getShipmentConfirmationEmailProperties(
			final String orderNumber, final String shipmentNumber) {
		Order order = orderService.findOrderByOrderNumber(orderNumber);

		OrderShipment orderShipment = order.getShipment(shipmentNumber);

		OrderEmailPropertyHelper orderEmailPropertyHelper = beanFactory
				.getSingletonBean(ContextIdNames.EMAIL_PROPERTY_HELPER_ORDER, OrderEmailPropertyHelper.class);

		return orderEmailPropertyHelper.getShipmentConfirmationEmailProperties(
				order, orderShipment);
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}
}
