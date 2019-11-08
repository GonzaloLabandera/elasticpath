/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.steps;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.builder.customer.StoreCustomerAttributeBuilder;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.test.persister.TestApplicationContext;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Store customer attribute steps.
 */
public class StoreCustomerAttributeSteps {

	@Autowired
	private TestApplicationContext tac;

	@Autowired
	private StoreCustomerAttributeService storeCustomerAttributeService;

	@Autowired
	private StoreCustomerAttributeBuilder storeCustomerAttributeBuilder;

	private SimpleStoreScenario scenario;

	/**
	 * Initialize the simple store scenario before the test.
	 */
	@Before(order = 0)
	public void initializeScenario() {
		scenario = tac.useScenario(SimpleStoreScenario.class);
	}

	/**
	 * Setup the tests with store customer attributes.
	 *
	 * @param dataTable store customer attribute data table
	 */
	@Given("^the existing store customer attributes of$")
	public void setUpStoreCustomerAttributes(final DataTable dataTable) {
		createStoreCustomerAttributesFromMap(dataTable.asMaps(String.class, String.class));
	}

	private void createStoreCustomerAttributesFromMap(final List<Map<String, String>> attributesMap) {
		attributesMap.forEach(properties -> {
			final StoreCustomerAttribute storeCustomerAttribute = storeCustomerAttributeBuilder.newInstance()
					.withGuid(properties.get("guid"))
					.withStoreCode(properties.get("storeCode"))
					.withAttributeKey(properties.get("attributeKey"))
					.withPolicyKey(PolicyKey.valueOf(properties.get("policyKey")))
					.build();
			storeCustomerAttributeService.add(storeCustomerAttribute);
		});
	}

	/**
	 * Creates a customer profile attribute.
	 *
	 * @param attributeKey the attribute key
	 */
	@Given("^the customer profile attribute (.+) has been created$")
	public void createCustomerProfileAttribute(final String attributeKey) {
		tac.getPersistersFactory().getCatalogTestPersister().persistAttribute(scenario.getCatalog().getCode(), attributeKey,
				attributeKey, AttributeUsageImpl.CUSTOMERPROFILE_USAGE.toString(), AttributeType.SHORT_TEXT.toString(), false,
				true, false);
	}

	/**
	 * Creates a store.
	 *
	 * @param storeCode the store code
	 */
	@Given("^the store (.+) has been created$")
	public void createStore(final String storeCode) {
		tac.getPersistersFactory().getStoreTestPersister().persistStore(scenario.getCatalog(),
				scenario.getWarehouse(), storeCode, "USD");
	}

	/**
	 * Ensure store customer attributes have correct values.
	 *
	 * @param attributeDataTable attribute data table
	 */
	@Then("the following store customer attributes exist$")
	public void ensureStoreCustomerAttributes(final DataTable attributeDataTable) {
		final List<Map<String, String>> attributeMaps = attributeDataTable.asMaps(String.class, String.class);
		attributeMaps.forEach(this::ensureStoreCustomerAttribute);
	}

	private void ensureStoreCustomerAttribute(final Map<String, String> attributeMap) {
		final String guid = attributeMap.get("guid");
		final String storeCode = attributeMap.get("storeCode");
		final String attributeKey = attributeMap.get("attributeKey");
		final PolicyKey policyKey = PolicyKey.valueOf(attributeMap.get("policyKey"));

		final StoreCustomerAttribute attribute = storeCustomerAttributeService.findByGuid(guid).get();

		assertThat(attribute)
				.as(String.format("Store customer attribute [%s] not found", guid))
				.isNotNull();

		assertThat(storeCode)
				.as(String.format("Store customer attribute [%s] store code is incorrect", guid))
				.isEqualTo(attribute.getStoreCode());

		assertThat(attributeKey)
				.as(String.format("Store customer attribute [%s] attribute key is incorrect", guid))
				.isEqualTo(attribute.getAttributeKey());

		assertThat(policyKey)
				.as(String.format("Store customer attribute [%s] policy key is incorrect", guid))
				.isEqualTo(attribute.getPolicyKey());
	}
}
