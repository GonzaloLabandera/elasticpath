/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors;

import org.eclipse.ui.forms.IManagedForm;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.admin.stores.AdminStoresPlugin;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;

/**
 * Represent store customer profile attributes tab.
 */
public class StoreCustomerAttributePoliciesPage extends AbstractStorePage {

	/**
	 * Constructs the store customer attribute policies page.
	 *
	 * @param editor     the editor
	 * @param authorized whether the current user is authorized to edit the current store
	 */
	public StoreCustomerAttributePoliciesPage(final AbstractCmClientFormEditor editor, final boolean authorized) {
		super(editor, "StoreCustomerAttributePoliciesPage", AdminStoresMessages.get().StoreProfileAttributePolicies, authorized);
	}

	@Override
	protected void addEditorSections(final AbstractCmClientFormEditor editor, final IManagedForm managedForm) {
		managedForm.addPart(new StoreCustomerAttributePoliciesSectionPart(this, editor, isEditable()));
		getCustomPageData().put("isEditable", isEditable());
		addExtensionEditorSections(editor, managedForm, AdminStoresPlugin.PLUGIN_ID, this.getClass().getSimpleName());
	}

	@Override
	protected int getFormColumnsCount() {
		return 1;
	}

	@Override
	protected String getFormTitle() {
		return AdminStoresMessages.get().StoreProfileAttributePolicies;
	}
}
