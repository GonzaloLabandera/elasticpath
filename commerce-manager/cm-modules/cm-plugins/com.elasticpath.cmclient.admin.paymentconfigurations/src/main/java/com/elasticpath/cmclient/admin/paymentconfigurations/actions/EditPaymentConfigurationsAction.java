package com.elasticpath.cmclient.admin.paymentconfigurations.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.elasticpath.cmclient.admin.paymentconfigurations.dialogs.AbstractPaymentConfigurationsDialog;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListView;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Action to edit the payment configuration.
 */
public class EditPaymentConfigurationsAction extends Action {

	private static final Logger LOG = LogManager.getLogger(EditPaymentConfigurationsAction.class);

	private final PaymentConfigurationsListView listView;

	/**
	 * The constructor.
	 *
	 * @param listView PaymentListView this action is associated with
	 * @param text action text
	 * @param imageDescriptor action image
	 */
	public EditPaymentConfigurationsAction(final PaymentConfigurationsListView listView, final String text, final ImageDescriptor imageDescriptor) {
		super(text, imageDescriptor);
		this.listView = listView;
	}

	@Override
	public void run() {
		LOG.debug(getClass().getName() + " called."); //$NON-NLS-1$

		if (listView.getSelectedPaymentConfiguration() == null) {
			return;
		}
		AbstractPaymentConfigurationsDialog editDialog =
				AbstractPaymentConfigurationsDialog.buildEditDialog(listView.getSelectedPaymentConfiguration(), listView.getSite().getShell());

		if (editDialog.openDialog()) {
			final PaymentProviderConfigManagementService paymentProviderConfigManagementService = BeanLocator.getSingletonBean(
					ContextIdNames.PAYMENT_PROVIDER_CONFIG_MANAGEMENT_SERVICE, PaymentProviderConfigManagementService.class);
			paymentProviderConfigManagementService.saveOrUpdate(editDialog.getPaymentProviderConfigDTO());
			listView.refreshViewerInput();
		}
	}
}
