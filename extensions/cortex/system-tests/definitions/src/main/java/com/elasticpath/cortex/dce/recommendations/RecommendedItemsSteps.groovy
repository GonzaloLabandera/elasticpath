package com.elasticpath.cortex.dce.recommendations

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DISPLAY_NAME_FIELD
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Item

class RecommendedItemsSteps {

	@When('^I retrieve the recommendation (crosssell|upsell|replacement|warranty|accessory) for this item$')
	static void gotoRecommendationByType(def recommendationType) {
		Item.navigateToRecommendationType(recommendationType)
	}

	@Then('^I get the ([0-9]*) recommended items$')
	static void getRecommendedItems(int numRecommendedItems) {
		def response = client.body.links.findAll {
			link -> link.rel
		}
		assertThat(response.size())
				.as("The number of recommended items is not as expected")
				.isEqualTo(numRecommendedItems.toInteger())
	}

	@And('^The ordering is correctly preserved (.+?)$')
	static void verifyOrderingPreserved(String firstAssociationName) {
		client.element()
				.definition()
				.stopIfFailure()
		assertThat(client.body[DISPLAY_NAME_FIELD])
				.as("The first association name is not as expected")
				.isEqualTo(firstAssociationName)
	}

	@When('^I zoom (.+?) into the cross-sells for this item$')
	static void zoomTo(String zoomParam) {
		client.GET(client.body.self.uri + zoomParam)
	}

	@Then('^The zoom ordering is correctly preserved (.+?) and (.+?)$')
	static void verifyZoomOrderingPreserved(String firstAssociationName, String secondAssociationName) {
		assertThat(client.body._crosssell._element._definition[DISPLAY_NAME_FIELD][0][0][0])
				.as("The first association name is not as expected")
				.isEqualTo(firstAssociationName)
		assertThat(client.body._crosssell._element._definition[DISPLAY_NAME_FIELD][0][1][0])
				.as("The second association name is not as expected")
				.isEqualTo(secondAssociationName)
	}

	@Then('^cross-sell item is not present$')
	static void verifyCrossSellItemNotPresent() {
		def response = client.body.links.findAll {
			link -> link.rel
		}
		assertThat(response.size())
				.as("The cross-sell item should not be present")
				.isEqualTo(0)
	}
}
