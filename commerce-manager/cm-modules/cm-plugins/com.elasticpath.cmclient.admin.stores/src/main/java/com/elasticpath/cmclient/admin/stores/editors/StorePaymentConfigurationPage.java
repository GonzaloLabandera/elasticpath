/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors;

import org.eclipse.ui.forms.IManagedForm;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.admin.stores.AdminStoresPlugin;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;

/**
 * Represent store customer profile attributes tab.
 */
public class StorePaymentConfigurationPage extends AbstractStorePage {

	private final StoreEditorModel storeEditorModel;
	/**
	 * Constructs the store customer attribute policies page.
	 *
	 * @param editor     the editor
	 * @param authorized whether the current user is authorized to edit the current store
	 * @param storeEditorModel {@link StoreEditorModel} for which the editor is opened
	 */
	public StorePaymentConfigurationPage(final AbstractCmClientFormEditor editor, final boolean authorized, final StoreEditorModel storeEditorModel) {
		super(editor, "StorePaymentConfigurationPage", AdminStoresMessages.get().StorePaymentProviderConfigurations, authorized);
		this.storeEditorModel = storeEditorModel;
	}

	@Override
	protected void addEditorSections(final AbstractCmClientFormEditor editor, final IManagedForm managedForm) {
		managedForm.addPart(new StorePaymentConfigurationSectionPart(this, editor, storeEditorModel));
		getCustomPageData().put("isEditable", isEditable());
		addExtensionEditorSections(editor, managedForm, AdminStoresPlugin.PLUGIN_ID, this.getClass().getSimpleName());
	}

	@Override
	protected int getFormColumnsCount() {
		return 1;
	}

	@Override
	protected String getFormTitle() {
		return AdminStoresMessages.get().StorePaymentProviderConfigurations;
	}
}
