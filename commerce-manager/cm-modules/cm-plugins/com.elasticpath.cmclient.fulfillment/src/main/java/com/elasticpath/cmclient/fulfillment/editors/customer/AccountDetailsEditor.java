/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.customer;

import org.apache.log4j.Logger;
import org.eclipse.ui.PartInitException;

import com.elasticpath.cmclient.core.service.AuthorizationService;
import com.elasticpath.cmclient.fulfillment.FulfillmentPermissions;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;

/**
 * Account details editor.
 */
public class AccountDetailsEditor extends CustomerDetailsEditor {
	
	/** serialVersionUID. */
	private static final long serialVersionUID = -6359555460519610826L;

	/**
	 * ID of the editor. It is the same as the class name.
	 */
	public static final String ID_EDITOR = AccountDetailsEditor.class.getName();

	private static final Logger LOG = Logger.getLogger(AccountDetailsEditor.class);

	@Override
	protected void addPages() {
		try {
			this.addPage(new CustomerDetailsProfilePage(this));
			this.addPage(new CustomerDetailsAdressesPage(this));
			this.addPage(new AccountChildAccountsPage(this));
			this.addPage(new AccountDetailsAssociatesPage(this));
			this.addPage(new CustomerDetailsOrdersPage(this));
			// Only show the customer segments tab if the user has permission.
			if (AuthorizationService.getInstance().isAuthorizedWithPermission(FulfillmentPermissions.ASSIGN_CUSTOMER_SEGMENTS)) {
				this.addPage(new CustomerDetailsCustomerSegmentsPage(this));
			}

			addExtensionPages(getClass().getSimpleName(), FulfillmentPlugin.PLUGIN_ID);
		} catch (final PartInitException e) {
			LOG.error("Can not create pages for the Customer editor", e); //$NON-NLS-1$
		}
	}
}
