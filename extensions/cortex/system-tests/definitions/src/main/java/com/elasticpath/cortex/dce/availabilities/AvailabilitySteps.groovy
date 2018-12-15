package com.elasticpath.cortex.dce.availabilities

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.LineItem

class AvailabilitySteps {

	@When('^I view the item availability$')
	static void viewItemAvailability() {
		Item.availability()
	}

	@When('^I view the cart line item availability$')
	static void viewCartLineItemAvailability() {
		LineItem.availability()
	}

	@Then('^The availability should be (.+?)$')
	static void verifyAvailability(def expectedAvailability) {
		assertThat(client.body.state)
				.as("Availability is not as expected")
				.isEqualTo(expectedAvailability)
	}

	@Then('^the field release-date contains a valid date')
	static void verifyReleaseDateIsValid() {
		assertThat(client["release-date"]."display-value".toString())
				.as("Release date is not as expected")
				.matches("(January|February|March|April|May|June|July|August|September|October|November|December) [1-3]?[0-9], \\d{4} \\d{1,2}:\\d{2}:\\d{2} (AM|PM)")
	}

}
