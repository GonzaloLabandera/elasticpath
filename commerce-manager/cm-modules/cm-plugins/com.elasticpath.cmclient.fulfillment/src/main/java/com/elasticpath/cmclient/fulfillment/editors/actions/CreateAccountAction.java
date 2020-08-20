/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.wizards.account.CreateAccountWizard;

/**
 * Create Account action.
 */
public class CreateAccountAction extends Action {

	private static final int CONTENT_WIDTH_HINT = 700;
	private static final int CONTENT_HEIGHT_HINT = 400;

	private final Shell shell;

	/**
	 * Constructor.
	 */
	public CreateAccountAction() {
		super(FulfillmentMessages.get().CreateAccountWizard_AddAccount_Label, FulfillmentImageRegistry.ADD_ACCOUNT_ICON);
		this.shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	@Override
	public void run() {
		// null Customer object to signal that this is a new root-level account that is being created.
		final CreateAccountWizard createAccountWizard = new CreateAccountWizard(FulfillmentMessages.get().CreateAccountWizard_AddNewAccount_Title,
				null);
		final WizardDialog dialog = new EpWizardDialog(shell, createAccountWizard);
		dialog.setPageSize(CONTENT_WIDTH_HINT, CONTENT_HEIGHT_HINT);
		dialog.addPageChangingListener(createAccountWizard);

		dialog.open();
	}
}
