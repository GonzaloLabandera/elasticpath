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
import org.eclipse.swt.widgets.Display;

import com.elasticpath.cmclient.core.CoreMessages;
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
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.IllegalReturnStateException;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderReturnOutOfDateException;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatisticService;

/**
 * The wizard for creating and editing an order return.
 */
public final class ReturnWizard extends AbstractEpWizard<OrderReturnItem> {

	/**
	 * ReturnWizard types.
	 */
	enum ReturnWizardType {
		/**
		 * Create type.
		 */
		CREATE_RETURN,
		/**
		 * Edit type.
		 */
		EDIT_RETURN,
		/**
		 * Complete type.
		 */
		COMPLETE_RETURN
	}

	private static final String PAGE_SUBJECT_RETURN = ReturnSubjectPage.class.getName();
	private static final String PAGE_METHOD_RETURN = ReturnMethodPage.class.getName();
	private static final String PAGE_SUMMARY_RETURN = ReturnSummaryPage.class.getName();

	private static final ReturnAndExchangeService ORDER_RETURN_SERVICE =
			BeanLocator.getSingletonBean(ContextIdNames.ORDER_RETURN_SERVICE, ReturnAndExchangeService.class);

	private final OrderReturnItem orderReturnItem;

	private final ReturnSubjectPage returnSubjectPage;
	private final ReturnMethodPage returnMethodPage;
	private final ReturnSummaryPage returnSummaryPage;

	private final ReturnWizardType returnWizardType;

