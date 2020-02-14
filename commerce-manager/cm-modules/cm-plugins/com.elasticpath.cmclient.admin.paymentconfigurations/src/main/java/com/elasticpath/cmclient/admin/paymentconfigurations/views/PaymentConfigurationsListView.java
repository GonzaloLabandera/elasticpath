/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.views;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationMessages;
import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationsImageRegistry;
import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationsPlugin;
import com.elasticpath.cmclient.admin.paymentconfigurations.actions.ActivatePaymentConfigurationsAction;
import com.elasticpath.cmclient.admin.paymentconfigurations.actions.CreatePaymentConfigurationsAction;
import com.elasticpath.cmclient.admin.paymentconfigurations.actions.DisablePaymentConfigurationsAction;
import com.elasticpath.cmclient.admin.paymentconfigurations.actions.EditPaymentConfigurationsAction;
import com.elasticpath.cmclient.admin.paymentconfigurations.event.PaymentConfigurationEventListener;
import com.elasticpath.cmclient.admin.paymentconfigurations.event.PaymentConfigurationEventService;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.views.AbstractListView;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderManagementService;

/**
 * View to show and allow the manipulation of the available Payment Configurations in CM.
 */
public class PaymentConfigurationsListView extends AbstractListView implements PaymentConfigurationEventListener {

	/**
	 * The View's ID.
	 */
	public static final String VIEW_ID = "com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListView"; //$NON-NLS-1$

	// Column indices
	/**
	 * payment name column.
	 */
	public static final int INDEX_PAYMENT_CONFIGURATION_NAME = 0;
	/**
	 * payment provider column.
	 */
	public static final int INDEX_PAYMENT_CONFIGURATION_PROVIDER = 1;
	/**
	 * payment method column.
	 */
	public static final int INDEX_PAYMENT_CONFIGURATION_METHOD = 2;
	/**
	 * payment stores column.
	 */
	public static final int INDEX_PAYMENT_CONFIGURATION_STORES = 3;
	/**
	 * payment status column.
	 */
	public static final int INDEX_PAYMENT_CONFIGURATION_STATUS = 4;

	private static final String PAYMENT_TABLE_NAME = "Payment Configurations"; //$NON-NLS-1$
	private static final String MISSING_PAYMENT_PROVIDER_MARKER = "MISSING";

	/**
	 * Column widths for payment gateway's list view.
	 */
	private static final int[] COLUMN_WIDTHS = new int[]{200, 100, 150, 300, 100};

	private final PaymentProviderConfigManagementService paymentProviderConfigManagementService;
	private final StorePaymentProviderConfigService storePaymentProviderConfigService;

	private final Map<String, PaymentProviderPluginDTO> paymentProviderPlugins;

	// Actions
	private Action editPaymentConfigurationsAction;
	private Action activatePaymentConfigurationsAction;
	private Action disablePaymentConfigurationsAction;

