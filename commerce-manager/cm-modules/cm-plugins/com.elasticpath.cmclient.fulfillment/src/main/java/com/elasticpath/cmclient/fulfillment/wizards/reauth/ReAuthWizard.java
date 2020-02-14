/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.reauth;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.wizard.AbstractEpWizard;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatisticService;

/**
 * Reauthorization wizard.
 */
public class ReAuthWizard extends AbstractEpWizard<ReAuthorizationItem> {

	private static final String PAGE_CARD_INFO_REAUTH = "PageCardInfoReauth"; //$NON-NLS-1$
	private static final String PAGE_SUMMARY_REAUTH = "PageSummaryReauth"; //$NON-NLS-1$

	private final ReAuthInitPage initPage;
	private final ReAuthSummaryPage summaryPage;

	private final ReAuthorizationItem reAuthorizationItem;

	private boolean operationSuccessful;
	private Collection<PaymentStatistic> paymentTransactions;

	/**
	 * The constructor.
	 *
	 * @param reAuthorizationItem the item for which reauthorization is required
	 */
	public ReAuthWizard(final ReAuthorizationItem reAuthorizationItem) {
		super(FulfillmentMessages.get().CaptureWizard_Title, null, null);
		this.reAuthorizationItem = reAuthorizationItem;
		final DataBindingContext dataBindingContext = new DataBindingContext();
		initPage = new ReAuthInitPage(PAGE_CARD_INFO_REAUTH, dataBindingContext);
		summaryPage = new ReAuthSummaryPage(PAGE_SUMMARY_REAUTH, dataBindingContext);
	}

	/**
	 * Invokes reauthorization wizard if required for the specified order.
	 *
	 * @param order order for which reauthorization is requested.
	 * @return if reauthorization was successful on is not required.
	 */
	public static boolean reAuthIfRequired(final Order order) {
		final OrderService orderService = BeanLocator.getSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class);
		final Order originalOrder = orderService.get(order.getUidPk());

		boolean reAuthSuccessfulOrNotRequired = true;
		if (originalOrder.getTotal().compareTo(order.getTotal()) != 0) {
			final ReAuthorizationItem reAuthorizationItem = new ReAuthorizationItem();
			reAuthorizationItem.setOrder(order);
			reAuthorizationItem.setOriginalAuthorizedAmount(originalOrder.sumUpFutureShipmentAmounts());
			reAuthorizationItem.setNewAuthorizedAmount(order.sumUpFutureShipmentAmounts());

			final PaymentInstrumentManagementService paymentInstrumentManagementService =
					BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_INSTRUMENT_MANAGEMENT_SERVICE, PaymentInstrumentManagementService.class);
			reAuthorizationItem.setOriginalInstruments(paymentInstrumentManagementService.findOrderInstruments(order));

			final ReAuthWizard reAuthWizard = new ReAuthWizard(reAuthorizationItem);
			final EpWizardDialog dialog = new EpWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), reAuthWizard);
			dialog.addPageChangingListener(reAuthWizard);
			reAuthSuccessfulOrNotRequired = (dialog.open() == Window.OK);
		}
		return reAuthSuccessfulOrNotRequired;

	}

	@Override
	public void addPages() {
		addPage(initPage);
		addPage(summaryPage);
	}

	@Override
	public boolean performCancel() {
		if (getContainer().getCurrentPage() == initPage || !isOperationSuccessful()) {
			MessageDialog.openWarning(getShell(), FulfillmentMessages.get().CaptureWizard_Cancel_Title,
					FulfillmentMessages.get().CaptureWizard_Cancel_Message);
			return true;
		}
		return false;
	}

	/**
	 * Execute reauthorization for the whole order.
	 *
	 * @return true if there were no gateway errors. Stores such exceptions in ReAuthorizationItem.
	 */
	@SuppressWarnings({"PMD.PrematureDeclaration"})
	public boolean process() {
		operationSuccessful = false;
		final OrderPaymentApiService paymentService =
				BeanLocator.getSingletonBean(ContextIdNames.ORDER_PAYMENT_API_SERVICE, OrderPaymentApiService.class);
		final List<PaymentStatistic> oldTransactions = getPaymentStatisticService().findPayments(
				reAuthorizationItem.getOrder(), reAuthorizationItem.getOriginalInstruments(),
				orderPayment -> orderPayment.getTransactionType() == TransactionType.RESERVE
						|| orderPayment.getTransactionType() == TransactionType.MODIFY_RESERVE);
		try {
			paymentService.orderModified(reAuthorizationItem.getOrder(),
					reAuthorizationItem.getNewInstruments(),
					reAuthorizationItem.getOrder().getAdjustedOrderTotalMoney());

			operationSuccessful = true;
		} catch (final PaymentsException paymentsException) {
			reAuthorizationItem.setPaymentsException(paymentsException);
		} catch (final Exception exception) {
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ReAuthWizard_GatewayError_Title,
					NLS.bind(FulfillmentMessages.get().ReAuthWizard_GatewayError_Text, exception.getLocalizedMessage()));
			return false;
		}

		paymentTransactions = getPaymentStatisticService().findPayments(
				reAuthorizationItem.getOrder(), reAuthorizationItem.getNewInstruments(),
				orderPayment -> orderPayment.getTransactionType() == TransactionType.RESERVE
						|| orderPayment.getTransactionType() == TransactionType.MODIFY_RESERVE);
		paymentTransactions.removeAll(oldTransactions);
		paymentTransactions = getPaymentStatisticService().accumulateByInstrument(paymentTransactions);

		return true;
	}

	@Override
	protected ReAuthorizationItem getModel() {
		return reAuthorizationItem;
	}

	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == initPage) {
			return false;
		}
		return isOperationSuccessful() && super.canFinish();
	}

	@Override
	public void onUpdateButtons(final EpWizardDialog dialog) {
		if (dialog.getSelectedPage() == summaryPage) {
			dialog.getWizardButton(IDialogConstants.BACK_ID).setEnabled(!isOperationSuccessful());
			dialog.getWizardButton(IDialogConstants.CANCEL_ID).setEnabled(!isOperationSuccessful());
		} else {
			dialog.getWizardButton(IDialogConstants.CANCEL_ID).setEnabled(true);
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setText(FulfillmentMessages.get().ReAuthWizard_Authorize_Button);
		}
		dialog.getWizardButton(IDialogConstants.FINISH_ID).setText(FulfillmentMessages.get().ReAuthWizard_Done_Button);
	}

	/**
	 * @return true if reauthorization was successful.
	 */
	public boolean isOperationSuccessful() {
		return operationSuccessful;
	}

	/**
	 * @return transaction info
	 */
	public Collection<PaymentStatistic> getPaymentTransactions() {
		return paymentTransactions;
	}

	protected PaymentStatisticService getPaymentStatisticService() {
		return BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_STATISTIC_SERVICE, PaymentStatisticService.class);
	}

}
