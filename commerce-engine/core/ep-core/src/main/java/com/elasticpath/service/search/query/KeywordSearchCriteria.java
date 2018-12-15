/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.search.query;

import java.util.Objects;

/**
 * Keyword search criteria.
 */
public class KeywordSearchCriteria extends AbstractProductCategorySearchCriteria {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	private String keyword;

	private Long categoryUid;

	private boolean offerSearch;

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		KeywordSearchCriteria that = (KeywordSearchCriteria) other;
		return offerSearch == that.offerSearch && Objects.equals(keyword, that.keyword) && Objects.equals(categoryUid, that.categoryUid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keyword, categoryUid, offerSearch);
	}

	/**
	 * Gets the keyword to search for.
	 *
	 * @return the keyword to search for
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * Sets the keyword to search for.
	 *
	 * @param keyword the keyword to search for
	 */
	public void setKeyword(final String keyword) {
		this.keyword = keyword;
	}

	/**
	 * Gets the category UID to search for.
	 *
	 * @return the category UID to search for
	 */
	@Override
	public Long getCategoryUid() {
		return categoryUid;
	}

	/**
	 * Sets the category UID to search for.
	 *
	 * @param categoryUid the category UID to search for
	 */
	public void setCategoryUid(final Long categoryUid) {
		this.categoryUid = categoryUid;
	}

	/**
	 * Checks if search criteria is offer search.
	 * @return true if offer search
	 */
	public boolean isOfferSearch() {
		return offerSearch;
	}

	/**
	 * Sets the search criteria to offer search.
	 * @param offerSearch offer search
	 */
	public void setOfferSearch(final boolean offerSearch) {
		this.offerSearch = offerSearch;
	}

	@Override
	public void optimizeInternal() {
		if (!isStringValid(keyword)) {
			keyword = null;
		}
	}
}
