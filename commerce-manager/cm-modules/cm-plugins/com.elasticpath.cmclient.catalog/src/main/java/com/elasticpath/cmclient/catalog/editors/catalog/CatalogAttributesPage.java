/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.catalog.editors.catalog;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.elasticpath.cmclient.catalog.CatalogPlugin;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.IManagedForm;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPage;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.editors.TableItems;
import com.elasticpath.cmclient.core.editors.TableReloadMarker;
import com.elasticpath.domain.attribute.Attribute;

/**
 * Implementation of the <code>CatalogEditor</code> attributes page providing attributes
 * associated with a catalog.
 */
public class CatalogAttributesPage extends AbstractCmClientEditorPage implements Observer {

	private List<Attribute> catalogAttributes;

	private CatalogAttributesSection catalogAttributeSection;

	private boolean reloadModel;

	/**
	 * Default constructor.
	 * 
	 * @param editor the editor this <code>FormPage</code> is apart of
	 */
	public CatalogAttributesPage(final AbstractCmClientFormEditor editor) {
		super(editor, "catalogAttributes", CatalogMessages.get().CatalogAttributesPage_Title, true); //$NON-NLS-1$
	}

	@Override
	protected void addEditorSections(final AbstractCmClientFormEditor editor, final IManagedForm managedForm) {
		if (isNotSaved() && !reloadModel) {
			catalogAttributeSection = new CatalogAttributesSection(this, editor, catalogAttributes);
		} else {
			catalogAttributeSection = new CatalogAttributesSection(this, editor);
			reloadModel = false;
		}
		managedForm.addPart(catalogAttributeSection);
		getCustomPageData().put("catalogAttributes", this.catalogAttributes);
		addExtensionEditorSections(editor, managedForm, CatalogPlugin.PLUGIN_ID, getClass().getSimpleName());
		if (isNotSaved()) {
			catalogAttributeSection.markDirty();
		}
	}

	private boolean isNotSaved() {
		TableItems<Attribute> attributeTableItems = ((CatalogEditor) getEditor()).getModel().getAttributeTableItems();
		return attributeTableItems != null && !attributeTableItems.isAllEmpty();
	}

	@Override
	protected int getFormColumnsCount() {
		return 1;
	}

	@Override
	protected String getFormTitle() {
		return CatalogMessages.get().CatalogAttributesPage_Form_Title;
	}

	@Override
	protected void addToolbarActions(final IToolBarManager toolBarManager) {
		// Empty for now
	}
	
	@Override
	protected Layout getLayout() {
		return new GridLayout(getFormColumnsCount(), false);
	}

	@Override
	public void update(final Observable observable, final Object catalogAttributes) {
		// Needed for refresh to work.
		this.catalogAttributes = (List<Attribute>) catalogAttributes;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		if (clazz == TableReloadMarker.class) {
			return new MyTableMarker();
		}
		return super.getAdapter(clazz);
	}

	/**
	 * Implementation for this table reload marker.
	 */
	class MyTableMarker implements TableReloadMarker {

		@Override
		public void markForReload() {
			reloadModel = true;
		}

	}
}