	/**
	 * The constructor.
	 */
	public PaymentConfigurationsListView() {
		super(false, PAYMENT_TABLE_NAME);
		paymentProviderConfigManagementService =
				BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_CONFIG_MANAGEMENT_SERVICE,
						PaymentProviderConfigManagementService.class);
		storePaymentProviderConfigService =
				BeanLocator.getSingletonBean(ContextIdNames.STORE_PAYMENT_PROVIDER_CONFIG_SERVICE, StorePaymentProviderConfigService.class);
		PaymentProviderManagementService paymentProviderManagementService =
				BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_MANAGEMENT_SERVICE, PaymentProviderManagementService.class);
		paymentProviderPlugins = paymentProviderManagementService.findAll();
		PaymentConfigurationEventService.getInstance().registerPaymentConfigurationEventListener(this);
	}

	@Override
	protected String getPluginId() {
		return AdminPaymentConfigurationsPlugin.PLUGIN_ID;
	}

	@Override
	protected void initializeViewToolbar() {
		Separator paymentConfigurationsActionGroup = new Separator("paymentConfigurationsActionGroup"); //$NON-NLS-1$

		getToolbarManager().add(paymentConfigurationsActionGroup);

		// Actions
		Action createPaymentConfigurationsAction = new CreatePaymentConfigurationsAction(this,
				AdminPaymentConfigurationMessages.get().CreatePaymentConfiguration,
				AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS_CREATE);
		createPaymentConfigurationsAction.setToolTipText(AdminPaymentConfigurationMessages.get().CreatePaymentConfiguration);

		editPaymentConfigurationsAction = new EditPaymentConfigurationsAction(this,
				AdminPaymentConfigurationMessages.get().EditPaymentConfiguration,
				AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS_EDIT);
		editPaymentConfigurationsAction.setToolTipText(AdminPaymentConfigurationMessages.get().EditPaymentConfiguration);
		editPaymentConfigurationsAction.setEnabled(false);

		activatePaymentConfigurationsAction = new ActivatePaymentConfigurationsAction(this,
				AdminPaymentConfigurationMessages.get().ActivatePaymentConfiguration,
				AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS_ACTIVATE);
		activatePaymentConfigurationsAction.setToolTipText(AdminPaymentConfigurationMessages.get().ActivatePaymentConfiguration);
		activatePaymentConfigurationsAction.setEnabled(false);

		disablePaymentConfigurationsAction = new DisablePaymentConfigurationsAction(this,
				AdminPaymentConfigurationMessages.get().DisablePaymentConfiguration,
				AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS_DISABLE);
		disablePaymentConfigurationsAction.setToolTipText(AdminPaymentConfigurationMessages.get().DisablePaymentConfiguration);
		disablePaymentConfigurationsAction.setEnabled(false);


		// Actions have to be wrapped in ActionContributionItems so that they
		// can be forced to display both text and image
		ActionContributionItem createPaymentConfigurationsActionContribItem = new ActionContributionItem(createPaymentConfigurationsAction);
		createPaymentConfigurationsActionContribItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		getToolbarManager().appendToGroup(paymentConfigurationsActionGroup.getGroupName(), createPaymentConfigurationsActionContribItem);

		ActionContributionItem editPaymentConfigurationsActionContribItem = new ActionContributionItem(editPaymentConfigurationsAction);
		editPaymentConfigurationsActionContribItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		getToolbarManager().appendToGroup(paymentConfigurationsActionGroup.getGroupName(), editPaymentConfigurationsActionContribItem);

		ActionContributionItem activatePaymentConfigurationsActionContribItem = new ActionContributionItem(activatePaymentConfigurationsAction);
		activatePaymentConfigurationsActionContribItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		getToolbarManager().appendToGroup(paymentConfigurationsActionGroup.getGroupName(), activatePaymentConfigurationsActionContribItem);

		ActionContributionItem disablePaymentConfigurationsActionContribItem = new ActionContributionItem(disablePaymentConfigurationsAction);
		disablePaymentConfigurationsActionContribItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		getToolbarManager().appendToGroup(paymentConfigurationsActionGroup.getGroupName(), disablePaymentConfigurationsActionContribItem);

		this.getViewer().addSelectionChangedListener(event -> {
			final ISelection selection = event.getSelection();

			if (selection instanceof StructuredSelection) {
				final IStructuredSelection strSelection = (IStructuredSelection) selection;

				editPaymentConfigurationsAction.setEnabled(false);
				activatePaymentConfigurationsAction.setEnabled(false);
				disablePaymentConfigurationsAction.setEnabled(false);
				if (strSelection.isEmpty()) {
					return;
				}

				PaymentConfigurationsListModel paymentProviderConfigModel = (PaymentConfigurationsListModel) strSelection.getFirstElement();
				editPaymentConfigurationsAction.setEnabled(true);
				if (paymentProviderConfigModel.getStatus().equals(PaymentProviderConfigurationStatus.DRAFT)) {
					activatePaymentConfigurationsAction.setEnabled(true);
					disablePaymentConfigurationsAction.setEnabled(true);
					return;
				}

				if (paymentProviderConfigModel.getStatus().equals(PaymentProviderConfigurationStatus.ACTIVE)) {
					disablePaymentConfigurationsAction.setEnabled(true);
					return;
				}
			}
		});
		addDoubleClickAction(editPaymentConfigurationsAction);
	}

	@Override
	protected void initializeTable(final IEpTableViewer viewerTable) {
		String[] columnNames = new String[]{
				AdminPaymentConfigurationMessages.get().PaymentConfigurationNameLabel,
				AdminPaymentConfigurationMessages.get().PaymentConfigurationProviderLabel,
				AdminPaymentConfigurationMessages.get().PaymentConfigurationMethodLabel,
				AdminPaymentConfigurationMessages.get().PaymentConfigurationStoresLabel,
				AdminPaymentConfigurationMessages.get().PaymentConfigurationStatusLabel
		};

		for (int i = 0; i < columnNames.length; i++) {
			viewerTable.addTableColumn(columnNames[i], COLUMN_WIDTHS[i]);
		}
	}

	@Override
	public void paymentConfigurationChanged(final ItemChangeEvent<PaymentConfigurationsListModel> event) {
		final PaymentConfigurationsListModel paymentProviderConfigModel = event.getItem();
		switch (event.getEventType()) {
			case ADD:
				getViewer().add(paymentProviderConfigModel);
				break;
			case CHANGE:
			case REMOVE:
				getViewer().update(paymentProviderConfigModel, null);
				getViewer().refresh(paymentProviderConfigModel, true);
				break;
			default:
				break;
		}
//		refresh selection to notify selection listener to update action buttons
		getViewer().setSelection(getViewer().getSelection());
	}

	/**
	 * Return a the table's selected payment configuration item.
	 *
	 * @return the selected payment configuration
	 */
	public PaymentConfigurationsListModel getSelectedPaymentConfiguration() {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		PaymentConfigurationsListModel paymentProviderConfigModel = null;
		if (!selection.isEmpty()) {
			paymentProviderConfigModel = (PaymentConfigurationsListModel) selection.getFirstElement();
		}
		return paymentProviderConfigModel;
	}

	@Override
	protected Object[] getViewInput() {
		final List<PaymentProviderConfigDTO> paymentProviderConfigDTOs = paymentProviderConfigManagementService.findAll();
		return paymentProviderConfigDTOs.parallelStream()
				.map(providerConfig -> {
					final Collection<String> storeNames = storePaymentProviderConfigService.findStoreNameByProviderConfig(providerConfig.getGuid());
					final PaymentProviderPluginDTO pluginDTO = paymentProviderPlugins.get(providerConfig.getPaymentProviderPluginBeanName());
					if (pluginDTO == null) {
						return new PaymentConfigurationsListModel(providerConfig, storeNames,
								MISSING_PAYMENT_PROVIDER_MARKER, MISSING_PAYMENT_PROVIDER_MARKER);
					} else {
						return new PaymentConfigurationsListModel(providerConfig, storeNames,
								pluginDTO.getPaymentVendorId(), pluginDTO.getPaymentMethodId());
					}
				})
				.toArray(Object[]::new);
	}

	@Override
	protected ITableLabelProvider getViewLabelProvider() {
		return new PaymentConfigurationViewLabelProvider();
	}

	@Override
	protected String getPartId() {
		return VIEW_ID;
	}

	@Override
	public void dispose() {
		PaymentConfigurationEventService.getInstance().unregisterPaymentConfigurationEventListener(this);
		super.dispose();
	}

	/**
	 * Label provider for Payment Configurations view.
	 */
	protected class PaymentConfigurationViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Get the image to put in each column.
		 *
		 * @param element     the row object
		 * @param columnIndex the column index
		 * @return the Image to put in the column
		 */
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/**
		 * Get the text to put in each column.
		 * @param element     the row object
		 * @param columnIndex the column index
		 * @return the String to put in the column
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			PaymentConfigurationsListModel paymentConfig = (PaymentConfigurationsListModel) element;

			PaymentProviderPluginDTO pluginDTO = paymentProviderPlugins.get(paymentConfig.getPaymentProviderId());
			switch (columnIndex) {
				case PaymentConfigurationsListView.INDEX_PAYMENT_CONFIGURATION_NAME:
					return paymentConfig.getConfigurationName();
				case PaymentConfigurationsListView.INDEX_PAYMENT_CONFIGURATION_PROVIDER:
					if (pluginDTO == null) {
						return MISSING_PAYMENT_PROVIDER_MARKER;
					}
					return pluginDTO.getPaymentVendorId();
				case PaymentConfigurationsListView.INDEX_PAYMENT_CONFIGURATION_METHOD:
					if (pluginDTO == null) {
						return MISSING_PAYMENT_PROVIDER_MARKER;
					}
					return pluginDTO.getPaymentMethodId();
				case PaymentConfigurationsListView.INDEX_PAYMENT_CONFIGURATION_STORES:
					return paymentConfig.getStoreNameString();
				case PaymentConfigurationsListView.INDEX_PAYMENT_CONFIGURATION_STATUS:
					return paymentConfig.getStatusString();
				default:
					return ""; //$NON-NLS-1$
			}
		}
	}
}
