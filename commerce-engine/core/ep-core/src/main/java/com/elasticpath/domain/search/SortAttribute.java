/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search;

import java.util.Map;

import com.elasticpath.persistence.api.Persistable;

/**
 * Sort attribute.
 */
public interface SortAttribute extends Persistable {

	/**
	 * Get the guid.
	 * @return guid
	 */
	String getGuid();

	/**
	 * Set the guid.
	 * @param guid guid
	 */
	void setGuid(String guid);

	/**
	 * If the attribute is a product attribute then this will be the attribute key, otherwise it will be the product name.
	 * @return key
	 */
	String getBusinessObjectId();

	/**
	 * Set the business object id.
	 * @param businessObjectId key
	 */
	void setBusinessObjectId(String businessObjectId);

	/**
	 * Get the store code.
	 * @return store code
	 */
	String getStoreCode();

	/**
	 * Set the store code.
	 * @param storeCode store code
	 */
	void setStoreCode(String storeCode);

	/**
	 * True if descending.
	 * @return is descending
	 */
	boolean isDescending();

	/**
	 * Sets the descending order.
	 * @param descending descending
	 */
	void setDescending(boolean descending);

	/**
	 * Get the localized names keyed on localeCode.
	 * @return map of localized names
	 */
	Map<String, SortLocalizedName> getLocalizedNames();

	/**
	 * Sets the localized names.
	 * @param localizedNames localized names
	 */
	void setLocalizedNames(Map<String, SortLocalizedName> localizedNames);

	/**
	 * Get sort attribute type.
	 * @return the type of the attribute
	 */
	SortAttributeGroup getSortAttributeGroup();

	/**
	 * Set the sort attribute type.
	 * @param sortAttributeGroup sort attribute type
	 */
	void setSortAttributeGroup(SortAttributeGroup sortAttributeGroup);

	/**
	 * Type of sort attribute.
	 * @return type
	 */
	SortAttributeType getSortAttributeType();

	/**
	 * Sets the type.
	 * @param sortAttributeType sort attribute type
	 */
	void setSortAttributeType(SortAttributeType sortAttributeType);

	/**
	 * True if sort attribute is default.
	 * @return boolean
	 */
	boolean isDefaultAttribute();

	/**
	 * Set if its default.
	 * @param defaultAttribute is default
	 */
	void setDefaultAttribute(boolean defaultAttribute);
}
