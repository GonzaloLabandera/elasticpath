/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer;

import com.elasticpath.persistence.api.Entity;

/**
 * Represents an attribute permission policy.
 */
public interface AttributePolicy extends Entity {

	/**
	 * Get policy key.
	 *
	 * @return policy key
	 */
	PolicyKey getPolicyKey();

	/**
	 * Set policy key.
	 *
	 * @param policyKey policy key
	 */
	void setPolicyKey(PolicyKey policyKey);

	/**
	 * Get policy permission.
	 *
	 * @return policy permission
	 */
	PolicyPermission getPolicyPermission();

	/**
	 * Set policy permission.
	 *
	 * @param policyPermission policy permission
	 */
	void setPolicyPermission(PolicyPermission policyPermission);
}
