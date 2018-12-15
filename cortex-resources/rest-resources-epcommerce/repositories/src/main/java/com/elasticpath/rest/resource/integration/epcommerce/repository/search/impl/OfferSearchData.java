/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import java.util.Map;
import java.util.Objects;

/**
 * Offer Search data.
 */
public final class OfferSearchData {

	private final int pageId;
	private final int pageSize;
	private final String searchKeyword;
	private final String scope;
	private final Map<String, String> appliedFacets;

	/**
	 * Constructor.
	 *
	 * @param pageId        page id
	 * @param pageSize      page size
	 * @param searchKeyword search keyword
	 * @param scope         scope
	 * @param appliedFacets applied facets
	 */
	public OfferSearchData(final int pageId, final int pageSize, final String searchKeyword, final String scope,
						   final Map<String, String> appliedFacets) {
		this.pageId = pageId;
		this.pageSize = pageSize;
		this.searchKeyword = searchKeyword;
		this.scope = scope;
		this.appliedFacets = appliedFacets;
	}


	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		OfferSearchData that = (OfferSearchData) other;
		return pageId == that.pageId
				&& pageSize == that.pageSize
				&& areObjectsEqual(that);
	}

	private boolean areObjectsEqual(final OfferSearchData that) {
		return Objects.equals(searchKeyword, that.searchKeyword)
				&& Objects.equals(scope, that.scope)
				&& objectsDeepEquals(that);
	}

	private boolean objectsDeepEquals(final OfferSearchData that) {
		return Objects.deepEquals(appliedFacets.keySet().toArray(), that.appliedFacets.keySet().toArray())
				&& Objects.deepEquals(appliedFacets.values().toArray(), that.appliedFacets.values().toArray());
	}

	@Override
	public int hashCode() {
		return Objects.hash(pageId, pageSize, searchKeyword, scope, appliedFacets);
	}


	public String getSearchKeyword() {
		return searchKeyword;
	}

	public int getPageSize() {
		return pageSize;
	}

	public String getScope() {
		return scope;
	}

	public int getPageId() {
		return pageId;
	}

	public Map<String, String> getAppliedFacets() {
		return appliedFacets;
	}
}

