/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.elasticpath.domain.customer.PolicyKey;

/**
 * Test of the public API of <code>StoreCustomerAttributeImpl</code>.
 */
public class StoreCustomerAttributeImplTest {

	private final StoreCustomerAttributeImpl storeCustomerAttribute = new StoreCustomerAttributeImpl();

	/**
	 * Test method for 'com.elasticpath.domain.impl.StoreCustomerAttributeImpl.uidpk' getter/setter.
	 */
	@Test
	public void testUidpk() {
		storeCustomerAttribute.setUidPk(1L);
		assertThat(storeCustomerAttribute.getUidPk()).as("uidpk should match").isEqualTo(1L);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.StoreCustomerAttributeImpl.guid' getter/setter.
	 */
	@Test
	public void testGuid() {
		final String guid = "GUID";
		storeCustomerAttribute.setGuid(guid);
		assertThat(storeCustomerAttribute.getGuid()).as("guid should match").isEqualTo(guid);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.StoreCustomerAttributeImpl.attributeKey' getter/setter.
	 */
	@Test
	public void testAttributeKey() {
		final String key = "KEY";
		storeCustomerAttribute.setAttributeKey(key);
		assertThat(storeCustomerAttribute.getAttributeKey()).as("attribute key should match").isEqualTo(key);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.StoreCustomerAttributeImpl.storeCode' getter/setter.
	 */
	@Test
	public void testStoreCode() {
		final String code = "CODE";
		storeCustomerAttribute.setStoreCode(code);
		assertThat(storeCustomerAttribute.getStoreCode()).as("store code should match").isEqualTo(code);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.StoreCustomerAttributeImpl.policyKey' getter/setter.
	 */
	@Test
	public void testPolicyKey() {
		storeCustomerAttribute.setPolicyKey(PolicyKey.DEFAULT);
		assertThat(storeCustomerAttribute.getPolicyKey()).as("policy key should match").isEqualTo(PolicyKey.DEFAULT);
	}
}
