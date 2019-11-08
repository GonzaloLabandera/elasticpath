/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets;

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
 * The Facet configuration page for a store.
 */
public class StoreFacetsPage extends AbstractStorePage {

	private static final String FACET_CONFIGURATION_PAGE = "facetConfigurationPage";

	private final StoreEditorModel storeEditorModel;

	/**
	 * Constructor.
	 * @param editor editor
	 * @param authorized authorized
	 * @param storeEditorModel storeEditorModel
	 */
	public StoreFacetsPage(final AbstractCmClientFormEditor editor, final boolean authorized, final StoreEditorModel storeEditorModel) {
		super(editor, FACET_CONFIGURATION_PAGE, AdminStoresMessages.get().StoreFacetConfiguration, authorized);
		this.storeEditorModel = storeEditorModel;
	}

	@Override
	protected void addEditorSections(final AbstractCmClientFormEditor abstractCmClientFormEditor, final IManagedForm iManagedForm) {
		Locale defaultLocale = storeEditorModel.getDefaultLocale();
		Catalog catalog = storeEditorModel.getCatalog();
		String storeCode = storeEditorModel.getStoreCode();
		Collection<Locale> supportedLocales = storeEditorModel.getSupportedLocales();
		if (defaultLocale == null || catalog == null || storeCode == null || supportedLocales == null) {
			new ConfirmationDialog(Display.getCurrent().getActiveShell(), AdminStoresMessages.get().StoreFacetErrorDialogHeader,
					AdminStoresMessages.get().StoreFacetErrorDialogMessage).open();
		} else {
			iManagedForm.addPart(new FacetTable(this, abstractCmClientFormEditor, ExpandableComposite.TITLE_BAR, storeEditorModel));
			addExtensionEditorSections(abstractCmClientFormEditor, iManagedForm, AdminStoresPlugin.PLUGIN_ID, this.getClass().getSimpleName());
		}
	}

	@Override
	protected int getFormColumnsCount() {
		return 1;
	}

	@Override
	protected String getFormTitle() {
		return AdminStoresMessages.get().StoreFacetConfiguration;
	}
}
