/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.refund;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.LoginManager;
import com.elasticpath.cmclient.core.wizard.AbstractEpWizard;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.order.OrderEventCmHelper;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatisticService;

/**
 * Create refund wizard.
 */
public final class RefundWizard extends AbstractEpWizard<RefundItem> {

	private static final String PAGE_CARD_INFO_REFUND = "PageCardInfoRefund"; //$NON-NLS-1$
	private static final String PAGE_SUMMARY_REFUND = "PageSummaryRefund"; //$NON-NLS-1$
	private static final String ERROR_OCCURRED_WHILE_PROCESSING_A_REFUND = "Error occurred while processing a refund.";

	private final RefundItem refundItem;

	private final RefundCardInfoPage cardInfoPage;
	private final RefundSummaryPage summaryPage;

	private static final Logger LOG = LogManager.getLogger(RefundWizard.class);

	/**
	 * The constructor.
	 *
	 * @param order order for which the refund should be done.
	 */
	public RefundWizard(final Order order) {
		super(FulfillmentMessages.get().RefundWizard_Title, null,
				FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_REFUND_CREATE));
		this.refundItem = new RefundItem();
		refundItem.setOrder(order);
		refundItem.setOriginalInstruments(getPaymentInstrumentManagementService().findOrderInstruments(order));

		final Money refundableAmount = getOrderPaymentApiService().getOrderPaymentAmounts(order).getAmountRefundable();

		cardInfoPage = new RefundCardInfoPage(PAGE_CARD_INFO_REFUND, refundableAmount);
		summaryPage = new RefundSummaryPage(PAGE_SUMMARY_REFUND);
	}

	@Override
	public void addPages() {
		addPage(cardInfoPage);
		addPage(summaryPage);
	}

	@Override
	public void onUpdateButtons(final EpWizardDialog dialog) {
		if (dialog.getSelectedPage() == summaryPage) {
			dialog.getWizardButton(IDialogConstants.BACK_ID).setEnabled(false);
			dialog.getWizardButton(IDialogConstants.CANCEL_ID).setEnabled(false);
		} else {
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setText(FulfillmentMessages.get().RefundWizard_Refund_Button);
		}
		dialog.getWizardButton(IDialogConstants.FINISH_ID).setText(FulfillmentMessages.get().RefundWizard_Done_Button);
	}

	/**
	 * Process refund.
	 *
	 * @param selectedPaymentInstruments payment sources to be used, empty list to refund manually
	 * @param amount                     refund amount
	 * @param refundNote                 refund note
	 * @return if refund was successful
	 */
	public boolean process(final List<PaymentInstrumentDTO> selectedPaymentInstruments, final BigDecimal amount, final String refundNote) {
		final Order order = getModel().getOrder();

		getModel().setRefundedAmount(Money.valueOf(amount, order.getCurrency()));

		final OrderService orderService = BeanLocator.getSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class);
		try {
			if (selectedPaymentInstruments.isEmpty()) {
				orderService.manualRefundOrderPayment(order, getModel().getRefundedAmount(), getEventOriginator());
				refundItem.setManual(true);
				refundItem.setPaymentTransactions(Collections.emptyList());
			} else {
				final List<PaymentStatistic> oldTransactions = getPaymentStatisticService().findPayments(
						order, getModel().getOriginalInstruments(),
						orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);

				orderService.refundOrderPayment(order, selectedPaymentInstruments, getModel().getRefundedAmount(), getEventOriginator());

				refundItem.setManual(false);
				final List<PaymentStatistic> newTransactions = getPaymentStatisticService().findPayments(
						order, getModel().getOriginalInstruments(),
						orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
				newTransactions.removeAll(oldTransactions);
				refundItem.setPaymentTransactions(getPaymentStatisticService().accumulateByInstrument(newTransactions));
			}

			// Logs refund note if necessary
			if (refundNote != null && refundNote.length() > 0) {
				OrderEventCmHelper.initForOrderAuditing(order);
				OrderEventCmHelper.getOrderEventHelper().logOrderNote(order, refundNote);
				getModel().setOrder(orderService.update(order));
			}
		} catch (final PaymentsException paymentsException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_A_REFUND, paymentsException); //$NON-NLS-1$
			if (paymentsException.getMessageId() == PaymentsExceptionMessageId.PAYMENT_INSUFFICIENT_FUNDS) {
				MessageDialog.openError(getShell(),
						FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Title,
						FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Message);
			} else {
				MessageDialog.openError(getShell(),
						FulfillmentMessages.get().RefundWizard_PaymentProceedError_Title,
						NLS.bind(FulfillmentMessages.get().RefundWizard_PaymentProceedError_Message,
								FulfillmentMessages.get().getErrorMessage(paymentsException)
						));
			}
			return false;
		} catch (final IncorrectRefundAmountException amountException) {
			MessageDialog.openError(getShell(),
					FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Title,
					amountException.getLocalizedMessage());
			return false;
		} catch (final Exception exception) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_A_REFUND, exception); //$NON-NLS-1$
			MessageDialog.openError(getShell(),
					FulfillmentMessages.get().RefundWizard_PaymentProceedError_Title,
					exception.getLocalizedMessage());
			return false;
		}
		return true;
	}

	@Override
	protected RefundItem getModel() {
		return refundItem;
	}

	private EventOriginator getEventOriginator() {
		EventOriginatorHelper helper = BeanLocator.getSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
		return helper.getCmUserOriginator(LoginManager.getCmUser());
	}

	protected PaymentInstrumentManagementService getPaymentInstrumentManagementService() {
		return BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_INSTRUMENT_MANAGEMENT_SERVICE, PaymentInstrumentManagementService.class);
	}

	protected PaymentStatisticService getPaymentStatisticService() {
		return BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_STATISTIC_SERVICE, PaymentStatisticService.class);
	}

	protected OrderPaymentApiService getOrderPaymentApiService() {
		return BeanLocator.getSingletonBean(ContextIdNames.ORDER_PAYMENT_API_SERVICE, OrderPaymentApiService.class);
	}

}