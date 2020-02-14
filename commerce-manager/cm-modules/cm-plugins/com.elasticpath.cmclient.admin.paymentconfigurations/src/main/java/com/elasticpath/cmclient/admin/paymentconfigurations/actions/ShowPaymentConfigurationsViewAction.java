/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.actions;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationsPlugin;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListView;
import com.elasticpath.cmclient.core.EpAuthorizationException;
import com.elasticpath.cmclient.core.actions.AbstractAuthorizedShowViewAction;

/**
 * Opens payment gateway list view.
 */
public class ShowPaymentConfigurationsViewAction extends AbstractAuthorizedShowViewAction {

	@Override
	protected String getViewId() {
		if (!isAuthorized()) {
			throw new EpAuthorizationException(PaymentConfigurationsListView.VIEW_ID);
		}
		return PaymentConfigurationsListView.VIEW_ID;
	}

	@Override
	public boolean isAuthorized() {
		return AdminPaymentConfigurationsPlugin.isAuthorized();
	}
}
