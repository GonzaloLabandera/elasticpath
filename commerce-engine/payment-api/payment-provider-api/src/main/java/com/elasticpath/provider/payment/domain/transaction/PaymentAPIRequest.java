/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * Object PaymentAPI used to initiate payment events.
 */
public interface PaymentAPIRequest {

	/**
	 * Gets order context.
	 *
	 * @return the order context
	 */
	OrderContext getOrderContext();

	/**
	 * Sets order context.
	 *
	 * @param orderContext the order context
	 */
	void setOrderContext(OrderContext orderContext);

	/**
	 * Gets custom data required by the plugin.
	 *
	 * @return the data
	 */
	Map<String, String> getCustomRequestData();

	/**
	 * Sets custom data required by the plugin.
	 *
	 * @param data the data
	 */
	void setCustomRequestData(Map<String, String> data);
}
