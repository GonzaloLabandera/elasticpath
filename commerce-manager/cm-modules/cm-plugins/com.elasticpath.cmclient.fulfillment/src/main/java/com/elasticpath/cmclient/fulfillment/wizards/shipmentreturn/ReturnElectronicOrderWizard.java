/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.shipmentreturn;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.LoginManager;
import com.elasticpath.cmclient.core.wizard.AbstractEpWizard;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatisticService;

/**
 * The wizard for creating and editing an order return.
 */
public final class ReturnElectronicOrderWizard extends AbstractEpWizard<OrderReturnItem> {

	private static final String PAGE_SUBJECT_RETURN = ReturnElectronicOrderSubjectPage.class.getName();
	private static final String PAGE_METHOD_RETURN = ReturnElectronicOrderMethodPage.class.getName();
	private static final String PAGE_SUMMARY_RETURN = ReturnElectronicOrderSummaryPage.class.getName();

	private static final ReturnAndExchangeService ORDER_RETURN_SERVICE =
			BeanLocator.getSingletonBean(ContextIdNames.ORDER_RETURN_SERVICE, ReturnAndExchangeService.class);

	private final OrderReturnItem orderReturnItem;

	private ReturnElectronicOrderSubjectPage returnElectronicOrderSubjectPage;
	private ReturnElectronicOrderMethodPage returnElectronicOrderMethodPage;
	private ReturnElectronicOrderSummaryPage returnElectronicOrderSummaryPage;

	private final boolean isInclusiveTax;

