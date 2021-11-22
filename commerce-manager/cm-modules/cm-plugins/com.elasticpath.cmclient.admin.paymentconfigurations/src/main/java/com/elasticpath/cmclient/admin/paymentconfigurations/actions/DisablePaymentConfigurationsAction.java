/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.actions;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationMessages;
import com.elasticpath.cmclient.admin.paymentconfigurations.event.PaymentConfigurationEventService;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListModel;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListView;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;
import com.elasticpath.service.store.StoreService;

/**
 * Activate payment configuration action.
 */
public class DisablePaymentConfigurationsAction extends Action {

	private static final Logger LOG = LogManager.getLogger(DisablePaymentConfigurationsAction.class);

	private final PaymentConfigurationsListView listView;

	/**
	 * The constructor.
	 *
	 * @param listView        PaymentListView this action is associated with
	 * @param text            action text
	 * @param imageDescriptor action image
	 */
	public DisablePaymentConfigurationsAction(
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
		final StorePaymentProviderConfigService storePaymentProviderConfigService = BeanLocator.getSingletonBean(
				ContextIdNames.STORE_PAYMENT_PROVIDER_CONFIG_SERVICE, StorePaymentProviderConfigService.class);
		PaymentConfigurationsListModel paymentProviderConfig = listView.getSelectedPaymentConfiguration();
		final Collection<StorePaymentProviderConfig> storePaymentProviderConfigs =
				storePaymentProviderConfigService.findByPaymentProviderConfigGuid(paymentProviderConfig.getGuid());

		if (storePaymentProviderConfigs.isEmpty()) {
			final boolean answerYes = MessageDialog.openConfirm(listView.getSite().getShell(),
					AdminPaymentConfigurationMessages.get().PaymentConfiguration_DisableDialogTitle,
					AdminPaymentConfigurationMessages.get().PaymentConfiguration_DisableConfirmation);
			if (answerYes) {
				paymentProviderConfig.setStatus(PaymentProviderConfigurationStatus.DISABLED);
				paymentProviderConfigManagementService.saveOrUpdate(paymentProviderConfig.getConfigDto());
				PaymentConfigurationEventService.getInstance()
						.firePaymentConfigurationChanged(new ItemChangeEvent<>(this, paymentProviderConfig, ItemChangeEvent.EventType.REMOVE));
			}
			return;
		}

		final StoreService storeService = BeanLocator.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class);
		final String storeNames = getStoreNames(storePaymentProviderConfigs, storeService);
		MessageDialog.openInformation(listView.getSite().getShell(),
				AdminPaymentConfigurationMessages.get().PaymentConfiguration_DisableDeniedDialogTitle,
				NLS.bind(AdminPaymentConfigurationMessages.get().PaymentConfiguration_DisableDeniedConfirmation,
						storeNames));

	}

	private String getStoreNames(final Collection<StorePaymentProviderConfig> storePaymentProviderConfigs,
								 final StoreService storeService) {
		return storePaymentProviderConfigs.stream()
				.map(storePaymentProviderConfig -> storeService.findStoreWithCode(storePaymentProviderConfig.getStoreCode()))
				.map(Store::getName)
				.collect(Collectors.joining(", "));
	}
}
