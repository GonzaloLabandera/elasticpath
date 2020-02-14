/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationMessages;
import com.elasticpath.cmclient.admin.paymentconfigurations.event.PaymentConfigurationEventService;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListModel;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListView;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Activate payment configuration action.
 */
public class ActivatePaymentConfigurationsAction extends Action {

	private static final Logger LOG = Logger.getLogger(ActivatePaymentConfigurationsAction.class);

	private final PaymentConfigurationsListView listView;

	/**
	 * The constructor.
	 *
	 * @param listView PaymentListView this action is associated with
	 * @param text action text
	 * @param imageDescriptor action image
	 */
	public ActivatePaymentConfigurationsAction(
			final PaymentConfigurationsListView listView,
			final String text,
			final ImageDescriptor imageDescriptor) {
		super(text, imageDescriptor);
		this.listView = listView;
	}

	@Override
	public void run() {
		LOG.debug("ActivatePaymentConfigurationsAction Action called."); //$NON-NLS-1$

		final PaymentProviderConfigManagementService paymentProviderConfigManagementService = BeanLocator.getSingletonBean(
				ContextIdNames.PAYMENT_PROVIDER_CONFIG_MANAGEMENT_SERVICE, PaymentProviderConfigManagementService.class);
		PaymentConfigurationsListModel paymentProviderConfig = listView.getSelectedPaymentConfiguration();


		if (paymentProviderConfig.getStatus().equals(PaymentProviderConfigurationStatus.DRAFT)) {
			final boolean answerYes = MessageDialog.openConfirm(listView.getSite().getShell(),
					AdminPaymentConfigurationMessages.get().PaymentConfiguration_ActivateDialogTitle,
					AdminPaymentConfigurationMessages.get().PaymentConfiguration_ActivateConfirmation);
			if (answerYes) {
				paymentProviderConfig.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
				paymentProviderConfigManagementService.saveOrUpdate(paymentProviderConfig.getConfigDto());
				PaymentConfigurationEventService.getInstance()
						.firePaymentConfigurationChanged(new ItemChangeEvent<>(this, paymentProviderConfig, ItemChangeEvent.EventType.REMOVE));
			}
		}
	}
}
