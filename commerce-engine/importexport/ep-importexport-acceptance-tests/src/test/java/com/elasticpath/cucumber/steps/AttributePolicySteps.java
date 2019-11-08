/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.steps;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.builder.customer.AttributePolicyBuilder;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.service.customer.AttributePolicyService;

/**
 * Attribute policy steps.
 */
public class AttributePolicySteps {

	@Autowired
	private AttributePolicyService attributePolicyService;

	@Autowired
	private AttributePolicyBuilder attributePolicyBuilder;

	/**
	 * Setup the tests with attribute policies.
	 *
	 * @param dataTable attribute policy data table
	 */
	@Given("^the existing attribute policies of$")
	public void setUpAttributePolicies(final DataTable dataTable) {
		createAttributePoliciesFromMap(dataTable.asMaps(String.class, String.class));
	}

	private void createAttributePoliciesFromMap(final List<Map<String, String>> policiesMap) {
		policiesMap.forEach(properties -> {
			final AttributePolicy policy = attributePolicyBuilder.newInstance()
					.withGuid(properties.get("guid"))
					.withPolicyKey(PolicyKey.valueOf(properties.get("policyKey")))
					.withPolicyPermission(PolicyPermission.valueOf(properties.get("policyPermission")))
					.build();
			attributePolicyService.add(policy);
		});
	}

	/**
	 * Ensure attribute policies have correct values.
	 *
	 * @param policyDataTable policy data table
	 */
	@Then("the following attribute policies exist$")
	public void ensureAttributePolicies(final DataTable policyDataTable) {
		final List<Map<String, String>> policyMaps = policyDataTable.asMaps(String.class, String.class);
		policyMaps.forEach(this::ensureAttributePolicy);
	}

	private void ensureAttributePolicy(final Map<String, String> policyMap) {
		final String guid = policyMap.get("guid");
		final PolicyKey policyKey = PolicyKey.valueOf(policyMap.get("policyKey"));
		final PolicyPermission policyPermission = PolicyPermission.valueOf(policyMap.get("policyPermission"));

		final AttributePolicy policy = attributePolicyService.findByGuid(guid).get();

		assertThat(policy)
				.as(String.format("Attribute policy [%s] not found", guid))
				.isNotNull();

		assertThat(policyKey)
				.as(String.format("Attribute policy [%s] key is incorrect", guid))
				.isEqualTo(policy.getPolicyKey());

		assertThat(policyPermission)
				.as(String.format("Attribute policy [%s] permission is incorrect", guid))
				.isEqualTo(policy.getPolicyPermission());
	}
}
