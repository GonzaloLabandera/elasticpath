package com.elasticpath.cmclient.admin.stores.editors.facets;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter used for filtering facet models by their field group.
 */
public class FieldGroupFilter extends ViewerFilter {
	private String attributeType;

	public void setAttributeType(final String attributeType) {
		this.attributeType = attributeType;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		return attributeType == null ||  attributeType.equals("All") || attributeType.equals(((FacetModel) element).getFacetGroup().getName());
	}
}
