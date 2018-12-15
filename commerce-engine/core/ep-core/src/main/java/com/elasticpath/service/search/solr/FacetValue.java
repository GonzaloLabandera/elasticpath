/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.search.solr;

/**
 * Class for holding relevant facet information.
 */
public class FacetValue {
	private String displayName;
	private String facetFilter;
	private String count;

	/**
	 * Constructor.
	 * @param displayName display name
	 * @param facetFilter facet filter
	 * @param count count
	 */
	public FacetValue(final String displayName, final String facetFilter, final String count) {
		this.displayName = displayName;
		this.facetFilter = facetFilter;
		this.count = count;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public String getFacetFilter() {
		return facetFilter;
	}

	public void setFacetFilter(final String facetFilter) {
		this.facetFilter = facetFilter;
	}

	public String getCount() {
		return count;
	}

	public void setCount(final String count) {
		this.count = count;
	}
}