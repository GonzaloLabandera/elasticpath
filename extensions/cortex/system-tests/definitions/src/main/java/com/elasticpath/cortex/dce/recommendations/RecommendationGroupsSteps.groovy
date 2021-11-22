package com.elasticpath.cortex.dce.recommendations

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Item

class RecommendationGroupsSteps {

	@When('^I go to recommendations for an (?:item|offer)$')
	static void clickRecommendationLink() {
		Item.recommendations()
	}

	@Then('^I get all the recommendation groups that exist$')
	static void getAllRecommendationGroups() {
		// mapped from CE to Cortex - currently there are 6 (cross sells, upsells, warranties, topsellers, accessories, replacements)

		def response = client.body.links.findAll {
			link -> link.rel
		}
		assertThat(response.size())
				.as("Number of links not as expected")
				.isEqualTo(6)
		assertThat(assertLinkExists(client, "crosssell"))
				.as("Cross Sell link does not exist")
				.isTrue()
		assertThat(assertLinkExists(client, "upsell"))
				.as("Upsell link does not exist")
				.isTrue()
		assertThat(assertLinkExists(client, "replacement"))
				.as("Replacement link does not exist")
				.isTrue()
		assertThat(assertLinkExists(client, "accessory"))
				.as("Accessory link does not exist")
				.isTrue()
		assertThat(assertLinkExists(client, "warranty"))
				.as("Warrenty link does not exist")
				.isTrue()
		assertThat(assertLinkExists(client, "recommendation"))
				.as("Recommendation link does not exist")
				.isTrue()
	}
}
