/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets.editingsupport;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;

import com.elasticpath.cmclient.admin.stores.editors.facets.FacetModel;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;

/**
 * Editing Support for searchable check box.
 */
public class SearchableCheckboxEditingSupport extends EditingSupport {

	private final IEpTableViewer tableViewer;
	private final AbstractCmClientFormEditor editor;

	/**
	 * Constructor.
	 * @param tableViewer table viewer
	 * @param editor editor
	 */
	public SearchableCheckboxEditingSupport(final IEpTableViewer tableViewer, final AbstractCmClientFormEditor editor) {
		super(tableViewer.getSwtTableViewer());
		this.tableViewer = tableViewer;
		this.editor = editor;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		return new CheckboxCellEditor((Composite) tableViewer.getSwtTableViewer().getControl());
	}

	@Override
	protected boolean canEdit(final Object element) {
		return true;
	}

	@Override
	protected Object getValue(final Object element) {
		return ((FacetModel) element).isSearchable();
	}

	@Override
	protected void setValue(final Object element, final Object value) {
		boolean selected = (boolean) value;
		FacetModel facetModel = (FacetModel) element;
		facetModel.setSearchable(selected);

		tableViewer.getSwtTableViewer().refresh();
		editor.controlModified();
	}
}