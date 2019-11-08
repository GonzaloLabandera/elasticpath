/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;

import com.elasticpath.domain.search.SortAttribute;

/**
 * Defines the logic when clicking the radio buttons in default attribute column.
 */
public class DefaultAttributeEditingSupport extends EditingSupport {

	private final SortAttributeTable sortAttributeTable;

	/**
	 * Constructor.
	 * @param sortAttributeTable table
	 */
	public DefaultAttributeEditingSupport(final SortAttributeTable sortAttributeTable) {
		super(sortAttributeTable.getTable().getSwtTableViewer());
		this.sortAttributeTable = sortAttributeTable;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		return new CheckboxCellEditor((Composite) sortAttributeTable.getTable().getSwtTableViewer().getControl());
	}

	@Override
	protected boolean canEdit(final Object element) {
		return true;
	}

	@Override
	protected Object getValue(final Object element) {
		return ((SortAttribute) element).isDefaultAttribute();
	}

	@Override
	protected void setValue(final Object element, final Object value) {
		sortAttributeTable.setDefaultSortAttribute((SortAttribute) element);
	}
}
