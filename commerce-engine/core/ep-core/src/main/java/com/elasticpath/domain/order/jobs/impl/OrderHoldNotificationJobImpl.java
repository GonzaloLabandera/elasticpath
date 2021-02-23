/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.order.jobs.impl;

import java.util.Set;

import com.elasticpath.domain.order.jobs.OrderHoldNotificationJob;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Job which will determine if there are ACTIVE order holds that need resolution and publish an event so that
 * a call-to-action notification can be sent.
 */
public class OrderHoldNotificationJobImpl implements OrderHoldNotificationJob {

	private OrderService orderService;

	private SettingsReader settingsReader;

	@Override
	public void publishHoldNotificationEvent() {
		Set<SettingValue> settingValues = settingsReader.getSettingValues(COMMERCE_STORE_ENABLE_DATA_HOLD_NOTIFICATION);

		settingValues.stream()
				.filter(SettingValue::getBooleanValue)
				.forEach(settingValue -> orderService.sendOrderHoldNotificationEvent(settingValue.getContext()));
	}

	public OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public SettingsReader getSettingsReader() {
		return settingsReader;
	}

	public void setSettingsReader(final SettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}
}
