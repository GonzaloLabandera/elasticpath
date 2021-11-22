package com.elasticpath.cortex.dce.facets

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager

import com.elasticpath.CucumberDTO.Facet
import com.elasticpath.cortexTestObjects.Facets

class FacetsSteps {

	private static final Logger LOGGER = LogManager.getLogger(FacetsSteps.class)
	static final int SLEEP_ONE_SECONDS_IN_MILLIS = 1000

	@Then('^the expected facet choice list matches the following list$')
	static void verifyChoiceList(DataTable choiceListTable) {
		def choiceList = choiceListTable.asList(Facet)

		def resultsUri = client.save()

		for (Facet facet : choiceList) {
			boolean isExist = false
			int attempts = 0;
			// waiting for cache to expire
			while (!isExist && attempts < 100) {
				client.resume(resultsUri)
				client.GET(client.body.self.uri)
				client.body.links.find {
					if (it.rel == "choice" || it.rel == "chosen") {
						client.GET(it.href)
								.description()
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace(attempts + " ..... actual values ..." + client["value"] + "count:" + client["count"])
						}
						if (client["count"] == facet.getCount() && client["value"] == facet.getValue()) {
							return isExist = true

						}
					}
				}
				if (isExist) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(attempts + " ..... found ..." + facet.value + ":" + facet.count)
					}
					break
				}
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(attempts + " .....Unable to find..." + facet.value + ":" + facet.count)
				}
				sleep(SLEEP_ONE_SECONDS_IN_MILLIS)
				attempts++;
			}
			assertThat(isExist)
					.as("Unable to find expected facet choice - " + facet.value + ":" + facet.count)
					.isTrue()
		}
		client.resume(resultsUri)
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

	@Then('^the list of facets does not contain the following$')
	static void verifyListMatches(DataTable facetsListTable) {
		def resultsUri = client.body.self.uri
		def facetList = facetsListTable.asList(String)
		assertThat(Facets.getActualList("element", "display-name"))
				.as("Facets list size/order is not as expected")
				.doesNotContain(facetList)
		client.GET(resultsUri)
	}
}
