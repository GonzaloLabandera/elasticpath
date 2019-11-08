/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;

/**
 * Test of the public API of <code>AttributePolicyImpl</code>.
 */
public class AttributePolicyImplTest {

	private final AttributePolicyImpl attributePolicy
			= new AttributePolicyImpl();

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributePolicyImpl.uidpk' getter/setter.
	 */
	@Test
	public void testUidpk() {
		attributePolicy.setUidPk(1L);
		assertThat(attributePolicy.getUidPk()).as("uidpk should match").isEqualTo(1L);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributePolicyImpl.guid' getter/setter.
	 */
	@Test
	public void testGuid() {
		final String guid = "GUID";
		attributePolicy.setGuid(guid);
		assertThat(attributePolicy.getGuid()).as("guid should match").isEqualTo(guid);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributePolicyImpl.policyKey' getter/setter.
	 */
	@Test
	public void testPolicyKey() {
		attributePolicy.setPolicyKey(PolicyKey.DEFAULT);
		assertThat(attributePolicy.getPolicyKey()).as("policy key should match")
				.isEqualTo(PolicyKey.DEFAULT);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributePolicyImpl.policyPermission' getter/setter.
	 */
	@Test
	public void testPolicyPermission() {
		attributePolicy.setPolicyPermission(PolicyPermission.NONE);
		assertThat(attributePolicy.getPolicyPermission()).as("policy permission should match")
				.isEqualTo(PolicyPermission.NONE);
	}
}