	/**
	 * Full type ReturnWizard factory method.
	 *
	 * @param orderShipment The order shipment
	 * @return ReturnWizard
	 */
	public static ReturnWizard createReturnWizard(final OrderShipment orderShipment) {
		OrderReturn orderReturn = ORDER_RETURN_SERVICE.getOrderReturnPrototype(orderShipment, OrderReturnType.RETURN);
		return new ReturnWizard(FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_RETURN_CREATE),
				FulfillmentMessages.get().ReturnWizard_Create_Title, orderReturn, ReturnWizardType.CREATE_RETURN, orderShipment.isInclusiveTax());
	}

	/**
	 * Edit type ReturnWizard factory method.
	 *
	 * @param orderReturn The order return
	 * @return Return Wizard
	 */
	public static ReturnWizard editReturnWizard(final OrderReturn orderReturn) {
		ReturnWizard returnWizard = new ReturnWizard(FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_RETURN_EDIT),
				FulfillmentMessages.get().ReturnWizard_Edit_Title, orderReturn, ReturnWizardType.EDIT_RETURN, orderReturn.isInclusiveTax());
		orderReturn.updateOrderReturnableQuantity(orderReturn.getOrder(), getProductSkuLookup());
		return returnWizard;
	}

	/**
	 * Complete type ReturnWizard factory method.
	 *
	 * @param orderReturn The order return
	 * @return Return Wizard
	 */
	public static ReturnWizard completeReturnWizard(final OrderReturn orderReturn) {
		return new ReturnWizard(FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.IMAGE_RETURN_COMPLETE),
				FulfillmentMessages.get().ReturnWizard_Complete_Title, orderReturn, ReturnWizardType.COMPLETE_RETURN, orderReturn.isInclusiveTax());
	}

	private ReturnWizard(final Image wizardImage, final String windowTitle, final OrderReturn orderReturn,
						 final ReturnWizardType returnWizardType, final boolean isInclusiveTax) {

		super(windowTitle, windowTitle + FulfillmentMessages.SPACE + FulfillmentMessages.get().ReturnWizard_Step_Info, wizardImage);

		final Order order = orderReturn.getOrder();
		final List<PaymentInstrumentDTO> orderInstruments = getPaymentInstrumentManagementService().findOrderInstruments(order);
		this.orderReturnItem = new OrderReturnItem(orderReturn, orderInstruments);

		this.returnWizardType = returnWizardType;

		switch (returnWizardType) {
			case CREATE_RETURN:
				returnSubjectPage = new ReturnSubjectPage(PAGE_SUBJECT_RETURN,
						FulfillmentMessages.get().ReturnWizard_SubjectPage_Message,
						FulfillmentMessages.get().ReturnWizard_ItemsToBeReturned_Section,
						getModel().getOrderReturn(), isInclusiveTax);
				returnMethodPage = new ReturnMethodPage(PAGE_METHOD_RETURN);
				returnSummaryPage = new ReturnSummaryPage(PAGE_SUMMARY_RETURN);
				break;
			case EDIT_RETURN:
				returnSubjectPage = new ReturnSubjectPage(PAGE_SUBJECT_RETURN,
						FulfillmentMessages.EMPTY_STRING,
						FulfillmentMessages.get().ReturnWizard_ItemsToBeReturned_Section,
						getModel().getOrderReturn(), isInclusiveTax);
				returnMethodPage = null;
				returnSummaryPage = null;
				break;
			case COMPLETE_RETURN:
				returnSubjectPage = null;
				returnMethodPage = new ReturnMethodPage(PAGE_METHOD_RETURN);
				returnSummaryPage = new ReturnSummaryPage(PAGE_SUMMARY_RETURN);
				break;
			default:
				returnSubjectPage = null;
				returnMethodPage = null;
				returnSummaryPage = null;
				break;
		}
	}

	@Override
	public void createPageControls(final Composite pageContainer) {
		if (returnWizardType == ReturnWizardType.CREATE_RETURN) {
			setPagesTitleBlank(CoreMessages.EMPTY_STRING);
			final String pageTitle = FulfillmentMessages.get().ReturnWizard_Create_Title
					+ FulfillmentMessages.SPACE + FulfillmentMessages.get().ReturnWizard_Step_Info;
			final IWizardPage[] pages = getPages();
			final int length = pages.length - 1;
			for (int index = 0; index < length; index++) {
				pages[index].setTitle(NLS.bind(pageTitle, new Integer[]{index + 1, length}));
			}
			pages[length].setTitle(FulfillmentMessages.get().ReturnWizard_Create_Title);
		}
		super.createPageControls(pageContainer);
	}

	@Override
	public void addPages() {
		addWizardPage(returnSubjectPage);
		addWizardPage(returnMethodPage);
		addWizardPage(returnSummaryPage);
	}

	private void addWizardPage(final IWizardPage page) {
		if (page != null) {
			super.addPage(page);
		}
	}

	@Override
	public boolean performCancel() {
		orderReturnItem.getOrderReturn().recalculateOrderReturn();
		return true;
	}

	@Override
	public boolean performFinish() {
		IWizardPage page = getContainer().getCurrentPage();

		if (page instanceof ReturnSubjectPage && getReturnWizardType() == ReturnWizard.ReturnWizardType.EDIT_RETURN) {
			if (!((ReturnSubjectPage) page).validate()) {
				return false;
			}
			orderReturnItem.getOrder().setModifiedBy(getEventOriginator());
			try {
				ORDER_RETURN_SERVICE.editReturn(orderReturnItem.getOrderReturn());
			} catch (OrderReturnOutOfDateException | IllegalReturnStateException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						FulfillmentMessages.get().OrderReturn_ErrDlgCollisionTitle, FulfillmentMessages.get().OrderReturn_ErrDlgCollisionMessage);
				return false;
			}
		}

		return true;
	}

	/**
	 * Completes return.
	 *
	 * @return true if the operation terminated correctly
	 */
	public boolean completeReturn() {
		try {
			ReturnExchangeRefundTypeEnum type = ((ReturnMethodPage) getPage(PAGE_METHOD_RETURN)).getRefundControlResult();

			orderReturnItem.getOrder().setModifiedBy(getEventOriginator());

			collectStatistic(type, () -> ORDER_RETURN_SERVICE.completeReturn(orderReturnItem.getOrderReturn(), type));
		} catch (OrderReturnOutOfDateException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					FulfillmentMessages.get().OrderReturn_ErrDlgCollisionTitle, FulfillmentMessages.get().OrderReturn_ErrDlgCollisionMessage);
			return false;
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

	/**
	 * Creates return.
	 *
	 * @return true if the operation terminated correctly
	 */
	public boolean createReturn() {
		try {
			final ReturnExchangeRefundTypeEnum type = ((ReturnMethodPage) getPage(PAGE_METHOD_RETURN)).getRefundControlResult();

			OrderReturn orderReturn = orderReturnItem.getOrderReturn();
			orderReturn.setCreatedByCmUser(LoginManager.getCmUser());
			orderReturn.normalizeOrderReturn();

			collectStatistic(type, () -> ORDER_RETURN_SERVICE.createShipmentReturn(
					orderReturn, type, orderReturn.getOrderShipmentForReturn(), getEventOriginator()));
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

	private void collectStatistic(final ReturnExchangeRefundTypeEnum type, final Callable<OrderReturn> orderReturnCallable) throws Exception {
		if (type == ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL) {
			final PaymentStatisticService paymentStatisticService = getPaymentStatisticService();
			final List<PaymentStatistic> existingPayments = paymentStatisticService.findPayments(
					orderReturnItem.getOrder(), orderReturnItem.getOrderInstruments(),
					orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
			orderReturnItem.updateOrderReturn(orderReturnCallable.call());
			final List<PaymentStatistic> newPayments = paymentStatisticService.findPayments(
					orderReturnItem.getOrder(), orderReturnItem.getOrderInstruments(),
					orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT);
			newPayments.removeAll(existingPayments);
			final Collection<PaymentStatistic> paymentStatistics = paymentStatisticService.accumulateByInstrument(newPayments);
			orderReturnItem.setPaymentStatistics(paymentStatistics);
		} else {
			orderReturnItem.updateOrderReturn(orderReturnCallable.call());
			orderReturnItem.setPaymentStatistics(Collections.emptyList());
		}
	}

	/**
	 * Getter to get ReturnWizard type.
	 *
	 * @return The ReturnWizard type.
	 */
	public ReturnWizardType getReturnWizardType() {
		return returnWizardType;
	}

	@Override
	public OrderReturnItem getModel() {
		return orderReturnItem;
	}

	@Override
	public void onUpdateButtons(final EpWizardDialog dialog) {
		// Nothing to do if this is a single page edit return wizard
		if (getReturnWizardType() == ReturnWizard.ReturnWizardType.EDIT_RETURN) {
			return;
		}
		IWizardPage currentPage = dialog.getCurrentPage();
		if (returnSummaryPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.BACK_ID).setEnabled(false);
			dialog.getWizardButton(IDialogConstants.CANCEL_ID).setEnabled(false);
		} else if (returnSubjectPage != null && returnSubjectPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setEnabled(returnSubjectPage.validate());
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setText(FulfillmentMessages.get().ReturnWizard_Next_Button);
		} else if (returnMethodPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setText(FulfillmentMessages.get().ReturnWizard_Refund_Button);
		}
	}

	private EventOriginator getEventOriginator() {
		EventOriginatorHelper helper = BeanLocator.getSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
		return helper.getCmUserOriginator(LoginManager.getCmUser());
	}

	protected static ProductSkuLookup getProductSkuLookup() {
		return BeanLocator.getSingletonBean(ContextIdNames.PRODUCT_SKU_LOOKUP, ProductSkuLookup.class);
	}

	protected PaymentInstrumentManagementService getPaymentInstrumentManagementService() {
		return BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_INSTRUMENT_MANAGEMENT_SERVICE, PaymentInstrumentManagementService.class);
	}

	protected PaymentStatisticService getPaymentStatisticService() {
		return BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_STATISTIC_SERVICE, PaymentStatisticService.class);
	}

}
