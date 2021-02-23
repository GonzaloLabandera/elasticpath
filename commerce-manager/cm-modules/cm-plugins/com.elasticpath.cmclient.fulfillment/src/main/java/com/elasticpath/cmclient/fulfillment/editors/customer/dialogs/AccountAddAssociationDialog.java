/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cmclient.fulfillment.editors.customer.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;
import com.elasticpath.cmclient.fulfillment.editors.customer.AccountDetailsAssociatesSection.AccountDetailsAssociatesRow;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * Account Add Association Dialog.
 */
@SuppressWarnings("restriction")
public class AccountAddAssociationDialog extends AbstractEpDialog {

	/** serialVersionUID. */
	private static final long serialVersionUID = 4453852539023821553L;

	private final Customer account;
	
	private final AccountDetailsAssociatesRow selectedRow;

	private CustomerService customerService;
	
	private UserAccountAssociationService userAccountAssociationService;

	private transient Text userFullNameTextField;
	
	private transient Text userEmailTextField;

	private transient CCombo roleCombo;
	
	private transient String selectedShopperRole;

	private final DataBindingContext bindingContext;

	private final boolean isAdd;

	/**
	 * Constructs the dialog.
	 *
	 * @param parentShell the parent Shell
	 * @param account the account
	 * @param selectedRow the selected row
	 * @param isAdd if the dialog is for adding
	 */
	public AccountAddAssociationDialog(final Shell parentShell, final Customer account, final AccountDetailsAssociatesRow selectedRow,
			final boolean isAdd) {
		super(parentShell, 2, false);
		this.account = account;
		this.selectedRow = selectedRow;
		this.isAdd = isAdd;

		this.bindingContext = new DataBindingContext();
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createEpButtonsForButtonsBar(ButtonsBarType.SAVE, parent);
	}

	@Override
	protected String getPluginId() {
		return FulfillmentPlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return Arrays.asList(account);
	}

	@Override
	protected void populateControls() {

		if (isAdd) {
			return;
		}

		userFullNameTextField.setText(selectedRow.getCustomer().getFullName());
		userEmailTextField.setText(selectedRow.getCustomer().getEmail());
		roleCombo.setText(selectedRow.getAssociation().getAccountRole());
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {

		final IEpLayoutData labelData = dialogComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL);
		final IEpLayoutData fieldData = dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false);

		EpState customerInfoState;
		if (isAdd) {
			customerInfoState = EpState.EDITABLE;
		} else {
			customerInfoState = EpState.READ_ONLY;

			dialogComposite.addLabelBoldRequired(FulfillmentMessages.get().AssociatesPage_UserFullName, null, labelData);
			userFullNameTextField = dialogComposite.addTextField(customerInfoState, fieldData);
		}

		dialogComposite.addLabelBoldRequired(FulfillmentMessages.get().AssociatesPage_UserEmail, null, labelData);
		userEmailTextField = dialogComposite.addTextField(customerInfoState, fieldData);

		dialogComposite.addLabelBoldRequired(FulfillmentMessages.get().AssociatesPage_Role, null, labelData);
		roleCombo = dialogComposite.addComboBox(null, fieldData);

		roleCombo.setItems(getRoleToPermissions().getDefinedRoleKeys().toArray(new String[0]));
		roleCombo.select(0);
	}

	@Override
	protected void bindControls() {
		final boolean hideDecorationOnFirstValidation = true;

		final ObservableUpdateValueStrategy roleUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				selectedShopperRole = roleCombo.getText();
				return Status.OK_STATUS;
			}
		};

		EpControlBindingProvider.getInstance().bind(bindingContext, roleCombo, null, null, roleUpdateStrategy, hideDecorationOnFirstValidation);

		EpDialogSupport.create(this, bindingContext);
	}

	@Override
	protected void okPressed() {

		if (userAccountAssociationService == null) {
			userAccountAssociationService = BeanLocator.getSingletonBean(ContextIdNames.USER_ACCOUNT_ASSOCIATION_SERVICE,
					UserAccountAssociationService.class);
		}

		if (isAdd) {
			boolean newAssociationCreated = createNewAssociation();

			if (!newAssociationCreated) {
				// if the association has not been created, we do not want to close the AccountAddAssociationDialog
				return;
			}

		} else {
			UserAccountAssociation association = (UserAccountAssociation) userAccountAssociationService
					.getObject(selectedRow.getAssociation().getUidPk());

			association.setAccountRole(selectedShopperRole);
			userAccountAssociationService.update(association);
		}

		super.okPressed();
	}

	private boolean createNewAssociation() {

		String userEmail = userEmailTextField.getText();
		List<Customer> customers = getCustomerService().findCustomersByProfileAttributeKeyAndValue(CustomerImpl.ATT_KEY_CP_EMAIL, userEmail);

		// verify customer exists
		if (customers.isEmpty()) {
			Status status = new Status(IStatus.ERROR, FulfillmentPlugin.PLUGIN_ID, FulfillmentMessages.get().AssociatesDialog_NoCustomerFoundError);
			ErrorDialog.openError(new Shell(Display.getCurrent()), FulfillmentMessages.get().Error_Title, null, status);
			return false;
		}
		Customer newCustomerToAssociate = customers.get(0);

		// check for duplicate association
		boolean associationExists = userAccountAssociationService.isExistingUserAssociation(account.getGuid(), newCustomerToAssociate.getGuid());
		if (associationExists) {
			Status status = new Status(IStatus.ERROR, FulfillmentPlugin.PLUGIN_ID,
					FulfillmentMessages.get().AssociatesDialog_DuplicateCustomerError);
			ErrorDialog.openError(new Shell(Display.getCurrent()), FulfillmentMessages.get().Error_Title, null, status);
			return false;
		}

		UserAccountAssociation userAccountAssociation = BeanLocator.getPrototypeBean(ContextIdNames.USER_ACCOUNT_ASSOCIATION,
				UserAccountAssociation.class);

		userAccountAssociation.setAccountGuid(account.getGuid());
		userAccountAssociation.setUserGuid(newCustomerToAssociate.getGuid());
		userAccountAssociation.setAccountRole(selectedShopperRole);

		userAccountAssociationService.add(userAccountAssociation);
		
		return true;
	}

	@Override
	protected String getTitle() {
		if (isAdd) {
			return FulfillmentMessages.get().AssociatesDialog_AddAssociateTitle;
		} else {
			return FulfillmentMessages.get().AssociatesDialog_EditAssociateTitle;
		}
	}

	@Override
	protected String getWindowTitle() {
		if (isAdd) {
			return FulfillmentMessages.get().AssociatesDialog_AddAssociateTitle;
		} else {
			return FulfillmentMessages.get().AssociatesDialog_EditAssociateTitle;
		}
	}

	@Override
	protected Image getWindowImage() {
		return null;
	}

	@Override
	protected String getInitialMessage() {
		return null;
	}
	
	private CustomerService getCustomerService() {
		if (customerService == null) {
			customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
		}
		
		return customerService;
	}

	private RoleToPermissionsMappingService getRoleToPermissions() {
		return BeanLocator.getSingletonBean(ContextIdNames.ROLE_TO_PERMISSION_MAPPING_SERVICE, RoleToPermissionsMappingService.class);
	}
}