	/**
	 * Full type ReturnWizard factory method.
	 *
	 * @param orderShipment The order shipment
	 * @return ReturnWizard
	 */
	public static ReturnElectronicOrderWizard createReturnWizard(final OrderShipment orderShipment) {
		final OrderReturn orderReturn = ORDER_RETURN_SERVICE.getOrderReturnPrototype(orderShipment, OrderReturnType.RETURN);
		orderReturn.setPhysicalReturn(false);

		return new ReturnElectronicOrderWizard(FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_RETURN_CREATE),
				FulfillmentMessages.get().ReturnWizard_Create_Title, orderReturn, orderShipment.isInclusiveTax());
	}

	private ReturnElectronicOrderWizard(final Image wizardImage, final String windowTitle, final OrderReturn orderReturn,
										final boolean isInclusiveTax) {

		super(windowTitle, null, wizardImage);

		final Order order = orderReturn.getOrder();
		final List<PaymentInstrumentDTO> orderInstruments = getPaymentInstrumentManagementService().findOrderInstruments(order);
		this.orderReturnItem = new OrderReturnItem(orderReturn, orderInstruments);
		this.isInclusiveTax = isInclusiveTax;

		returnElectronicOrderSubjectPage = null;
		returnElectronicOrderMethodPage = null;
		returnElectronicOrderSummaryPage = null;

		createSubjectPage(FulfillmentMessages.get().ReturnWizard_SubjectPage_Message);
		createMethodPage(FulfillmentMessages.get().ReturnWizard_MethodPage_Message);
		createSummaryPage();
	}

	@Override
	public void createPageControls(final Composite pageContainer) {
		final String pagesTitleBlank = FulfillmentMessages.get().ReturnWizard_Create_Title
				+ FulfillmentMessages.SPACE + FulfillmentMessages.get().ReturnWizard_Step_Info;
		final IWizardPage[] pages = getPages();
		final int length = pages.length - 1;
		for (int index = 0; index < length; index++) {
			pages[index].setTitle(NLS.bind(pagesTitleBlank, new Integer[]{index + 1, length}));
		}
		pages[length].setTitle(FulfillmentMessages.get().ReturnWizard_Create_Title);
		super.createPageControls(pageContainer);
	}

	private void createSubjectPage(final String message) {
		returnElectronicOrderSubjectPage = new ReturnElectronicOrderSubjectPage(PAGE_SUBJECT_RETURN, message,
				FulfillmentMessages.get().ReturnWizard_ItemsToBeReturned_Section, getModel().getOrderReturn(), isInclusiveTax);
	}

	private void createMethodPage(final String message) {
		returnElectronicOrderMethodPage = new ReturnElectronicOrderMethodPage(PAGE_METHOD_RETURN, message);
	}

	private void createSummaryPage() {
		returnElectronicOrderSummaryPage = new ReturnElectronicOrderSummaryPage(PAGE_SUMMARY_RETURN);
	}

	/**
	 * Add all needed pages. Note, that not all pages will be actually added, only those that was really created.
	 */
	@Override
	public void addPages() {
		addWizardPage(returnElectronicOrderSubjectPage);
		addWizardPage(returnElectronicOrderMethodPage);
		addWizardPage(returnElectronicOrderSummaryPage);
	}

	private void addWizardPage(final IWizardPage page) {
		if (page != null) {
			super.addPage(page);
		}
	}

	/**
	 * Restore qty to zero to release order returns in case of cancelation.
	 *
	 * @return always returns true, cause cancelation is not needed to be executed.
	 */
	@Override
	public boolean performCancel() {
		orderReturnItem.getOrderReturn().recalculateOrderReturn();
		return true;
	}

	/**
	 * Creates return.
	 *
	 * @return true if the operation terminated correctly
	 */
	public boolean createReturn() {
		try {
			ReturnExchangeRefundTypeEnum type = ((ReturnElectronicOrderMethodPage) getPage(PAGE_METHOD_RETURN)).getRefundControlResult();

			OrderReturn orderReturn = orderReturnItem.getOrderReturn();
			orderReturn.setCreatedByCmUser(LoginManager.getCmUser()); // order return should be the prototype
			orderReturn.normalizeOrderReturn();

			collectStatistic(type, () -> ORDER_RETURN_SERVICE.createShipmentReturn(orderReturn, type,
					orderReturn.getOrderShipmentForReturn(), getEventOriginator()));
		} catch (IncorrectRefundAmountException e) {
			MessageDialog.openError(getShell(), FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Title,
					FulfillmentMessages.get().RefundWizard_IncorrectRefundAmount_Message);
			return false;
		} catch (Exception exception) {
			MessageDialog.openError(getShell(), FulfillmentMessages.get().ReturnWizard_ProceedError_Title,
					NLS.bind(FulfillmentMessages.get().ReturnWizard_ProceedError_Msg, exception.getLocalizedMessage()));
			return false;
		}

		return true;
	}

	private void collectStatistic(final ReturnExchangeRefundTypeEnum type, final Callable<OrderReturn> originalCallable) throws Exception {
		if (type == ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL) {
			final PaymentStatisticService paymentStatisticService = getPaymentStatisticService();
			final List<PaymentStatistic> existingPayments = paymentStatisticService.findPayments(
					orderReturnItem.getOrder(), orderReturnItem.getOrderInstruments(),
					orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
			orderReturnItem.updateOrderReturn(originalCallable.call());
			final List<PaymentStatistic> newPayments = paymentStatisticService.findPayments(
					orderReturnItem.getOrder(), orderReturnItem.getOrderInstruments(),
					orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
			newPayments.removeAll(existingPayments);
			final Collection<PaymentStatistic> paymentStatistics = paymentStatisticService.accumulateByInstrument(newPayments);
			orderReturnItem.setPaymentStatistics(paymentStatistics);
		} else {
			orderReturnItem.updateOrderReturn(originalCallable.call());
			orderReturnItem.setPaymentStatistics(Collections.emptyList());
		}
	}

	/**
	 * Getter to get order return.
	 *
	 * @return The order return
	 */
	@Override
	public OrderReturnItem getModel() {
		return orderReturnItem;
	}

	/**
	 * Back and Cancel buttons are disabled in the summary page.
	 *
	 * @param dialog parent EpWizardDialog.
	 */
	@Override
	public void onUpdateButtons(final EpWizardDialog dialog) {
		IWizardPage currentPage = dialog.getCurrentPage();
		if (returnElectronicOrderSummaryPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.BACK_ID).setEnabled(false);
			dialog.getWizardButton(IDialogConstants.CANCEL_ID).setEnabled(false);
		} else if (returnElectronicOrderSubjectPage != null && returnElectronicOrderSubjectPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setEnabled(returnElectronicOrderSubjectPage.validate());
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setText(FulfillmentMessages.get().ReturnWizard_Next_Button);
		} else if (returnElectronicOrderMethodPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setText(FulfillmentMessages.get().ReturnWizard_Refund_Button);
		}
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
