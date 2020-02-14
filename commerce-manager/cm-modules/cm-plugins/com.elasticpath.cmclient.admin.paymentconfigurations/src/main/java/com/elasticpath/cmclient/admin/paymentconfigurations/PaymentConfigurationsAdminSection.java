/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.elasticpath.cmclient.admin.AbstractAdminSection;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListView;

/**
 * Payment admin section.
 */
public class PaymentConfigurationsAdminSection extends AbstractAdminSection {

	@Override
	public void createItems(final FormToolkit toolkit, final Composite parent, final IWorkbenchPartSite site) {
		createItem(toolkit, parent, site, PaymentConfigurationsListView.VIEW_ID,
				AdminPaymentConfigurationMessages.get().PaymentConfigsAdminItemCompositeFactory_Admin,
				AdminPaymentConfigurationsImageRegistry.getImage(AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS));
	}

	@Override
	public boolean isAuthorized() {
		return AdminPaymentConfigurationsPlugin.isAuthorized();
	}
}
