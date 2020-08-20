/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.attribute;

import java.util.Map;

/**
 * Defines where the <code>Attribute</code> is used for.
 */
public interface AttributeUsage {

	/** Category attribute usage id. */
	int CATEGORY = 1;

	/** Product attribute usage id. */
	int PRODUCT = 2;

	/** Sku attribute usage id. */
	int SKU = 3;

	/** User Profile attribute usage id. */
	int USER_PROFILE = 4;
	
	/** 
	 * Customer Profile attribute usage id. 
	 * @deprecated Use {@link #USER_PROFILE} instead. 
	 */
	@Deprecated
	int CUSTOMERPROFILE = USER_PROFILE;
	
	/** Account Profile attribute usage id. */
	int ACCOUNT_PROFILE = 5;
	/**
	 * Returns the attribute usage value.
	 *
	 * @return the attribute usage value
	 */
	int getValue();

	/**
	 * Sets the attribute usage value.
	 *
	 * @param value the attribute usage value
	 */
	void setValue(int value);
	
	/**
	 * Returns the attribute usage name.
	 *
	 * @return the attribute usage name
	 */
	String getName();

	/**
	 * Interface for adding types.
	 * You can use this to wire in changes.
	 *
	 * @param addedTypes the map to wire in.
	 */
	void setAddedTypes(Map<String, String> addedTypes);

	/**
	 * Return a map of attribute usages keyed by their value (int id). Created to get rid of duplicate info in the attribute service.
	 *
	 * @return map of attribute usages.
	 * @Override
	 */
	Map<String, String> getAttributeUsageMap();

	/**
	 * Returns an <code>AttributeUsage</code> of the given id.
	 *
	 * @param usageId the attribute usage id
	 * @return an <code>AttributeUsage</code> of the given id
	 */
	AttributeUsage getAttributeUsageById(int usageId);
	
	/**
	 * Returns the attribute usage name message key.
	 *
	 * @return the attribute usage name message key.
	 */
	String getNameMessageKey();
}
