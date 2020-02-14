/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import static com.elasticpath.service.order.ReturnExchangeRefundTypeEnum.MANUAL_REFUND;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnStatus;
import com.elasticpath.money.MoneyFormatter;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;

/**
 * Exchange summary dialog.
 */
public class ExchangeSummaryDialog extends AbstractEpDialog {

	private static final int COLUMN_WIDTH_PAYMENT_SOURCE = 250;
	private static final int COLUMN_INDEX_PAYMENT_SOURCE = 0;

	private static final int COLUMN_WIDTH_AMOUNT = 180;
	private static final int COLUMN_INDEX_AMOUNT = 1;

	private static final int COLUMN_WIDTH_STATUS = 100;
	private static final int COLUMN_INDEX_STATUS = 2;

	private static final int RESULT_SECTION_WIDTH_MARGIN = 0;
	private static final int RESULT_SECTION_HEIGHT_MARGIN = 20;

	private final String windowTitle;
	private final ExchangeModel exchangeModel;

	/**
	 * Constructor.
	 *
	 * @param windowTitle   title of dialog.
	 * @param exchangeModel exchange model.
	 */
	public ExchangeSummaryDialog(final String windowTitle, final ExchangeModel exchangeModel) {
		super((Shell) null, 1, false);

		this.exchangeModel = exchangeModel;
		this.windowTitle = windowTitle;
	}

	@Override
	protected String getPluginId() {
		return FulfillmentPlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return exchangeModel;
	}

	@Override
	protected void populateControls() {
		// nothing to populate here.
	}

	@Override
	protected void bindControls() {
		// nothing to bind here.
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite parent) {
		IEpLayoutComposite composite = parent.addTableWrapLayoutComposite(1, true,
				parent.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true));

		final IManagedForm form = EpControlFactory.getInstance().createManagedForm(composite.getSwtComposite());
		final Composite formBody = form.getForm().getBody();
		formBody.setLayout(new TableWrapLayout());
		form.getForm().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

		final OrderReturn orderReturn = exchangeModel.getOrderReturn();
		final String returnActionLabel;
		if (exchangeModel.getExchangeWizardType() == ExchangeWizard.ExchangeWizardType.COMPLETE_EXCHANGE) {
			returnActionLabel = NLS.bind(FulfillmentMessages.get().ExchangeWizard_Exchange_Completed_Label, orderReturn.getRmaCode());
		} else if (Objects.nonNull(orderReturn.getRmaCode())) {
			returnActionLabel = NLS.bind(FulfillmentMessages.get().ExchangeWizard_NewExchangeCreated_Label, orderReturn.getRmaCode());
		} else {
			returnActionLabel = FulfillmentMessages.get().ExchangeWizard_CancelExchangeCreated_Label;
		}

		createResultSection(form, FulfillmentMessages.get().ExchangeWizard_RefundResultOriginalOrder_Label,
				returnActionLabel, exchangeModel.getRefundTransactions());

