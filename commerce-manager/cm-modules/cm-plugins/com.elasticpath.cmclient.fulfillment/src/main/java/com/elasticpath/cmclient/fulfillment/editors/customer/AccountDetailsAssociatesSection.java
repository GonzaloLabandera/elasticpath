/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cmclient.fulfillment.editors.customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
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
import com.elasticpath.cmclient.fulfillment.FulfillmentPermissions;
import com.elasticpath.cmclient.fulfillment.editors.customer.dialogs.AccountAddAssociationDialog;
import com.elasticpath.cmclient.fulfillment.editors.customer.dialogs.AccountRemoveAssociationDialog;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Account Associates Section.
 */
public class AccountDetailsAssociatesSection extends AbstractCmClientEditorPageSectionPart implements SelectionListener {

	/** serialVersionUID. */
	private static final long serialVersionUID = -3131557219939699766L;
	
	private static final Logger LOG = Logger.getLogger(AccountDetailsAssociatesSection.class);

	private static final int COLUMN_WIDTH_ASSOCIATE_NAME = 250;
	
	private static final int COLUMN_WIDTH_ASSOCIATE_EMAIL = 250;
	
	private static final int COLUMN_WIDTH_ASSOCIATE_ROLES = 250;
	
	private static final String ASSOCIATES_TABLE = "Associates Table"; //$NON-NLS-1$

	private final transient Customer account;

	private final transient ControlModificationListener listener;

	private transient TableViewer tableViewer;

	private transient IEpLayoutComposite mainPane;

	private transient EpState authorization;

	private transient AccountDetailsAssociatesRow selectedRow;

	private transient Button addAssociateButton;

	private transient Button editAssociateButton;

	private transient Button removeAssociateButton;

	private transient Button openCustomerProfileButton;

	private transient CustomerService customerService;
	
	private transient UserAccountAssociationService userAccountAssociationService;
	
	/**
	 * Construct the Account Associates section.
	 * 
	 * @param formPage the form page
	 * @param editor the editor
	 */
	public AccountDetailsAssociatesSection(final FormPage formPage, final AbstractCmClientFormEditor editor) {
		super(formPage, editor, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		this.account = (Customer) editor.getModel();
		this.listener = editor;
	}

	/**
	 * Gets the section title.
	 * 
	 * @return string
	 */
	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().AssociatesPage_Title;
	}

	/**
	 * Creates the account associates controls in the dialog.
	 * 
	 * @param client the composite
	 * @param toolkit the form toolkit
	 */
	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {

		if (customerService == null) {
			customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
		}

		final AuthorizationService authorizationService = AuthorizationService.getInstance();

		boolean isAuthorized = authorizationService.isAuthorizedWithPermission(FulfillmentPermissions.CUSTOMER_EDIT)
				&& authorizationService.isAuthorizedForStore(account.getStoreCode());

		if (isAuthorized) {
			authorization = EpState.EDITABLE;
		} else {
			authorization = EpState.READ_ONLY;
		}

		mainPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		mainPane.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

		final IEpLayoutData tableData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);

		final IEpTableViewer epTableViewer = mainPane.addTableViewer(false, authorization, tableData, ASSOCIATES_TABLE);

		epTableViewer.addTableColumn(FulfillmentMessages.get().AssociatesPage_Name, COLUMN_WIDTH_ASSOCIATE_NAME);
		epTableViewer.addTableColumn(FulfillmentMessages.get().AssociatesPage_Email, COLUMN_WIDTH_ASSOCIATE_EMAIL);
		epTableViewer.addTableColumn(FulfillmentMessages.get().AssociatesPage_Roles, COLUMN_WIDTH_ASSOCIATE_ROLES);
		

		epTableViewer.setLabelProvider(new AssociatesTableLabelProvider());
		epTableViewer.setContentProvider(new AssociatesTableContentProvider());

