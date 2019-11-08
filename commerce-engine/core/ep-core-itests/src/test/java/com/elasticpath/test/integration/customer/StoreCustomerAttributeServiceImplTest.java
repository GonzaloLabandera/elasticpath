/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.StoreCustomerAttributeImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * Store Customer Attribute Service integration test. These tests rely on a set of predefined policies that
 * are part of the core liquibase changelog.
 */
public class StoreCustomerAttributeServiceImplTest extends BasicSpringContextTest {
	private static final String GUID = "guid";

	@Autowired
	private StoreCustomerAttributeService storeCustomerAttributeService;

	private String prefLocaleGuid;

	private String emailGuid;

	private Store store;

	@Before
	public void initialize() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);
		store = scenario.getStore();

	}

	@DirtiesDatabase
	@Test
	public void ensureThatDefaultPoliciesCanBeFoundByGuid() {
		createStoreCustomerAttributes();

		assertThat(storeCustomerAttributeService.findByGuid(prefLocaleGuid).get())
				.as("store customer attribute should be found by guid")
				.hasFieldOrPropertyWithValue(GUID, prefLocaleGuid);

		assertThat(storeCustomerAttributeService.findByGuids(Lists.newArrayList(prefLocaleGuid, emailGuid)))
				.as("store customer attributes should be found by list of guids")
				.hasSize(2);
	}

	@DirtiesDatabase
	@Test
	public void ensureThatStoreCustomerAttributesCanBeFoundByStoreCodeAndAttributeKey() {
		createStoreCustomerAttributes();

		assertThat(storeCustomerAttributeService.findByStoreCodeAndAttributeKey(store.getCode(),
				CustomerImpl.ATT_KEY_CP_PREF_LOCALE).get())
				.as("store customer attribute should be found by store code and attribute key")
				.hasFieldOrPropertyWithValue(GUID, prefLocaleGuid);
	}

	@DirtiesDatabase
	@Test
	public void ensureThatStoreCustomerAttributesCanBeFoundByStoreCode() {
		createStoreCustomerAttributes();

		assertThat(storeCustomerAttributeService.findByStore(store.getCode()))
				.as("store customer attributes should be found by store code")
				.hasSize(2);
	}

	@DirtiesDatabase
	@Test
	public void ensureThatAllStoreCustomerAttributesCanBeFound() {
		createStoreCustomerAttributes();

		assertThat(storeCustomerAttributeService.findAll())
				.as("all store customer attributes should be found")
				.hasSize(2);
	}

	private void createStoreCustomerAttributes() {
		StoreCustomerAttribute storeCustomerAttribute = new StoreCustomerAttributeImpl();
		storeCustomerAttribute.setAttributeKey(CustomerImpl.ATT_KEY_CP_PREF_LOCALE);
		storeCustomerAttribute.setPolicyKey(PolicyKey.DEFAULT);
		storeCustomerAttribute.setStoreCode(store.getCode());
		prefLocaleGuid = Utils.uniqueCode(CustomerImpl.ATT_KEY_CP_PREF_LOCALE);
		storeCustomerAttribute.setGuid(prefLocaleGuid);
		storeCustomerAttributeService.add(storeCustomerAttribute);

		storeCustomerAttribute = new StoreCustomerAttributeImpl();
		storeCustomerAttribute.setAttributeKey(CustomerImpl.ATT_KEY_CP_EMAIL);
		storeCustomerAttribute.setPolicyKey(PolicyKey.DEFAULT);
		storeCustomerAttribute.setStoreCode(store.getCode());
		emailGuid = Utils.uniqueCode(CustomerImpl.ATT_KEY_CP_EMAIL);
		storeCustomerAttribute.setGuid(emailGuid);
		storeCustomerAttributeService.add(storeCustomerAttribute);
	}


}
