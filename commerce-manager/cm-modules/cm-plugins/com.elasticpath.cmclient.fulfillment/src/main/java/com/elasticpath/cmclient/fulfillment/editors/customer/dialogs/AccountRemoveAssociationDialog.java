/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cmclient.fulfillment.editors.customer.dialogs;

import org.eclipse.jface.dialogs.IDialogLabelKeys;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.customer.AccountDetailsAssociatesSection.AccountDetailsAssociatesRow;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Confirmation dialog when removing an association between an account and a user.
 */
public class AccountRemoveAssociationDialog extends MessageDialog {

	/** serialVersionUID. */
	private static final long serialVersionUID = -2672251505789573331L;

	private static final String[] BUTTONS = { 
			JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY),
			JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY) };

	private static final int BUTTON_INDEX_OK = 0;

	private static UserAccountAssociationService userAccountAssociationService;

	private final AccountDetailsAssociatesRow selectedRow;

	/**
	 * Dialog for confirmation to remove a user from an account.
	 *
	 * @param parentShell the parent shell
	 * @param selectedRow the selected row
	 */
	public AccountRemoveAssociationDialog(final Shell parentShell, final AccountDetailsAssociatesRow selectedRow) {
		super(parentShell, FulfillmentMessages.get().AssociatesPageDialog_RemoveConfirm, null,
				FulfillmentMessages.get().AssociatesPageDialog_RemoveMessage, QUESTION, BUTTONS, 0);
		this.selectedRow = selectedRow;
	}


	/**
	 * Window will configure the shell after construction. Set things like Image or Title.
	 *
	 * @param newShell the shell
	 */
	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(FulfillmentImageRegistry.CUSTOMER_SEGMENT_ICON.createImage());
	}
	/**
	 * Open the dialog box, and remove the association if OK is pressed.
	 *
	 * @return result button selection
	 */
	@Override
	public int open() {
		message = FulfillmentMessages.get().AssociatesPageDialog_RemoveMessage + "\n\n" //$NON-NLS-1$
				+ selectedRow.getCustomer().getEmail();

		final int result = super.open();
		if (result == BUTTON_INDEX_OK) {
			if (userAccountAssociationService == null) {
				userAccountAssociationService = BeanLocator.getSingletonBean(ContextIdNames.USER_ACCOUNT_ASSOCIATION_SERVICE,
						UserAccountAssociationService.class);
			}
			userAccountAssociationService.remove(selectedRow.getAssociation());
		}
		return result;
	}

}
