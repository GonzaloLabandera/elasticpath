/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer;

import com.elasticpath.persistence.api.Entity;

/**
 * Represents Store Customer attribute.
 */
public interface StoreCustomerAttribute extends Entity {

	/**
	 * Gets store code.
	 *
	 * @return store code
	 */
	String getStoreCode();

	/**
	 * Sets store code.
	 *
	 * @param storeCode storeCode
	 */
	void setStoreCode(String storeCode);

	/**
	 * Get the attribute key.
	 *
	 * @return the attribute key
	 */

	String getAttributeKey();

	/**
	 * Set the attribute key.
	 *
	 * @param attributeKey attribute key
	 */
	void setAttributeKey(String attributeKey);

	/**
	 * Set policy key.
	 *
	 * @param policyKey policy key.
	 */
	void setPolicyKey(PolicyKey policyKey);

	/**
	 * Set policy key.
	 *
	 * @return policy key.
	 */
	PolicyKey getPolicyKey();

}
