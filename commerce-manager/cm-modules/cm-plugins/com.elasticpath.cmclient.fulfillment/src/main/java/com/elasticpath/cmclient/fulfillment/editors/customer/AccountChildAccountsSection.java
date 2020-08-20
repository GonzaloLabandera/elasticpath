/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.customer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.event.FulfillmentEventService;
import com.elasticpath.cmclient.fulfillment.wizards.account.CreateAccountWizard;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Account's child accounts section.
 */
public class AccountChildAccountsSection extends AbstractCmClientEditorPageSectionPart {

	private static final int COLUMN_NAME_WIDTH = 250;

	private static final int COLUMN_STATUS_WIDTH = 100;

	private static final String CHILD_ACCOUNTS_TABLE = "Child Accounts"; //$NON-NLS-1$

	private static final int HEIGHT_HINT = 105;

	private static final int PAGE_WIDTH = 700;

	private static final int PAGE_HEIGHT = 400;

	private final Customer customer;

	private IEpLayoutComposite mainPane;

	private Customer selectedChild;

	private Button addChildAccountButton;

	private Button openAccountProfileButton;

	private final IWorkbenchPartSite workbenchPartSite;

	private static final Logger LOG = Logger.getLogger(AccountChildAccountsSection.class);

	private final AccountTreeService accountTreeService;

	private final CustomerService customerService;

	private TableViewer tableViewer;

	/**
	 * Construct the child accounts section.
	 *
	 * @param formPage          the form page.
	 * @param editor            the editor.
	 * @param workbenchPartSite the workbench site to be used.
	 */
	public AccountChildAccountsSection(final FormPage formPage, final AbstractCmClientFormEditor editor,
									   final IWorkbenchPartSite workbenchPartSite) {
		super(formPage, editor, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		this.customer = (Customer) editor.getModel();
		this.workbenchPartSite = workbenchPartSite;
		this.accountTreeService = BeanLocator.getSingletonBean(ContextIdNames.ACCOUNT_TREE_SERVICE, AccountTreeService.class);
		this.customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
	}

	/**
	 * Creates the account group controls in the dialog.
	 *
	 * @param client  the composite.
	 * @param toolkit the form toolkit.
	 */
	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		mainPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		mainPane.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

		final IEpLayoutData tableData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);

		final IEpTableViewer epTableViewer = mainPane.addTableViewer(false, EpState.EDITABLE, tableData, CHILD_ACCOUNTS_TABLE);
		final TableWrapData layoutData = (TableWrapData) epTableViewer.getSwtTable().getLayoutData();
		layoutData.heightHint = HEIGHT_HINT;

		epTableViewer.addTableColumn(FulfillmentMessages.get().AccountChildAccounts_ColumnName, COLUMN_NAME_WIDTH);
		epTableViewer.addTableColumn(FulfillmentMessages.get().AccountChildAccounts_ColumnStatus, COLUMN_STATUS_WIDTH);

		epTableViewer.setLabelProvider(new AccountChildrenTableLabelProvider());
		epTableViewer.setContentProvider(new AccountChildrenTableContentProvider());

