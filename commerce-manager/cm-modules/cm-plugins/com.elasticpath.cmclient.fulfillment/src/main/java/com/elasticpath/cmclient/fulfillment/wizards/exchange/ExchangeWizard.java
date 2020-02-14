/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.LoginManager;
import com.elasticpath.cmclient.core.wizard.AbstractEpWizard;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.wizards.shipmentreturn.ReturnSubjectPage;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.InsufficientInventoryException;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderReturnOutOfDateException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatisticService;

/**
 * The wizard for creating and editing Exchange.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class ExchangeWizard extends AbstractEpWizard<ExchangeModel> {

	/**
	 * The logger.
	 */
	private static final Logger LOG = Logger.getLogger(ExchangeWizard.class);

	private static final String ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE = "Error occurred while processing an exchange."; //$NON-NLS-1$

	/**
	 * ExchangeWizardType defines type of the wizard for Exchange.
	 */
	enum ExchangeWizardType {
		/**
		 * Create exchange wizard.
		 */
		CREATE_EXCHANGE,
		/**
		 * Complete exchange wizard.
		 */
		COMPLETE_EXCHANGE
	}

	private static final String PAGE_SUBJECT_RETURN = "PageSubjectReturn"; //$NON-NLS-1$
	private static final String PAGE_ORDER_EXCHANGE = "PageOrderExchange"; //$NON-NLS-1$
	private static final String PAGE_PAYMENT_EXCHANGE = "PagePaymentExchange"; //$NON-NLS-1$

	private final ExchangeModel exchangeModel;

	private final ReturnSubjectPage returnSubjectPage;
	private final ExchangeOrderItemsPage exchangeOrderItemsPage;
	private final ExchangePaymentPage paymentPage;

	private static ReturnAndExchangeService orderReturnService =
			BeanLocator.getSingletonBean(ContextIdNames.ORDER_RETURN_SERVICE, ReturnAndExchangeService.class);
	private static OrderService orderService =
			BeanLocator.getSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class);

	private final ExchangeWizardType exchangeWizardType;

	/**
	 * Returns wizard type for exchange order.
	 *
	 * @return exchange order wizard type.
	 */
	public ExchangeWizardType getExchangeWizardType() {
		return exchangeWizardType;
	}

	/**
	 * Creates instance of exchange wizard. (Factory Method)
	 *
	 * @param orderShipment the order shipment
	 * @return ExchangeWizard
	 */
	public static ExchangeWizard createExchangeWizard(final OrderShipment orderShipment) {
		OrderReturn orderReturn = orderReturnService.getOrderReturnPrototype(orderShipment, OrderReturnType.EXCHANGE);
		return new ExchangeWizard(FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_EXCHANGE_CREATE),
				FulfillmentMessages.get().ExchangeWizard_Create_Title, orderReturn,
				ExchangeWizardType.CREATE_EXCHANGE, orderShipment.isInclusiveTax());
	}

	/**
	 * Creates instance of complete exchange wizard. (Factory Method)
	 *
	 * @param orderReturn order return
	 * @return ExchangeWizard
	 */
	public static ExchangeWizard completeExchangeWizard(final OrderReturn orderReturn) {
		return new ExchangeWizard(FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_EXCHANGE_COMPLETE),
				FulfillmentMessages.get().ExchangeWizard_Complete_Title, orderReturn,
				ExchangeWizardType.COMPLETE_EXCHANGE, orderReturn.isInclusiveTax());
	}

	private ExchangeWizard(final Image wizardImage, final String windowTitle, final OrderReturn orderReturn,
						   final ExchangeWizardType exchangeWizardType, final boolean isInclusiveTax) {
		super(windowTitle, windowTitle + FulfillmentMessages.SPACE + FulfillmentMessages.get().ReturnWizard_Step_Info, wizardImage);

		final List<PaymentInstrumentDTO> orderInstruments = getPaymentInstrumentManagementService().findOrderInstruments(orderReturn.getOrder());
		this.exchangeModel = new ExchangeModel(orderReturn, orderInstruments);
		this.exchangeWizardType = exchangeWizardType;


		switch (exchangeWizardType) {
			case CREATE_EXCHANGE:
				returnSubjectPage = new ReturnSubjectPage(PAGE_SUBJECT_RETURN,
						FulfillmentMessages.get().ExchangeWizard_ExchangeItemsExchangePage_Message,
						FulfillmentMessages.get().ExchangeWizard_ItemsToBeExchanged_Section, orderReturn, isInclusiveTax);
				exchangeOrderItemsPage = new ExchangeOrderItemsPage(PAGE_ORDER_EXCHANGE);
				paymentPage = new ExchangePaymentPage(PAGE_PAYMENT_EXCHANGE);
				break;
			case COMPLETE_EXCHANGE:
				returnSubjectPage = null;
				exchangeOrderItemsPage = null;
				paymentPage = new ExchangePaymentPage(PAGE_PAYMENT_EXCHANGE);
				break;
			default:
				returnSubjectPage = null;
				exchangeOrderItemsPage = null;
				paymentPage = null;
				break;
		}

	}

	/**
	 * Completes exchange.
	 *
	 * @return true if the operation terminated correctly
	 */
	public boolean completeExchange() {
		try {
			OrderReturn orderReturn = exchangeModel.getOrderReturn();
			orderReturn.getOrder().setModifiedBy(getEventOriginator());

			final ReturnExchangeRefundTypeEnum refundType = getModel().getRefundType();
			collectRefundStatistic(refundType, () -> orderReturnService.completeExchange(orderReturn, refundType));
		} catch (OrderReturnOutOfDateException e) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, e); //$NON-NLS-1$
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					FulfillmentMessages.get().OrderReturn_ErrDlgCollisionTitle, FulfillmentMessages.get().OrderReturn_ErrDlgCollisionMessage);
			return false;
		} catch (PaymentsException paymentsException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, paymentsException); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ErrorProcessingPayment,
					FulfillmentMessages.get().getErrorMessage(paymentsException));
			return false;
		} catch (InsufficientInventoryException insufficientInventoryException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, insufficientInventoryException); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ExchangeWizard_InsufficientInventory_Title,
					FulfillmentMessages.get().ExchangeWizard_InsufficientInventory_Message);
			return false;
		} catch (IncorrectRefundAmountException amountException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, amountException); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Title,
					FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Message);
			return false;
		} catch (Exception exception) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, exception); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ReturnWizard_ProceedError_Title,
					NLS.bind(FulfillmentMessages.get().ReturnWizard_ProceedError_Msg, exception.getLocalizedMessage()));
			return false;
		}
		return true;
	}

	/**
	 * Creates exchange.
	 *
	 * @return true if the operation terminated correctly
	 */
	public boolean createExchange() {
		try {
			OrderReturn orderReturn = exchangeModel.getOrderReturn();
			orderReturn.getOrder().setModifiedBy(getEventOriginator());
			orderReturn.setCreatedByCmUser(LoginManager.getCmUser());

			final List<PaymentInstrumentDTO> instruments = getSelectedPaymentInstrument();
			exchangeModel.setReservationInstruments(instruments);

			final ReturnExchangeRefundTypeEnum refundType = getModel().getRefundType();
			collectFullStatistic(refundType, () -> orderReturnService.createExchange(orderReturn, refundType, instruments));
		} catch (PaymentsException paymentsException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, paymentsException); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ErrorProcessingPayment,
					FulfillmentMessages.get().getErrorMessage(paymentsException));
			return false;
		} catch (InsufficientInventoryException insufficientInventoryException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, insufficientInventoryException); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ExchangeWizard_InsufficientInventory_Title,
					FulfillmentMessages.get().ExchangeWizard_InsufficientInventory_Message);
			return false;
		} catch (IncorrectRefundAmountException amountException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, amountException); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Title,
					FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Message);
			return false;
		} catch (Exception exception) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, exception); //$NON-NLS-1$
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ReturnWizard_ProceedError_Title,
					NLS.bind(FulfillmentMessages.get().ReturnWizard_ProceedError_Msg, exception.getLocalizedMessage()));
			return false;
		}

		return true;
	}


	private void collectFullStatistic(final ReturnExchangeRefundTypeEnum refundType,
									  final Callable<OrderReturn> orderReturnCallable) throws Exception {
		collectRefundStatistic(refundType, orderReturnCallable);
		Collection<PaymentStatistic> reservationTransactions = getPaymentStatisticService().findPayments(
				exchangeModel.getOrderReturn().getExchangeOrder(), exchangeModel.getReservationInstruments(),
				orderPayment -> orderPayment.getTransactionType() == TransactionType.RESERVE);
		reservationTransactions = getPaymentStatisticService().accumulateByInstrument(reservationTransactions);
		exchangeModel.setReservationTransactions(reservationTransactions);
	}

	private void collectRefundStatistic(final ReturnExchangeRefundTypeEnum refundType,
										final Callable<OrderReturn> orderReturnCallable) throws Exception {
		if (refundType == ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL) {
			final PaymentStatisticService paymentStatisticService = getPaymentStatisticService();
			final List<PaymentStatistic> existingRefunds = paymentStatisticService.findPayments(
					exchangeModel.getOrder(), exchangeModel.getOrderInstruments(),
					orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
			exchangeModel.updateOrderReturn(orderReturnCallable.call());
			Collection<PaymentStatistic> refundTransactions = paymentStatisticService.findPayments(
					exchangeModel.getOrder(), exchangeModel.getOrderInstruments(),
					orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
			refundTransactions.removeAll(existingRefunds);
			refundTransactions = paymentStatisticService.accumulateByInstrument(refundTransactions);
			exchangeModel.setRefundTransactions(refundTransactions);
			exchangeModel.setExchangeOrder(orderService.get(exchangeModel.getOrderReturn().getExchangeOrder().getUidPk()));
		} else {
			exchangeModel.updateOrderReturn(orderReturnCallable.call());
			exchangeModel.setRefundTransactions(Collections.emptyList());
		}
	}

	@Override
	public void addPages() {
		addWizardPage(returnSubjectPage);
		addWizardPage(exchangeOrderItemsPage);
		addWizardPage(paymentPage);
	}

	private void addWizardPage(final IWizardPage page) {
		if (page != null) {
			super.addPage(page);
		}
	}

	private List<PaymentInstrumentDTO> getSelectedPaymentInstrument() {
		return Stream.concat(((ExchangePaymentPage) getPage(PAGE_PAYMENT_EXCHANGE)).getSelectedOriginalPaymentInstruments().stream(),
				((ExchangePaymentPage) getPage(PAGE_PAYMENT_EXCHANGE)).getSelectedAlternatePaymentInstruments().stream())
				.collect(Collectors.toList());
	}

	@Override
	public ExchangeModel getModel() {
		return exchangeModel;
	}

	@Override
	public void onUpdateButtons(final EpWizardDialog dialog) {
		switch (exchangeWizardType) {
			case CREATE_EXCHANGE:
				dialog.getWizardButton(IDialogConstants.FINISH_ID).setText(FulfillmentMessages.get().ExchangeWizard_Authorize_Button);
				break;
			case COMPLETE_EXCHANGE:
				dialog.getWizardButton(IDialogConstants.FINISH_ID).setText(FulfillmentMessages.get().ExchangeWizard_Refund_Button);
				break;
			default:
				break;
		}
	}

	@Override
	public boolean performFinish() {
		if (super.performFinish()) {
			final ExchangePaymentPage exchangePaymentPage = (ExchangePaymentPage) getPage(PAGE_PAYMENT_EXCHANGE);

			if (exchangePaymentPage.getSelectedAlternatePaymentInstruments().isEmpty()
					&& exchangePaymentPage.isAlternateSelection() && exchangeWizardType == ExchangeWizardType.CREATE_EXCHANGE) {
				return false;
			}
			if (exchangePaymentPage.isPaymentSourceOptionsValid() || exchangePaymentPage.isRefundMethodValid()) {
				getModel().setRefundType(exchangePaymentPage.getSelectionResult());
				getModel().setExchangeWizardType(exchangeWizardType);
				if (exchangeWizardType == ExchangeWizardType.COMPLETE_EXCHANGE) {
					return completeExchange();
				} else if (exchangeWizardType == ExchangeWizardType.CREATE_EXCHANGE) {
					createExchange();
				}
			}
			return true;
		}
		return false;
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

}
