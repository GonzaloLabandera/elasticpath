/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.cmclient.fulfillment.editors.order.dialog;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.EPTestUtilFactory;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.domain.OrderPaymentPresenter;
import com.elasticpath.cmclient.fulfillment.domain.OrderPaymentPresenterImpl;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentData;
import com.elasticpath.service.order.OrderService;

/**
 * Dialog displaying the details of an Order Payment.
 */
public class OrderPaymentDetailsDialog extends Dialog {

    private final OrderPayment orderPayment;
    private final Locale locale;

    private static final String ORDER_PAYMENT_DATA_TABLE = "Order Payment Data";
    private static final String ORDER_PAYMENT_TABLE = "Order Payment";
    private static final String COLUMN_KEY = "Key";
    private static final String COLUMN_VALUE = "Value";
    private static final int COLUMN_INDEX_KEY = 0;
    private static final int COLUMN_INDEX_VALUE = 1;
    private static final int COLUMN_KEY_WIDTH = 200;
    private static final int COLUMN_VALUE_WIDTH = 350;
    private static final int TABLE_WIDTH_HINT = 500;
	private static final int TABLE_HEIGHT_HINT = 280;

    /**
     * Create a new dialog to display Order payment details.
     *
     * @param parentShell  - The {@link Shell} to attach the dialog to.
     * @param orderPayment - The {@link OrderPayment} whose details are to be displayed in this dialog.
     */
    public OrderPaymentDetailsDialog(final Shell parentShell, final OrderPayment orderPayment) {
        super(parentShell);
        this.orderPayment = orderPayment;
        this.locale = getOrderService().findOrderByOrderNumber(orderPayment.getOrderNumber()).getLocale();
    }

