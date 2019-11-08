/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.List;
import java.util.Optional;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Implementation of {@link AttributePolicyService}.
 */
public class AttributePolicyServiceImpl extends AbstractEpPersistenceServiceImpl implements AttributePolicyService {

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getPersistenceEngine().get(AttributePolicy.class, uid);
	}

	@Override
	public Optional<AttributePolicy> findByGuid(final String guid) {
		final List<AttributePolicy> attributePolicies =
				getPersistenceEngine().retrieveByNamedQuery("ATTRIBUTE_POLICY_FIND_BY_GUID", guid);

		return attributePolicies.stream()
				.findFirst();
	}

	@Override
	public List<AttributePolicy> findByGuids(final List<String> guids) {
		return getPersistenceEngine().retrieveByNamedQueryWithList("ATTRIBUTE_POLICY_FIND_BY_GUIDS", "list", guids);
	}

	@Override
	public List<AttributePolicy> findByPolicyKey(final PolicyKey policyKey) {
		return getPersistenceEngine().retrieveByNamedQuery("ATTRIBUTE_POLICY_FIND_BY_KEY", policyKey);
	}

	@Override
	public List<AttributePolicy> findAll() {
		return getPersistenceEngine().retrieveByNamedQuery("ATTRIBUTE_POLICIES_FIND_ALL");
	}

	@Override
	public void add(final AttributePolicy attributePolicy) {
		getPersistenceEngine().save(attributePolicy);
	}

	@Override
	public void update(final AttributePolicy attributePolicy) {
		getPersistenceEngine().saveOrUpdate(attributePolicy);
	}

	@Override
	public void remove(final AttributePolicy attributePolicy) {
		getPersistenceEngine().delete(attributePolicy);
	}

	@Override
	public List<AttributePolicy> findByStoreCodeAndAttributeKey(final String storeCode, final String attributeKey) {
		return getPersistenceEngine()
				.retrieveByNamedQuery("ATTRIBUTE_POLICIES_FIND_BY_ATTRIBUTE_STORE", storeCode, attributeKey);
	}
}
