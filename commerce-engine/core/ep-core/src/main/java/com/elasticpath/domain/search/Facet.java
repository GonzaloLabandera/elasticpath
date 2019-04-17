/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search;

import java.util.Map;
import java.util.SortedSet;

import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.persistence.api.Persistable;

/**
 * A facet.
 */
public interface Facet extends Persistable {

	/**
	 * Gets the display name.
	 *
	 * @return the display name.
	 */
	String getDisplayName();

	/**
	 * Sets the display name.
	 *
	 * @param displayName the display name.
	 */
	void setDisplayName(String displayName);

	/**
	 * Gets the facet name.
	 *
	 * @return the facet name.
	 */
	String getFacetName();

	/**
	 * Sets the facet name.
	 *
	 * @param facetName the facet name.
	 */
	void setFacetName(String facetName);

	/**
	 * Get the type of the field key.
	 *
	 * @return the field key type.
	 */
	Integer getFieldKeyType();

	/**
	 * Sets the field key type.
	 *
	 * @param fieldKeyType the field key.
	 */
	void setFieldKeyType(Integer fieldKeyType);

	/**
	 * Gets the facet type.
	 *
	 * @return the facet type.
	 */
	Integer getFacetType();

	/**
	 * Sets the facet type.
	 *
	 * @param facetType the facet type.
	 */
	void setFacetType(Integer facetType);

	/**
	 * Check if the facet is searchable.
	 *
	 * @return true if the facet is searchable, false otherwise.
	 */
	Boolean getSearchableOption();

	/**
	 * Sets the searchable option for the facet.
	 *
	 * @param searchableOption the facet searchable option.
	 */
	void setSearchableOption(Boolean searchableOption);

	/**
	 * Gets the range facet values.
	 *
	 * @return the range facet values.
	 */
	String getRangeFacetValues();

	/**
	 * Sets the range facet values.
	 *
	 * @param rangeFacetValues the range facet values;
	 */
	void setRangeFacetValues(String rangeFacetValues);

	/**
	 * Gets the sorted range facet.
	 *
	 * @return the sorted range facet.
	 */
	SortedSet<RangeFacet> getSortedRangeFacet();

	/**
	 * Set the range facet map.
	 *
	 * @param sortedRangeFacet range facet map
	 */
	void setSortedRangeFacet(SortedSet<RangeFacet> sortedRangeFacet);

	/**
	 * Gets the store code.
	 *
	 * @return the store code.
	 */
	String getStoreCode();

	/**
	 * Sets the store code.
	 *
	 * @param storeCode the store code.
	 */
	void setStoreCode(String storeCode);

	/**
	 * Get ordinal of field group.
	 * @return ordinal
	 */
	Integer getFacetGroup();

	/**
	 * Set the ordinal of facet group.
	 * @param facetGroup facet group
	 */
	void setFacetGroup(Integer facetGroup);

	/**
	 * Get display name map.
	 *
	 * @return display name map
	 */
	Map<String, String> getDisplayNameMap();

	/**
	 * Set display name map.
	 *
	 * @param displayNameMap display name map
	 */
	void setDisplayNameMap(Map<String, String> displayNameMap);

	/**
	 * Get the facet guid.
	 *
	 * @return the facet guid
	 */
	String getFacetGuid();

	/**
	 * Set the facet guid.
	 *
	 * @param facetGuid the facet guid
	 */
	void setFacetGuid(String facetGuid);

	/**
	 * Get the business object id.
	 *
	 * @return the business object id
	 */
	String getBusinessObjectId();

	/**
	 * Set the business object id.
	 *
	 * @param businessObjectId the business object id
	 */
	void setBusinessObjectId(String businessObjectId);
}