    private OrderService getOrderService() {
        return BeanLocator.getSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite area = (Composite) super.createDialogArea(parent);
        final IEpLayoutComposite dialogComposite = CompositeFactory.createGridLayoutComposite(area, 1, false);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // alter the layout data margins for the dialog
        final GridLayout gridLayout = (GridLayout) dialogComposite.getSwtComposite().getLayout();
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginBottom = 0;

		this.createEpDialogContent(dialogComposite);
		return area;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getWindowTitle());
		EPTestUtilFactory.getInstance().getTestIdUtil().setId(newShell, getWindowTitle());
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.get().CLOSE_LABEL, true);
	}

	@Override
	protected Button createButton(final Composite parent, final int buttonId, final String label, final boolean defaultButton) {
		Button button = super.createButton(parent, buttonId, label, defaultButton);
		EPTestUtilFactory.getInstance().getTestIdUtil().setId(button, label);
		return button;
	}

	@Override
	protected void setButtonLayoutData(final Button button) {
		super.setButtonLayoutData(button);
		((GridData) button.getLayoutData()).verticalAlignment = GridData.FILL;
	}

	private String getWindowTitle() {
		return FulfillmentMessages.get().OrderPaymentsHistorySection_ViewPaymentDetails_WindowTitle;
	}

	private void createEpDialogContent(final IEpLayoutComposite dialogComposite) {
		IEpLayoutComposite dialogMainPane = dialogComposite.addGridLayoutComposite(1, false, dialogComposite.createLayoutData());
		createOrderPaymentTable(dialogMainPane);
		dialogMainPane.addHorizontalSeparator(dialogMainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false, 1, 1));
		IEpLayoutData additionalDataLayout = dialogMainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false, 1, 1);
		dialogMainPane.addLabel(FulfillmentMessages.get().OrderPaymentsHistorySection_ViewPaymentDetails_AdditionalDataLabel, additionalDataLayout);
		createOrderPaymentDataTable(dialogMainPane);
	}

	private void createOrderPaymentTable(final IEpLayoutComposite dialogMainPane) {
		IEpLayoutData tableLayoutData = dialogMainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true);
		IEpTableViewer additionalDataTable = dialogMainPane.addTableViewer(false, EpControlFactory.EpState.READ_ONLY, tableLayoutData,
				ORDER_PAYMENT_TABLE);
		additionalDataTable.addTableColumn(COLUMN_KEY, COLUMN_KEY_WIDTH);
		additionalDataTable.addTableColumn(COLUMN_VALUE, COLUMN_VALUE_WIDTH);
		additionalDataTable.getSwtTable().setHeaderVisible(false);
		additionalDataTable.setContentProvider(new OrderPaymentDetailsDialog.OrderPaymentContentProvider());
		additionalDataTable.setLabelProvider(new OrderPaymentDetailsDialog.OrderPaymentLabelProvider());
		additionalDataTable.setInput(orderPayment);
		GridData tableGridData = (GridData) additionalDataTable.getSwtTable().getLayoutData();
		tableGridData.widthHint = TABLE_WIDTH_HINT;
	}

	private void createOrderPaymentDataTable(final IEpLayoutComposite dialogMainPane) {
		IEpLayoutData tableLayoutData = dialogMainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, false, false);
		IEpTableViewer additionalDataTable = dialogMainPane.addTableViewer(false, EpControlFactory.EpState.READ_ONLY, tableLayoutData,
				ORDER_PAYMENT_DATA_TABLE);
		additionalDataTable.addTableColumn(COLUMN_KEY, COLUMN_KEY_WIDTH);
		additionalDataTable.addTableColumn(COLUMN_VALUE, COLUMN_VALUE_WIDTH);
		additionalDataTable.getSwtTable().setHeaderVisible(false);
		additionalDataTable.setContentProvider(new OrderPaymentDetailsDialog.OrderPaymentDataContentProvider());
		additionalDataTable.setLabelProvider(new OrderPaymentDetailsDialog.OrderPaymentDataLabelProvider());
		additionalDataTable.setInput(orderPayment);
		GridData tableGridData = (GridData) additionalDataTable.getSwtTable().getLayoutData();
		tableGridData.widthHint = TABLE_WIDTH_HINT;
		tableGridData.heightHint = TABLE_HEIGHT_HINT;
	}

	/**
	 * {@link IStructuredContentProvider} for Order Payment.
	 */
	private class OrderPaymentContentProvider implements IStructuredContentProvider {

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// No action required
		}

		@Override
		public void dispose() {
			// Nothing to dispose
		}

		@Override
		public Object[] getElements(final Object inputElement) {
			if (inputElement != null) {
                OrderPaymentPresenter orderPaymentPresenter = new OrderPaymentPresenterImpl((OrderPayment) inputElement, locale);
                return Arrays.asList(
                        new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_DateTime,
                                orderPaymentPresenter.getDisplayCreatedDate()),
                        new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Method,
                                orderPaymentPresenter.getDisplayPaymentMethod()),
                        new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Type,
                                orderPaymentPresenter.getDisplayTransactionType()),
                        new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Details,
                                orderPaymentPresenter.getDisplayPaymentDetails()),
                        new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Status,
								orderPaymentPresenter.getDisplayStatus()),
						new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Amount,
								orderPaymentPresenter.getDisplayPaymentAmount()),
						new OrderPaymentDetailsDialog.OrderPaymentsPair(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_IsOriginalPI,
								orderPaymentPresenter.getDisplayIsOriginalPI())
				).toArray();
			}
			return new Object[0];
		}
	}

	/**
	 * {@link LabelProvider} for Order Payment.
	 */
	private class OrderPaymentLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			switch (columnIndex) {
				case COLUMN_INDEX_KEY:
					return ((OrderPaymentDetailsDialog.OrderPaymentsPair) element).getColumnName();
				case COLUMN_INDEX_VALUE:
					return ((OrderPaymentDetailsDialog.OrderPaymentsPair) element).getColumnValue();
				default:
					return StringUtils.EMPTY;
			}
		}
	}

	/**
	 * A pair of key and values to be displayed in a two column table as first and second values respectively.
	 */
	private class OrderPaymentsPair {
		private final String columnName;
		private final String columnValue;

		OrderPaymentsPair(final String columnName, final String columnValue) {
			this.columnName = columnName;
			this.columnValue = columnValue;
		}

		String getColumnName() {
			return columnName;
		}

		String getColumnValue() {
			return columnValue;
		}
	}

	/**
	 * {@link IStructuredContentProvider} for Order Payment detailed data.
	 */
	private class OrderPaymentDataContentProvider implements IStructuredContentProvider {

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// No action required
		}

		@Override
		public void dispose() {
			// Nothing to dispose
		}

		@Override
		public Object[] getElements(final Object inputElement) {
			if (inputElement != null) {
				return ((OrderPayment) inputElement).getOrderPaymentData().toArray();
			}
			return new Object[0];
		}
	}

	/**
	 * {@link LabelProvider} for Order Payment detailed data.
	 */
	private class OrderPaymentDataLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			switch (columnIndex) {
				case COLUMN_INDEX_KEY:
					return ((OrderPaymentData) element).getKey();
				case COLUMN_INDEX_VALUE:
					return ((OrderPaymentData) element).getValue();
				default:
					return StringUtils.EMPTY;
			}
		}
	}

	/**
	 * Notes: Might need to implement AbstractEpDialog.dialogResizeOverride()
	 */
}
