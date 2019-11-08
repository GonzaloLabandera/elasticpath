/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.StoreCustomerAttributeImpl;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * Attribute Policy Service integration test.  These tests rely on a set of predefined policies that
 * are part of the core liquibase changelog.
 */
public class AttributePolicyServiceImplTest extends BasicSpringContextTest {

	private static final String DEFAULT_EDIT = "default-edit";
	private static final String DEFAULT_EMIT = "default-emit";
	private static final String GUID = "guid";

	@Autowired
	private AttributePolicyService attributePolicyService;

	@Autowired
	private StoreCustomerAttributeService storeCustomerAttributeService;

	@Test
	public void ensureThatDefaultPoliciesCanBeFoundByGuid() {
		assertThat(attributePolicyService.findByGuid(DEFAULT_EDIT).get()).as("policy should be found by guid")
				.hasFieldOrPropertyWithValue(GUID, DEFAULT_EDIT);

		assertThat(attributePolicyService.findByGuids(Lists.newArrayList(DEFAULT_EDIT, DEFAULT_EMIT)))
				.as("policies should be found by list of guids")
				.hasSize(2);
	}

	@Test
	public void ensureThatDefaultPoliciesCanBeFoundByPolicyKey() {
		assertThat(attributePolicyService.findByPolicyKey(PolicyKey.DEFAULT))
				.as("policies should be found by policy key")
				.hasSize(2);
	}

	@Test
	public void ensureThatAllPoliciesCanBeFound() {
		assertThat(attributePolicyService.findAll())
				.as("all policies should be found")
				.hasSize(4);
	}

	@Test
	public void ensureThatPoliciesCanBeFoundByStoreCodeAndAttributeKey() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);

		StoreCustomerAttribute storeCustomerAttribute = new StoreCustomerAttributeImpl();
		storeCustomerAttribute.setAttributeKey(CustomerImpl.ATT_KEY_CP_PREF_LOCALE);
		storeCustomerAttribute.setPolicyKey(PolicyKey.DEFAULT);
		storeCustomerAttribute.setStoreCode(scenario.getStore().getCode());
		storeCustomerAttribute.setGuid(Utils.uniqueCode("SCA"));
		storeCustomerAttributeService.add(storeCustomerAttribute);

		assertThat(attributePolicyService.findByStoreCodeAndAttributeKey(scenario.getStore().getCode(),
				CustomerImpl.ATT_KEY_CP_PREF_LOCALE))
				.as("policies should be found by store code and attribute key")
				.hasSize(2);
	}
}
