package com.elasticpath.cortex.dce.facets

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.CucumberDTO.Facet
import com.elasticpath.cortexTestObjects.Facets

class FacetsSteps {
	@Then('^the expected facet choice list matches the following list$')
	static void verifyChoiceList(DataTable choiceListTable) {
		def choiceList = choiceListTable.asList(Facet)

		def resultsUri = client.save()

		for (Facet facet : choiceList) {
			boolean isExist = false
			client.resume(resultsUri)
			client.body.links.find {
				if (it.rel == "choice" || it.rel == "chosen") {
					client.GET(it.href)
							.description()
					if (client["count"] == facet.getCount() && client["value"] == facet.getValue()) {
						return isExist = true

					}
				}
			}
			assertThat(isExist)
					.as("Unable to find expected facet choice - " + facet.value + ":" + facet.count)
					.isTrue()
		}
		client.resume(resultsUri)
	}

	@When('^I select the choice (.+)$')
	static void selectFaceetChoice(String choice) {
		Facets.selectFacetChoice(choice, "choice")
	}

	@When('^I unselect the choice (.+)$')
	static void unselectFaceetChoice(String choice) {
		Facets.selectFacetChoice(choice, "chosen")
	}

	@Then('^the offer search results list contains items with display-names$')
	static void verifyElementListContains(DataTable dataTable) {
		def facetResultDisplayNameList = dataTable.asList(String)
		def resultsUri = client.save()
		assertThat(Facets.getFacetResultDisplayNameList())
				.as("Facets list size/order is not as expected")
				.containsExactlyInAnyOrderElementsOf(facetResultDisplayNameList)
		client.resume(resultsUri)
	}
}
