/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer;

import java.util.List;
import java.util.Optional;

import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;

/**
 * Attribute policy service.
 */
public interface AttributePolicyService {

	/**
	 * Find attribute policy by guid.
	 *
	 * @param guid the guid
	 * @return the attribute policy
	 */
	Optional<AttributePolicy> findByGuid(String guid);

	/**
	 * Find attribute policies by guids.
	 *
	 * @param guids the guids
	 * @return list of attribute policies
	 */
	List<AttributePolicy> findByGuids(List<String> guids);

	/**
	 * Find attribute policies by key.
	 *
	 * @param policyKey the policy key
	 * @return list of attribute policies
	 */
	List<AttributePolicy> findByPolicyKey(PolicyKey policyKey);

	/**
	 * Find all attribute policies.
	 *
	 * @return list of attribute policies
	 */
	List<AttributePolicy> findAll();

	/**
	 * Add attribute policy.
	 *
	 * @param attributePolicy the attribute policy
	 */
	void add(AttributePolicy attributePolicy);

	/**
	 * Update attribute policy.
	 *
	 * @param attributePolicy the attribute policy
	 */
	void update(AttributePolicy attributePolicy);

	/**
	 * Remove attribute policy.
	 *
	 * @param attributePolicy the attribute policy
	 */
	void remove(AttributePolicy attributePolicy);

	/**
	 * Find attribute policies by store code and attribute key.
	 *
	 * @param storeCode    store code
	 * @param attributeKey attribute key
	 * @return list of attribute policies
	 */
	List<AttributePolicy> findByStoreCodeAndAttributeKey(String storeCode, String attributeKey);
}
