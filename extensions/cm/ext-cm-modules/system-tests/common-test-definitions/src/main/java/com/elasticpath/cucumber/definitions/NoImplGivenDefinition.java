package com.elasticpath.cucumber.definitions;

import java.util.Map;

import cucumber.api.java.en.Given;

/**
 * Steps without actual implementations, used to provide business clarity.
 */
public class NoImplGivenDefinition {

	@Given("a facet (.+) that is configured to be (.+)$")
	public void givenSearchableState(final String facetName, final String state) {
		// empty
	}

	@Given("^the following sort attribute exists$")
	public void givenSortAttribute(final Map<String, String> map) {
		// empty
	}

	@Given("^the default sort attribute is (.+)")
	public void givenDefaultSortAttribute(final String sortAttribute) {
		// empty
	}

	@Given("^the payment configuration (.+) is associated with store$")
	public void givenExistingPaymentConfigurationAssociatedToStore(final String paymentConfigurationName) {
		// empty
	}
}
