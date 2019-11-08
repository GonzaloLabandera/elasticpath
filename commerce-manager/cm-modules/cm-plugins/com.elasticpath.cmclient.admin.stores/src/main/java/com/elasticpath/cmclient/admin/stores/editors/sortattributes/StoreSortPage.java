/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.admin.stores.AdminStoresPlugin;
import com.elasticpath.cmclient.admin.stores.editors.AbstractStorePage;
import com.elasticpath.cmclient.core.dialog.value.dialog.ConfirmationDialog;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.domain.catalog.Catalog;

/**
 * This page allows users to configure sort attributes for a store.
 */
public class StoreSortPage extends AbstractStorePage {

	private static final String SORT_CONFIGURATION_PAGE = "sortConfigurationPage";

	private final StoreEditorModel storeEditorModel;

	/**
	 * Constructor.
	 * @param editor editor
	 * @param authorized authorized
	 * @param storeEditorModel store model
	 */
	public StoreSortPage(final AbstractCmClientFormEditor editor, final boolean authorized, final StoreEditorModel storeEditorModel) {
		super(editor, SORT_CONFIGURATION_PAGE, AdminStoresMessages.get().StoreSortAttributeConfiguration, authorized);
		this.storeEditorModel = storeEditorModel;
	}

	@Override
	protected void addEditorSections(final AbstractCmClientFormEditor editor, final IManagedForm managedForm) {
		Locale defaultLocale = storeEditorModel.getDefaultLocale();
		Catalog catalog = storeEditorModel.getCatalog();
		String storeCode = storeEditorModel.getStoreCode();
		Collection<Locale> supportedLocales = storeEditorModel.getSupportedLocales();
		if (defaultLocale == null || catalog == null || storeCode == null || supportedLocales == null) {
			new ConfirmationDialog(Display.getCurrent().getActiveShell(), AdminStoresMessages.get().StoreSortAttributeErrorDialogHeader,
					AdminStoresMessages.get().StoreSortAttributeErrorDialogMessage).open();
		} else {
			managedForm.addPart(new SortAttributeTable(this, editor, ExpandableComposite.TITLE_BAR, storeEditorModel));
			addExtensionEditorSections(editor, managedForm, AdminStoresPlugin.PLUGIN_ID, this.getClass().getSimpleName());
		}
	}

	@Override
	protected int getFormColumnsCount() {
		return 1;
	}

	@Override
	protected String getFormTitle() {
		return AdminStoresMessages.get().StoreSortAttributeConfiguration;
	}
}