		final String reserveActionLabel;
		final ReturnExchangeRefundTypeEnum refundType = exchangeModel.getRefundType();
		if (refundType == MANUAL_REFUND && BigDecimal.ZERO.compareTo(orderReturn.getReturnTotal()) < 0) {
			final String amount = FulfillmentMessages.get().formatMoneyAsString(orderReturn.getReturnTotalMoney(),
					orderReturn.getOrder().getLocale());
			reserveActionLabel = NLS.bind(FulfillmentMessages.get().PaymentSummaryControl_Manual_Refund_Payment, amount);
			createResultSection(form, FulfillmentMessages.get().ExchangeWizard_RefundResultExchangeOrder_Label,
					reserveActionLabel, Collections.emptyList());
		} else if (Objects.nonNull(exchangeModel.getExchangeOrder()) && orderReturn.getReturnStatus().equals(OrderReturnStatus.CANCELLED)) {
			reserveActionLabel = NLS.bind(FulfillmentMessages.get().ExchangeWizard_NewOrderCreatedCancelled_Label,
					exchangeModel.getExchangeOrder().getOrderNumber());
			createResultSection(form, FulfillmentMessages.get().ExchangeWizard_RefundResultExchangeOrder_Label,
					reserveActionLabel, Collections.emptyList());
		} else if (Objects.nonNull(exchangeModel.getReservationTransactions()) && !exchangeModel.getReservationTransactions().isEmpty()) {
			reserveActionLabel = NLS.bind(FulfillmentMessages.get().ExchangeWizard_NewOrderCreated_Label,
					orderReturn.getExchangeOrder().getOrderNumber());
			createResultSection(form, FulfillmentMessages.get().ExchangeWizard_RefundResultExchangeOrder_Label,
					reserveActionLabel, exchangeModel.getReservationTransactions());
		}
	}

	@Override
	protected String getTitle() {
		return FulfillmentMessages.get().ExchangeWizard_Confirmation_Section;
	}

	@Override
	protected String getInitialMessage() {
		return null;
	}

	@Override
	protected String getWindowTitle() {
		return windowTitle;
	}

	@Override
	protected Image getWindowImage() {
		return null;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.BACK_ID, FulfillmentMessages.get().ExchangeWizard_Back_Button, false).setEnabled(false);
		createButton(parent, IDialogConstants.NEXT_ID, FulfillmentMessages.get().ExchangeWizard_Next_Button, false).setEnabled(false);
		createButton(parent, IDialogConstants.OK_ID, FulfillmentMessages.get().ExchangeWizard_Done_Button, true);
		createButton(parent, IDialogConstants.CANCEL_ID, CoreMessages.get().AbstractEpDialog_ButtonCancel, false);
	}

	private void createResultSection(final IManagedForm managedForm, final String title,
									 final String actionText, final Collection<PaymentStatistic> paymentStatistics) {
		final Composite formComposite = managedForm.getForm().getBody();
		final Composite resultSectionComposite = new Composite(formComposite, SWT.NONE);
		final GridLayout resultSectionLayout = new GridLayout();
		resultSectionLayout.marginWidth = RESULT_SECTION_WIDTH_MARGIN;
		resultSectionLayout.marginHeight = RESULT_SECTION_HEIGHT_MARGIN;
		resultSectionComposite.setLayout(resultSectionLayout);

		final Label sectionName = managedForm.getToolkit().createLabel(resultSectionComposite, title);
		sectionName.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

		managedForm.getToolkit().createLabel(resultSectionComposite, actionText);

		if (Objects.nonNull(paymentStatistics) && !paymentStatistics.isEmpty()) {
			final Table table = managedForm.getToolkit().createTable(resultSectionComposite, SWT.BORDER | SWT.FULL_SELECTION);
			table.setHeaderVisible(true);

			final TableViewer tableViewer = new TableViewer(table);
			addColumnToTableViewer(tableViewer, FulfillmentMessages.get().RefundWizard_PaymentSource_ColumnTitle, COLUMN_WIDTH_PAYMENT_SOURCE);
			if (sectionName.getText().equals(FulfillmentMessages.get().ExchangeWizard_RefundResultOriginalOrder_Label)) {
				addColumnToTableViewer(tableViewer, FulfillmentMessages.get().RefundWizard_Refund_ColumnTitle, COLUMN_WIDTH_AMOUNT);
			} else if (sectionName.getText().equals(FulfillmentMessages.get().ExchangeWizard_RefundResultExchangeOrder_Label)) {
				addColumnToTableViewer(tableViewer, FulfillmentMessages.get().RefundWizard_Authorization_ColumnTitle, COLUMN_WIDTH_AMOUNT);
			}
			addColumnToTableViewer(tableViewer, FulfillmentMessages.get().RefundWizard_Status_ColumnTitle, COLUMN_WIDTH_STATUS);

			tableViewer.setLabelProvider(new ResultsTableLabelProvider());
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setInput(paymentStatistics);
		}
	}

	private void addColumnToTableViewer(final TableViewer tableViewer, final String title, final int width) {
		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.getColumn().setText(title);
		tableViewerColumn.getColumn().setWidth(width);
	}

	/**
	 * Label provider for results table.
	 */
	class ResultsTableLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final PaymentStatistic paymentStatistic = (PaymentStatistic) element;

			switch (columnIndex) {
				case COLUMN_INDEX_PAYMENT_SOURCE:
					return paymentStatistic.getInstrument().getName();
				case COLUMN_INDEX_AMOUNT:
					return paymentStatistic.getFormattedAmount(BeanLocator.getSingletonBean(ContextIdNames.MONEY_FORMATTER, MoneyFormatter.class));
				case COLUMN_INDEX_STATUS:
					return paymentStatistic.isSuccessful()
							? FulfillmentMessages.get().PaymentStatus_Approved
							: FulfillmentMessages.get().PaymentStatus_Not_Processed;
				default:
					return null;
			}
		}

		@Override
		public void addListener(final ILabelProviderListener listener) {
			// nothing
		}

		@Override
		public void dispose() {
			// nothing
		}

		@Override
		public boolean isLabelProperty(final Object element, final String property) {
			return false;
		}

		@Override
		public void removeListener(final ILabelProviderListener listener) {
			// nothing
		}

	}

}