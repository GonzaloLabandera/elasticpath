/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.builder.customer;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.DomainObjectBuilder;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;

/**
 * A builder that builds {@link AttributePolicy}s for testing purposes.
 */
public class AttributePolicyBuilder implements DomainObjectBuilder<AttributePolicy> {

	@Autowired
	private BeanFactory beanFactory;

	private Long uidPk = 0L;

	private String guid;

	private PolicyKey policyKey;

	private PolicyPermission policyPermission;

	/**
	 * Get the instance.
	 * @return the instance
	 */
	public AttributePolicyBuilder newInstance() {
		final AttributePolicyBuilder newBuilder = new AttributePolicyBuilder();
		newBuilder.setBeanFactory(beanFactory);
		return newBuilder;
	}

	/**
	 * Add the uidpk.
	 * @param uidPk the uidpk
	 * @return the builder
	 */
	public AttributePolicyBuilder withUidPk(final Long uidPk) {
		this.uidPk = uidPk;
		return this;
	}

	/**
	 * Add the guid.
	 * @param guid the guid
	 * @return the builder
	 */
	public AttributePolicyBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * Add the policy key.
	 * @param policyKey the policy key
	 * @return the builder
	 */
	public AttributePolicyBuilder withPolicyKey(final PolicyKey policyKey) {
		this.policyKey = policyKey;
		return this;
	}

	/**
	 * Add the policy permission.
	 * @param policyPermission the policy permission
	 * @return the builder
	 */
	public AttributePolicyBuilder withPolicyPermission(final PolicyPermission policyPermission) {
		this.policyPermission = policyPermission;
		return this;
	}

	@Override
	public AttributePolicy build() {
		final AttributePolicy attributePolicy =
				beanFactory.getPrototypeBean(ContextIdNames.ATTRIBUTE_POLICY, AttributePolicy.class);
		attributePolicy.setUidPk(uidPk);
		attributePolicy.setGuid(guid);
		attributePolicy.setPolicyKey(policyKey);
		attributePolicy.setPolicyPermission(policyPermission);
		return attributePolicy;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