		epTableViewer.setInput(this.account);
		// Add the listener to the table to retrieve selections
		epTableViewer.getSwtTableViewer().addSelectionChangedListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				AccountDetailsAssociatesSection.this.selectedRow = (AccountDetailsAssociatesRow) ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				setButtonsState();
			}
		});
		this.tableViewer = epTableViewer.getSwtTableViewer();

		final IEpLayoutData buttonPaneData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL);
		final IEpLayoutComposite buttonsPane = mainPane.addTableWrapLayoutComposite(1, true, buttonPaneData);
		final IEpLayoutData buttonData = mainPane.createLayoutData();

		this.addAssociateButton = buttonsPane.addPushButton(FulfillmentMessages.get().AssociatesPage_Add,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_ADD), authorization, buttonData);
		
		this.editAssociateButton = buttonsPane.addPushButton(FulfillmentMessages.get().AssociatesPage_Edit,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT), authorization, buttonData);

		this.removeAssociateButton = buttonsPane.addPushButton(FulfillmentMessages.get().AssociatesPage_Remove,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_REMOVE), authorization, buttonData);
		
		this.openCustomerProfileButton = buttonsPane.addPushButton(FulfillmentMessages.get().AssociatesPage_OpenCustomerProfile,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_OPEN), authorization, buttonData);

		setButtonsState();
	}

	private void setButtonsState() {
		final boolean canEdit = EpState.EDITABLE.equals(authorization);
		this.addAssociateButton.setEnabled(canEdit);
		this.editAssociateButton.setEnabled(canEdit && isSelected());
		this.removeAssociateButton.setEnabled(canEdit && isSelected());
		this.openCustomerProfileButton.setEnabled(canEdit && isSelected());
	}

	private boolean isSelected() {
		return selectedRow != null;
	}

	@Override
	protected void populateControls() {
		this.addAssociateButton.addSelectionListener(this);
		this.editAssociateButton.addSelectionListener(this);
		this.removeAssociateButton.addSelectionListener(this);
		this.openCustomerProfileButton.addSelectionListener(this);
		this.mainPane.setControlModificationListener(listener);
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// Not used
	}

	/**
	 * Handle button push events.
	 * 
	 * @param event the selection event
	 */
	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		// Nothing to do
	}

	/**
	 * Handle button events on this page. Open dialog for add/remove customer group. Refresh the table if saved.
	 * 
	 * @param event the selection event
	 */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		int returnVal = 0;
		
		if (event.getSource() == this.addAssociateButton) {
			final AccountAddAssociationDialog dialog =
					new AccountAddAssociationDialog(this.getManagedForm().getForm().getShell(), this.account, this.selectedRow, true);
			returnVal = dialog.open();
		} else if (event.getSource() == this.editAssociateButton) {
			final AccountAddAssociationDialog dialog =
					new AccountAddAssociationDialog(this.getManagedForm().getForm().getShell(), this.account, this.selectedRow, false);
			returnVal = dialog.open();
			
		} else if (event.getSource() == this.removeAssociateButton && isSelected()) {
			final AccountRemoveAssociationDialog dialog =
					new AccountRemoveAssociationDialog(this.getManagedForm().getForm().getShell(), this.selectedRow);
			returnVal = dialog.open();
		} else if (event.getSource() == this.openCustomerProfileButton && isSelected()) {
			openCustomerProfile();
		} else {
			return;
		}
		
		if (returnVal == Window.OK) {
			tableViewer.refresh();
		}
	}

	private void openCustomerProfile() {
		final IEditorInput editorInput = new CustomerDetailsEditorInput(selectedRow.getCustomer().getUidPk());
		try {
			IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().
				getActivePage().openEditor(editorInput, CustomerDetailsEditor.ID_EDITOR);
			if (editorPart instanceof FormEditor) {
				((FormEditor) editorPart).setActivePage(CustomerDetailsProfilePage.PAGE_ID);						
			}
		} catch (final PartInitException e) {
			LOG.error("Can not open customer profile editor", e); //$NON-NLS-1$
		}
	}

	/**
	 * Content Provider class. Gets the Accounts Associates.
	 */
	class AssociatesTableContentProvider implements IStructuredContentProvider {
		/** serialVersionUID. */
		private static final long serialVersionUID = -8540637077401923943L;

		/**
		 * Return the addresses of customer as array.
		 * 
		 * @param element Customer input
		 * @return the addresses
		 */
		@Override
		public Object[] getElements(final Object element) {
			if (element instanceof Customer) {

				if (userAccountAssociationService == null) {
					userAccountAssociationService = BeanLocator.getSingletonBean("userAccountAssociationService",
							UserAccountAssociationService.class);
				}
				
				// look up associates of Customer
				Collection<UserAccountAssociation> associations = userAccountAssociationService.findAssociationsForAccount(account);
				
				List<AccountDetailsAssociatesRow> rows = new ArrayList<>();
				for (UserAccountAssociation userAccountAssociation : associations) {

					// TODO create a new query to do here - only existing bulk Customer lookup here is by UID and we have only GUIDs.
					Customer associate = customerService.findByGuid(userAccountAssociation.getUserGuid());
					if (!associate.getCustomerType().equals(CustomerType.SINGLE_SESSION_USER)) {
						rows.add(new AccountDetailsAssociatesRow(associate, userAccountAssociation));
					}
				}

				Object[] result = rows.toArray(new Object[rows.size()]);

				return result;
			}
			return null;
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
		 * @param viewer the view
		 * @param oldObject the old object
		 * @param newObject the new object
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldObject, final Object newObject) {
			// not needed
		}
	}

	/**
	 * Provides the columns for the Associates table.
	 */
	class AssociatesTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		/** serialVersionUID. */
		private static final long serialVersionUID = 2879260776457163226L;

		/**
		 * Get the column image.
		 * 
		 * @param element not used
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
		 * @param element the data input element
		 * @param columnIndex the column index
		 * @return text
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final AccountDetailsAssociatesRow row = (AccountDetailsAssociatesRow) element;

			switch (columnIndex) {
			case 0:
				return row.getCustomer().getFullName();
			case 1:
				return row.getCustomer().getEmail();
			case 2:
				return row.getAssociation().getAccountRole();
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Represents a row in the Account Associates table. Customer and UserAccountAssociation do not have a direct relationship, so this is a nice
	 * tidy way to tie them together for CM UI purposes.
	 */
	public class AccountDetailsAssociatesRow {
		
		private final Customer customer;

		private final UserAccountAssociation association;

		/**
		 * Constructor. 
		 * 
		 * @param customer the user
		 * @param association the association
		 */
		AccountDetailsAssociatesRow(final Customer customer, final UserAccountAssociation association) {
			this.customer = customer;
			this.association = association;
		}

		public Customer getCustomer() {
			return customer;
		}

		public UserAccountAssociation getAssociation() {
			return association;
		}
	}
}