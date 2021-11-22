/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import static com.elasticpath.cmclient.admin.stores.editors.sortattributes.SortAttributeTable.SORT_ATTIBUTE_DISPLAY_NAME_COLUMN;
import static com.elasticpath.cmclient.admin.stores.editors.sortattributes.SortAttributeTable.SORT_ATTIBUTE_GROUP_COLUMN;
import static com.elasticpath.cmclient.admin.stores.editors.sortattributes.SortAttributeTable.SORT_ATTRIBUTE_DIRECTION_COLUMN;
import static com.elasticpath.cmclient.admin.stores.editors.sortattributes.SortAttributeTable.SORT_ATTRIBUTE_KEY_COLUMN;
import static com.elasticpath.cmclient.admin.stores.editors.sortattributes.SortAttributeTable.SORT_ATTRIBUTE_TYPE_COLUMN;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortLocalizedName;

/**
 * Comparator for sorting table columns.
 */
public class SortAttributeViewerComparator extends ViewerComparator {

	private int columnIndex;
	private final String defaultLocaleCode;
	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;
	private int direction = ASCENDING;

	/**
	 * Constructor.
	 * @param defaultLocaleCode default locale code
	 */
	public SortAttributeViewerComparator(final String defaultLocaleCode) {
		this.columnIndex = 0;
		this.defaultLocaleCode = defaultLocaleCode;
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
		SortAttribute sortAttribute = (SortAttribute) element;
		SortAttribute otherSortAttribute = (SortAttribute) otherElement;
		int value;
		switch (columnIndex) {
			case SORT_ATTRIBUTE_KEY_COLUMN:
				value = sortAttribute.getBusinessObjectId().compareTo(otherSortAttribute.getBusinessObjectId());
				break;
			case SORT_ATTRIBUTE_TYPE_COLUMN:
				value = sortAttribute.getSortAttributeType().getName().compareTo(otherSortAttribute.getSortAttributeType().getName());
				break;
			case SORT_ATTIBUTE_GROUP_COLUMN:
				value = sortAttribute.getSortAttributeGroup().getName().compareTo(otherSortAttribute.getSortAttributeGroup().getName());
				break;
			case SORT_ATTRIBUTE_DIRECTION_COLUMN:
				if (sortAttribute.isDescending() == otherSortAttribute.isDescending()) {
					value = 0;
				} else {
					value = sortAttribute.isDescending() ? 1 : -1;
				}
				break;
			case SORT_ATTIBUTE_DISPLAY_NAME_COLUMN:
				SortLocalizedName empty = BeanLocator.getPrototypeBean(ContextIdNames.SORT_LOCALIZED_NAME, SortLocalizedName.class);
				empty.setName(StringUtils.EMPTY);
				value = sortAttribute.getLocalizedNames().getOrDefault(defaultLocaleCode, empty).getName()
						.compareTo(otherSortAttribute.getLocalizedNames().getOrDefault(defaultLocaleCode, empty).getName());
				break;
			default:
				value = 0;
				break;
		}
		return direction == DESCENDING ? -value : value;
	}
}
