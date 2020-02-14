/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.event;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListModel;
import com.elasticpath.cmclient.core.CmSingletonUtil;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;

/**
 * Event service for sending notifications on occurring events.
 */
public final class PaymentConfigurationEventService {

	private final List<PaymentConfigurationEventListener> paymentConfigurationEventListeners;

	/**
	 * Private constructor following the singleton pattern.
	 */
	private PaymentConfigurationEventService() {
		paymentConfigurationEventListeners = new ArrayList<>();
	}

	/**
	 * Gets a singleton instance of <code>PaymentConfigurationEventService</code>.
	 *
	 * @return singleton instance of <code>PaymentConfigurationEventService</code>
	 */
	public static PaymentConfigurationEventService getInstance() {
		return CmSingletonUtil.getSessionInstance(PaymentConfigurationEventService.class);
	}

	/**
	 * Notifies all the listeners with an <code>ItemChangeEvent</code> event.
	 *
	 * @param event the payment configuration change event
	 */
	public void firePaymentConfigurationChanged(final ItemChangeEvent<PaymentConfigurationsListModel> event) {
		for (final PaymentConfigurationEventListener eventListener : paymentConfigurationEventListeners) {
			eventListener.paymentConfigurationChanged(event);
		}
	}

	/**
	 * Registers a <code>ItemChangeEvent</code> listener.
	 *
	 * @param listener the payment configuration event listener
	 */
	public void registerPaymentConfigurationEventListener(final PaymentConfigurationEventListener listener) {
		if (!paymentConfigurationEventListeners.contains(listener)) {
			paymentConfigurationEventListeners.add(listener);
		}
	}

	/**
	 * Unregisters a <code>ItemChangeEvent</code> listener.
	 *
	 * @param listener the payment configuration event listener
	 */
	public void unregisterPaymentConfigurationEventListener(final PaymentConfigurationEventListener listener) {
		paymentConfigurationEventListeners.remove(listener);
	}
}
