/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.order;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.service.AuthorizationService;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.ControlModificationListener;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.actions.ResolveOrderHoldContributionAction;
import com.elasticpath.cmclient.fulfillment.editors.actions.MarkOrderHoldUnresolvableContributionAction;
import com.elasticpath.cmclient.fulfillment.editors.actions.ViewOrderHoldContributionAction;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.service.order.OrderHoldService;

/**
 * A part to display a table of order holds associated with an order. Provides mechanisms to mark resolved, mark unresolvable and view holds.
 */
public class OrderHoldListSectionPart extends AbstractCmClientEditorPageSectionPart implements SelectionListener, IPropertyListener {

	private static final int RULE_WIDTH = 435;
	private static final int STATUS_WIDTH = 200;
	private static final int RESOLVED_BY_WIDTH = 200;
	private static final int TABLE_HEIGHT_HINT = 263;
	private static final int TABLE_WIDTH_HINT = 1200;

	private static final String ORDER_HOLD_TABLE = "Order Hold Table";

	private Button resolveHoldButton;

	private Button markHoldUnresolvedButton;

	private Button viewHoldButton;

	private final List<OrderHold> orderHolds;

	private OrderHold orderHold;

	private EpState authorization;

	private TableViewer tableViewer;

	private final ControlModificationListener controlModificationListener;

	private ViewOrderHoldContributionAction viewHoldAction;

	private ResolveOrderHoldContributionAction resolveHoldAction;

	private MarkOrderHoldUnresolvableContributionAction markHoldUnresolvedAction;

	private final OrderEditor editor;

	/**
	 * Constructor.
	 *
	 * @param formPage the FormPage
	 * @param editor the AbstractCmClientFormEditor
	 */
	public OrderHoldListSectionPart(final FormPage formPage, final AbstractCmClientFormEditor editor) {
		super(formPage, editor, ExpandableComposite.TITLE_BAR);
		this.orderHolds = getOrderHoldService().findOrderHoldsByOrderUid(((Order) editor.getModel()).getUidPk());
		this.controlModificationListener = editor;
		this.editor = (OrderEditor) editor;
		editor.addPropertyListener(this);
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// nothing

	}

	private OrderHoldService getOrderHoldService() {
		return BeanLocator.getSingletonBean(ContextIdNames.ORDER_HOLD_SERVICE, OrderHoldService.class);
	}

	@Override
	protected void createControls(final Composite parentComposite, final FormToolkit toolkit) {
		final IEpLayoutComposite mainPane = CompositeFactory.createTableWrapLayoutComposite(parentComposite, 1, false);
		mainPane.setControlModificationListener(this.controlModificationListener);
		final TableWrapData mainPaneWrapData = new TableWrapData(TableWrapData.FILL, TableWrapData.FILL);
		mainPaneWrapData.grabHorizontal = true;
		mainPane.setLayoutData(mainPaneWrapData);

		//CTable Composite
		createTableComposite(mainPane);
	}

