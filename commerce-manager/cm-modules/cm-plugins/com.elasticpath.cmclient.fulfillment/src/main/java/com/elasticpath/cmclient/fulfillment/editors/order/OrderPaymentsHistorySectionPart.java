/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.order;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.domain.OrderPaymentPresenter;
import com.elasticpath.cmclient.fulfillment.domain.OrderPaymentPresenterImpl;
import com.elasticpath.cmclient.fulfillment.editors.order.actions.impl.ViewOrderPaymentDetailsAction;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;

/**
 * Represents the UI of order payment history section.
 */
public class OrderPaymentsHistorySectionPart extends AbstractCmClientEditorPageSectionPart implements SelectionListener {

	private static final String ORDER_PAYMENTS_HISTORY_TABLE = "Order Payments History Table"; //$NON-NLS-1$
	private static final OrderPaymentService ORDER_PAYMENTS_SERVICE =
			BeanLocator.getSingletonBean(ContextIdNames.ORDER_PAYMENT_SERVICE, OrderPaymentService.class);

	private static final int COLUMN_WIDTH_DATE = 150;

	private static final int COLUMN_WIDTH_METHOD = 200;

	private static final int COLUMN_WIDTH_TYPE = 100;

	private static final int COLUMN_WIDTH_DETAILS = 200;

	private static final int COLUMN_WIDTH_STATUS = 80;

	private static final int COLUMN_WIDTH_AMOUNT = 100;

	private static final int COLUMN_INDEX_DATE = 0;

	private static final int COLUMN_INDEX_METHOD = 1;

	private static final int COLUMN_INDEX_TYPE = 2;

    private static final int COLUMN_INDEX_DETAIL = 3;

    private static final int COLUMN_INDEX_STATUS = 4;

    private static final int COLUMN_INDEX_AMOUNT = 5;

    private static final int TABLE_HEIGHT_HINT = 150;
    private IEpTableViewer epTableViewer;

    private final Order order;
    private final Collection<OrderPayment> orderPayments;
    private boolean skippedEventsIsSelected;

    private Button viewPaymentDetailsButton;
    private Button showSkippedEvents;

    private Action viewOrderDetailsAction;

    /**
     * Constructor.
	 *
	 * @param formPage the formpage
	 * @param editor   the CmClientFormEditor that contains the form
	 */
	public OrderPaymentsHistorySectionPart(final FormPage formPage, final AbstractCmClientFormEditor editor) {
        super(formPage, editor, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
        // super(formPage, editor, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR); //| ExpandableComposite.EXPANDED);
        this.order = (Order) editor.getModel();
        this.orderPayments = ORDER_PAYMENTS_SERVICE.findByOrder(order);
    }

	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {

		showSkippedEvents = toolkit.createButton(client, FulfillmentMessages.get().ShowSkippedPaymentEvents_Label, SWT.CHECK);
		showSkippedEvents.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL, 1, 2));

		final IEpLayoutComposite mainPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		((TableWrapLayout) mainPane.getSwtComposite().getLayout()).leftMargin = 0;

		mainPane.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

