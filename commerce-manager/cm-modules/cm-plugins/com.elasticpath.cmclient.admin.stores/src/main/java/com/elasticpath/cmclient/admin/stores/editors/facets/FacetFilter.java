/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filtering the table by field key.
 */
public class FacetFilter extends ViewerFilter {

	private String filterText;

	void setFilterText(final String filterText) {
		this.filterText = filterText;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		return filterText == null || filterText.length() == 0 || StringUtils.containsIgnoreCase(((FacetModel) element).getFacetName(),
				filterText);
	}
}