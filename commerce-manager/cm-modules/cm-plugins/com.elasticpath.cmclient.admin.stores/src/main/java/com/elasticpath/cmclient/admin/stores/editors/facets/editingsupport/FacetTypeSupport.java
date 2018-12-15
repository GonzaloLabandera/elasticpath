/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets.editingsupport;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.elasticpath.cmclient.admin.stores.editors.facets.FacetModel;
import com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.domain.search.FacetType;

/**
 * Editing Support facet type.
 */
public class FacetTypeSupport extends EditingSupport {

	private final TableViewer tableViewer;
	private final AbstractCmClientFormEditor editor;
	private final Set<String> defaultProductAttributes;
	private final Button editButton;
	private final FacetTable facetTable;

	/**
	 * Constructor.
	 * @param tableViewer tableViewer
	 * @param defaultProductAttributes product attributes to ignore
	 * @param editButton edit button to enable or disable based on facet type
	 * @param facetTable facet table
	 */
	public FacetTypeSupport(final TableViewer tableViewer, final Set<String> defaultProductAttributes,
							final Button editButton, final FacetTable facetTable) {
		super(tableViewer);
		this.tableViewer = tableViewer;
		this.editor = facetTable.getEditor();
		this.defaultProductAttributes = defaultProductAttributes;
		this.editButton = editButton;
		this.facetTable = facetTable;
	}



	@Override
	protected CellEditor getCellEditor(final Object element) {
		FacetModel facetModel = (FacetModel) element;
		List<FacetType> facetTypes = FacetType.getFacetTypesForFieldKeyType(facetModel.getFieldKeyType());
		int length = facetTypes.size();
		String[] values = new String[length];
		for (int index = 0; index < length; index++) {
			values[index] = facetTypes.get(index).getName();
		}

		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor((Composite) tableViewer.getControl(), values,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		CCombo cCombo = (CCombo) comboBoxCellEditor.getControl();

		cCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent selectionEvent) {
				FacetType facetType = FacetType.getFacetTypeForName(cCombo.getItem(cCombo.getSelectionIndex()));
				if (facetType == FacetType.NO_FACET) {
					editButton.setEnabled(false);
				} else {
					editButton.setEnabled(true);
				}

				facetModel.updateDefaultValuesInMaps();
				facetModel.setFacetType(facetType);

				tableViewer.refresh();
				editor.controlModified();
				facetTable.markDirty();
			}
		});
		return comboBoxCellEditor;
	}

	@Override
	protected boolean canEdit(final Object element) {
		return !defaultProductAttributes.contains(((FacetModel) element).getFacetName());
	}

	@Override
	protected Object getValue(final Object element) {
		return ((FacetModel) element).getFacetType().getOrdinal();
	}

	@Override
	protected void setValue(final Object element, final Object value) {
		// we set the value in the listener instead because this only gets called when the dropdown is out of focus
	}
}