		epTableViewer.setInput(this.customer);
//		// Add the listener to the table to retrieve selections
		epTableViewer.getSwtTableViewer().addSelectionChangedListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				AccountChildAccountsSection.this.selectedChild =
						(Customer) ((IStructuredSelection) event.getSelection()).getFirstElement();
				this.openAccountProfileButton.setEnabled(true);
			}
		});
		this.tableViewer = epTableViewer.getSwtTableViewer();

		final IEpLayoutData buttonPaneData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL);
		final IEpLayoutComposite buttonsPane = mainPane.addTableWrapLayoutComposite(1, true, buttonPaneData);
		final IEpLayoutData buttonData = mainPane.createLayoutData();

		this.addChildAccountButton = buttonsPane.addPushButton(FulfillmentMessages.get().AccountChildAccountsPage_Add, EpState.EDITABLE,
				buttonData);
		this.addChildAccountButton.addSelectionListener(createOpenAccountButtonListener());

		this.openAccountProfileButton = buttonsPane.addPushButton(FulfillmentMessages.get().AccountChildAccountsPage_Open, EpState.EDITABLE,
				buttonData);
		openAccountProfileButton.addSelectionListener(createChildAccountsButtonListener());
		this.openAccountProfileButton.setEnabled(false);

	}

	private SelectionListener createOpenAccountButtonListener() {
		return new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				//not used
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				final CreateAccountWizard createAccountWizard =
						new CreateAccountWizard(FulfillmentMessages.get().CreateAccountWizard_AddChildAccount_Title,
								AccountChildAccountsSection.this.customer);

				final WizardDialog dialog = new EpWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						createAccountWizard);
				dialog.setPageSize(PAGE_WIDTH, PAGE_HEIGHT);
				dialog.addPageChangingListener(createAccountWizard);

				final int result = dialog.open();

				if (result == Window.OK) {
					// RCPRAP - experiment to get onto appropriate thread
					Display.getDefault().asyncExec(
							new Runnable() {
								@Override
								public void run() {
									tableViewer.refresh();
									FulfillmentEventService.getInstance().fireCustomerChangeEvent(new ItemChangeEvent<>(this, customer));
								}
							});
				}
			}
		};
	}

	private SelectionListener createChildAccountsButtonListener() {
		return new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				final IEditorInput editorInput = new CustomerDetailsEditorInput(selectedChild.getUidPk());

				try {
					workbenchPartSite.getPage().openEditor(editorInput, AccountDetailsEditor.ID_EDITOR);
				} catch (final PartInitException e) {
					LOG.error("Can not open child account details editor", e); //$NON-NLS-1$
				}
			}
		};
	}

	@Override
	protected void populateControls() {
		// Not used
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// Not used
	}

	/**
	 * Content Provider class. Gets the account's children.
	 */
	class AccountChildrenTableContentProvider implements IStructuredContentProvider {
		/**
		 * Return the children of account as array.
		 *
		 * @param element Customer input
		 * @return the addresses
		 */
		@Override
		public Object[] getElements(final Object element) {
			return (element instanceof Customer)
					? accountTreeService.fetchChildAccountGuids((Customer) element)
					.stream()
					.map(customerService::findByGuid)
					.toArray()
					: null;
		}

		/**
		 * Not needed.
		 */
		@Override
		public void dispose() {
			// nothing to dispose
		}

		/**
		 * Not needed unless customer search results sharing same view.
		 *
		 * @param viewer    the view
		 * @param oldObject the old object
		 * @param newObject the new object
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldObject, final Object newObject) {
			// not needed
		}
	}

	/**
	 * Provides the columns for the account children table.
	 */
	class AccountChildrenTableLabelProvider extends LabelProvider implements ITableLabelProvider {

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
		 * Get the column text from selected address.
		 *
		 * @param element     the data input element
		 * @param columnIndex the column index
		 * @return text
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final Customer child = (Customer) element;

			switch (columnIndex) {
				case 0:
					return child.getBusinessName();
				case 1:
					return extractStatus(child.getStatus());
				default:
					return ""; //$NON-NLS-1$
			}
		}
	}

	private String extractStatus(final int statusCode) {
		final String status;
		switch (statusCode) {
			case Customer.STATUS_ACTIVE:
				status = FulfillmentMessages.get().AccountChildAccounts_ColumnStatusActive;
				break;
			case Customer.STATUS_DISABLED:
				status = FulfillmentMessages.get().AccountChildAccounts_ColumnStatusDisabled;
				break;
			case Customer.STATUS_PENDING_APPROVAL:
				status = FulfillmentMessages.get().AccountChildAccounts_ColumnStatusPendingApproval;
				break;
			default:
				status = StringUtils.EMPTY;
				break;
		}

		return status;
	}
}
