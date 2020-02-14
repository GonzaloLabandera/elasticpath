/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.event;

import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListModel;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;

/**
 * Event listener for changes to objects which can be in change set.
 */
public interface PaymentConfigurationEventListener {

	/**
	 * Notifies for a changed payment configuration.
	 *
	 * @param event payment configuration change event
	 */
	void paymentConfigurationChanged(ItemChangeEvent<PaymentConfigurationsListModel> event);
}
