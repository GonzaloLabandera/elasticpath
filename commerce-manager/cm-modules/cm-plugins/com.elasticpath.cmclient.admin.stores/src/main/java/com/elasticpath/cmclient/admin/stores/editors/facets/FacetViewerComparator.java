/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets;

import static com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable.DISPLAY_NAME_COLUMN_NUMBER;
import static com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable.FACET_GROUP_COLUMN_NUMBER;
import static com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable.FACET_NAME_COLUMN_NUMBER;
import static com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable.FACET_TYPE_COLUMN_NUMBER;
import static com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable.FIELD_TYPE_COLUMN_NUMBER;
import static com.elasticpath.cmclient.admin.stores.editors.facets.FacetTable.SEARCHABLE_COLUMN_NUMBER;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Logic for sorting columns.
 */
public class FacetViewerComparator extends ViewerComparator {
	private int columnIndex;
	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;
	private int direction = ASCENDING;

	/**
	 * Constructor.
	 */
	public FacetViewerComparator() {
		this.columnIndex = 0;
	}

	/**
	 * Get the sorting direction.
	 * @return SWT.UP or SWT.DOWN
	 */
	public int getDirection() {
		return direction == ASCENDING ? SWT.UP : SWT.DOWN;
	}

	/**
	 * Set the column selected by the user and switch the direction.
	 * @param columnIndex column index of the column selected
	 */
	public void setColumnIndex(final int columnIndex) {
		if (columnIndex == this.columnIndex) {
			direction ^= 1;
		} else {
			this.columnIndex = columnIndex;
			direction = ASCENDING;
		}
	}

	@Override
	public int compare(final Viewer viewer, final Object element, final Object otherElement) {
		FacetModel facetModel = (FacetModel) element;
		FacetModel otherFacetModel = (FacetModel) otherElement;
		int value;
		switch (columnIndex) {
			case FACET_NAME_COLUMN_NUMBER:
				value = facetModel.getFacetName().compareTo(otherFacetModel.getFacetName());
				break;
			case FACET_GROUP_COLUMN_NUMBER:
				value = facetModel.getFacetGroup().getName().compareTo(otherFacetModel.getFacetGroup().getName());
				break;
			case FIELD_TYPE_COLUMN_NUMBER:
				value = facetModel.getFieldKeyType().getName().compareTo(otherFacetModel.getFieldKeyType().getName());
				break;
			case SEARCHABLE_COLUMN_NUMBER:
				if (facetModel.isSearchable() == otherFacetModel.isSearchable()) {
					value = 0;
				} else {
					value = facetModel.isSearchable() ? 1 : -1;
				}
				break;
			case FACET_TYPE_COLUMN_NUMBER:
				value = facetModel.getFacetType().compareTo(otherFacetModel.getFacetType());
				break;
			case DISPLAY_NAME_COLUMN_NUMBER:
				value = facetModel.getDefaultDisplayName().compareTo(otherFacetModel.getDefaultDisplayName());
				break;
			default:
				value = 0;
				break;
		}
		return direction == DESCENDING ? -value : value;
	}
}