	private void createTableComposite(final IEpLayoutComposite mainPane) {
		IEpLayoutComposite tableComposite = mainPane.addGridLayoutComposite(2, false,
				mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		final IEpTableViewer epTableViewer = tableComposite.addTableViewer(false, authorization,
				mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false), ORDER_HOLD_TABLE);

		GridData tableGridData = (GridData) epTableViewer.getSwtTable().getLayoutData();
		tableGridData.heightHint = TABLE_HEIGHT_HINT;
		tableGridData.widthHint = TABLE_WIDTH_HINT;
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderHoldTable_RuleColumn, RULE_WIDTH);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderHoldTable_StatusColumn, STATUS_WIDTH);
		epTableViewer.addTableColumn(FulfillmentMessages.get().OrderHoldTable_ResolvedByColumn, RESOLVED_BY_WIDTH);

		epTableViewer.setLabelProvider(new OrderHoldLabelProvider());
		epTableViewer.setContentProvider(new OrderHoldContentProvider());
		epTableViewer.setInput(this.orderHolds);

		epTableViewer.getSwtTableViewer().addSelectionChangedListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				if (event.getSelection().isEmpty()) {
					resolveHoldButton.setEnabled(false);
					markHoldUnresolvedButton.setEnabled(false);
					viewHoldButton.setEnabled(false);
					return;
				}

				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				this.orderHold = (OrderHold) selection.getFirstElement();
				viewHoldButton.setEnabled(true);
				viewHoldAction = new ViewOrderHoldContributionAction(orderHold);
				viewHoldAction.setViewOnly(true);

				if (orderHold.getStatus().equals(OrderHoldStatus.ACTIVE)
						&& AuthorizationService.getInstance().isAuthorizedWithPermission(orderHold.getPermission())
				) {
					resolveHoldButton.setEnabled(true);
					markHoldUnresolvedButton.setEnabled(true);

					Order order = ((Order) editor.getModel());
					resolveHoldAction = new ResolveOrderHoldContributionAction(
							orderHold, order);
					resolveHoldAction.setViewOnly(false);
					markHoldUnresolvedAction = new MarkOrderHoldUnresolvableContributionAction(
							orderHold, order);
					markHoldUnresolvedAction.setViewOnly(false);

				} else {
					resolveHoldButton.setEnabled(false);
					markHoldUnresolvedButton.setEnabled(false);
				}

			}
		});
		epTableViewer.getSwtTableViewer().setSorter(new ViewerSorter() {
			@Override
			public int compare(final Viewer viewer, final Object obj1, final Object obj2) {
				return ((OrderHold) obj2).getCreationDate().compareTo(((OrderHold) obj1).getCreationDate());
			}
		});
		epTableViewer.getSwtTableViewer().addDoubleClickListener(event -> {
			orderHold = (OrderHold) ((IStructuredSelection) event.getSelection()).getFirstElement();
			viewHoldAction.run();
		});
		this.tableViewer = epTableViewer.getSwtTableViewer();

		final IEpLayoutComposite buttonsPane = tableComposite.addTableWrapLayoutComposite(1, true,
				tableComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		resolveHoldButton = buttonsPane.addPushButton(FulfillmentMessages.get().OrderHoldList_MarkResolvedButton, null, authorization,
				mainPane.createLayoutData());
		resolveHoldButton.setEnabled(false);

		markHoldUnresolvedButton = buttonsPane.addPushButton(FulfillmentMessages.get().OrderHoldList_MarkUnresolvableButton,
				null, EpState.EDITABLE, mainPane.createLayoutData());
		markHoldUnresolvedButton.setEnabled(false);

		viewHoldButton = buttonsPane.addPushButton(FulfillmentMessages.get().OrderHoldList_ViewButton,
				null, EpState.EDITABLE, mainPane.createLayoutData());
		viewHoldButton.setEnabled(false);
	}

	@Override
	protected void populateControls() {
		resolveHoldButton.addSelectionListener(this);
		markHoldUnresolvedButton.addSelectionListener(this);
		viewHoldButton.addSelectionListener(this);
	}

	/**
	 * Hold used.
	 *
	 * @param event the selectionEvent
	 */
	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		// nothing
	}

	/**
	 * Invoked when a button (mark resolved/mark unresolvable/view) is clicked.
	 *
	 * @param event the selectionEvent
	 */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (event.getSource() == resolveHoldButton) {
			resolveHoldAction.run();
			refreshHolds();
			resolveHoldButton.setEnabled(false);
			markHoldUnresolvedButton.setEnabled(false);
		}
		if (event.getSource() == markHoldUnresolvedButton) {
			markHoldUnresolvedAction.run();
			refreshHolds();
			resolveHoldButton.setEnabled(false);
			markHoldUnresolvedButton.setEnabled(false);
		}
		if (event.getSource() == viewHoldButton) {
			viewHoldAction.setViewOnly(true);
			viewHoldAction.run();
		}
	}

	@Override
	protected String getSectionDescription() {
		return FulfillmentMessages.EMPTY_STRING;
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.EMPTY_STRING;
	}

	/**
	 * This label provider returns the text that should appear in each column for a given <code>OrderHold</code> object. This
	 * also determines the icon that should appear in the first column.
	 */
	class OrderHoldLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Gets the image for each column.
		 *
		 * @param element the image
		 * @param columnIndex the index for the column
		 * @return Image the image
		 */
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/**
		 * Gets the text for each column.
		 *
		 * @param element the text
		 * @param columnIndex the index for the column
		 * @return String the text
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final OrderHold orderHold = (OrderHold) element;
			switch (columnIndex) {
				case 0:
					return orderHold.getHoldDescription();
				case 1:
					return orderHold.getStatus().toString();
				case 2:
					return (orderHold.getResolvedBy() == null) ? StringUtils.EMPTY : orderHold.getResolvedBy();
				default:
					return StringUtils.EMPTY;
			}
		}
	}

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in adapters or
	 * simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore it and always show
	 * the same content.
	 */
	class OrderHoldContentProvider implements IStructuredContentProvider {

		/**
		 * Gets the order hold from the list of order holds for each row.
		 *
		 * @param inputElement the input order hold element
		 * @return Object[] the returned input
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public Object[] getElements(final Object inputElement) {
			return ((List<OrderHold>) inputElement).toArray();
		}

		/**
		 * dispose the provider.
		 */
		@Override
		public void dispose() {
			// does nothing
		}

		/**
		 * Notify the provider the input has changed.
		 *
		 * @param viewer the epTableViewer
		 * @param oldInput the previous input
		 * @param newInput the current selected input
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// does nothing
		}
	}

	/**
	 * Refreshes the order hold table.
	 */
	public void refreshHolds() {
		tableViewer.refresh();
	}

	/**
	 * Gets the table's modification listener.
	 *
	 * @return ControlModificationListener the modification listener attached to table viewer
	 */
	public ControlModificationListener getControlModificationListener() {
		return controlModificationListener;
	}

	@Override
	public void sectionDisposed() {
		editor.removePropertyListener(this);
	}

	/**
	 * Invoked when property of this section is changed.
	 *
	 * @param source the event source
	 * @param propId the specific id
	 */
	@Override
	public void propertyChanged(final Object source, final int propId) {
		if (propId == OrderEditor.PROP_HOLD_MODIFIED) {
			refreshHolds();
		}
	}

}