		final IEpLayoutData tableData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, false);
		epTableViewer = mainPane.addTableViewer(false, EpState.READ_ONLY, tableData, ORDER_PAYMENTS_HISTORY_TABLE);
		
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_DateTime, COLUMN_WIDTH_DATE);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Method, COLUMN_WIDTH_METHOD);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Type, COLUMN_WIDTH_TYPE);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Details, COLUMN_WIDTH_DETAILS);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Status, COLUMN_WIDTH_STATUS);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderPaymentHistorySection_TableTitle_Amount, COLUMN_WIDTH_AMOUNT);

		epTableViewer.setContentProvider(new ViewContentProvider());
		epTableViewer.setLabelProvider(new ViewLabelProvider());
		((TableWrapData) epTableViewer.getSwtTable().getLayoutData()).heightHint = TABLE_HEIGHT_HINT;

		final IEpLayoutData buttonPaneData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL);
		final IEpLayoutComposite buttonsPane = mainPane.addTableWrapLayoutComposite(1, true, buttonPaneData);
		final IEpLayoutData buttonData = mainPane.createLayoutData();

		viewPaymentDetailsButton = buttonsPane.addPushButton(FulfillmentMessages.get().OrderPaymentHistorySection_ViewPaymentDetailsButton,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_VIEW_ORDER_DETAILS), EpState.READ_ONLY, buttonData);
		viewPaymentDetailsButton.setEnabled(false);
	}

	@Override
	protected String getSectionDescription() {
		return FulfillmentMessages.get().PaymentHistorySection_Description;
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().PaymentHistorySection_Title;
	}

	@Override
	protected void populateControls() {
		viewPaymentDetailsButton.addSelectionListener(this);
		epTableViewer.getSwtTableViewer().addSelectionChangedListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				if (event.getSelection().isEmpty()) {
					viewPaymentDetailsButton.setEnabled(false);
					return;
				}

				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				OrderPayment selectedOrderPayment = (OrderPayment) selection.getFirstElement();
				viewPaymentDetailsButton.setEnabled(true);

				viewOrderDetailsAction = new ViewOrderPaymentDetailsAction(selectedOrderPayment);
			}
		});
		showSkippedEvents.addSelectionListener(this);
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// Nothing to do.
	}

	/**
	 * Provides the column text and image for the payments table.
	 */
	@SuppressWarnings("PMD.CyclomaticComplexity")
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Get the column image.
		 *
		 * @param element     not used
		 * @param columnIndex the column to create an image for
		 * @return the image
		 */
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/**
		 * Provide the content for each column.
		 *
		 * @param element     element object
		 * @param columnIndex index
		 * @return the value for that column
		 */
		@Override
		@SuppressWarnings("PMD.CyclomaticComplexity")
		public String getColumnText(final Object element, final int columnIndex) {
            OrderPaymentPresenter orderPaymentPresenter = new OrderPaymentPresenterImpl((OrderPayment) element, order.getLocale());
            switch (columnIndex) {
                case COLUMN_INDEX_DATE:
                    return orderPaymentPresenter.getDisplayCreatedDate();
                case COLUMN_INDEX_METHOD:
                    return orderPaymentPresenter.getDisplayPaymentMethod();
                case COLUMN_INDEX_TYPE:
                    return orderPaymentPresenter.getDisplayTransactionType();
                case COLUMN_INDEX_DETAIL:
                    return orderPaymentPresenter.getDisplayPaymentDetails();
                case COLUMN_INDEX_STATUS:
					return orderPaymentPresenter.getDisplayStatus();
				case COLUMN_INDEX_AMOUNT:
					return orderPaymentPresenter.getDisplayPaymentAmount();
				default:
					return StringUtils.EMPTY;
			}
		}
	}

	/**
	 * Provides the content for the payments.
	 */
	class ViewContentProvider implements IStructuredContentProvider {

		/**
		 * Called when the viewer input is changed.
		 *
		 * @param viewer   the viewer
		 * @param oldInput the old input
		 * @param newInput the new input
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// No action required
		}

		/**
		 * Dispose the content.
		 */
		@Override
		public void dispose() {
			// Nothing to dispose
		}

		/**
		 * Returns an array of elements to display.
		 *
		 * @param inputElement the input element (An Order)
		 * @return an array of order payments
		 */
		@Override
		public Object[] getElements(final Object inputElement) {
			if (inputElement != null) {
				if (!skippedEventsIsSelected) {
					return orderPayments.stream()
							.filter(orderPayment -> !orderPayment.getOrderPaymentStatus().equals(OrderPaymentStatus.SKIPPED))
							.toArray();
				}

				return orderPayments.toArray();
			}
			return new Object[0];
		}
	}

	/**
	 * Refresh the section with order data.
	 *
	 * @param order the order
	 */
	public void refreshData(final Order order) {
		epTableViewer.setInput(order);
	}

	/**
	 * Not used.
	 *
	 * @param event the event object
	 */
	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		// Empty method
	}

	/**
	 * Invoked on selection event.
	 *
	 * @param event the event object
	 */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (event.getSource() == viewPaymentDetailsButton) {
			viewOrderDetailsAction.run();
		} else if (event.getSource() == this.showSkippedEvents) {
			this.skippedEventsIsSelected = showSkippedEvents.getSelection();
			this.epTableViewer.getSwtTableViewer().refresh();
		}
	}
}
