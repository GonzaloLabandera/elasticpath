package com.elasticpath.cortex.dce.searches

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortex.dce.CommonSteps
import com.elasticpath.cortexTestObjects.FindItemBy

class SearchesSteps {

	@When('^I follow the root searches link$')
	static void clickSearchLink() {
		FindItemBy.search()
	}

	@Then('^I find the searches resource (.+?)$')
	static void verifySearchLinkExists(String searchLink) {
		assertLinkExists(client, searchLink)
	}

	@When('^I search for item name (.+?)$')
	static void searchForItemByName(String searchItemName) {
		CommonMethods.searchAndOpenItemWithKeyword(searchItemName)
	}

	@When('^I search for keyword \"(.+?)\"$')
	static void searchByKeyWord(String keyword) {
		CommonMethods.search(keyword, "5")
		client.follow()
				.stopIfFailure()
	}

	@When('^I search for the keyword \"(.+?)\" with page-size (.+)$')
	static void searchByKeyWordAndPageSize(String keyword, String pageSize) {
		CommonMethods.search(keyword, pageSize)
		client.follow()
				.stopIfFailure()
	}

	@When('^I POST to the search form the keyword \"(.+?)\" with page-size (.+)$')
	static void searchByKeyWordAndPageSizeNoFollow(String keyword, String pageSize) {
		CommonMethods.search(keyword, pageSize)
	}

	@When('^I POST to the search form with a (.+) char keyword$')
	static void searchWithChars(int numberOfChars) {
		String keyword = ""
		for (def i = 1; i < numberOfChars; i++) {
			keyword = keyword + "a"
		}
		CommonMethods.search(keyword, "5")
	}

	@Then('^the element list contains items with display-names$')
	static void verifyElementListContains(DataTable dataTable) {
		def productNames = dataTable.asList(String)
		def resultsUri = client.body.self.uri
		for (String name : productNames) {
			Boolean found = false
			client.GET(resultsUri)
			client.findElement {
				item ->
					def definition = item.definition()
					if (definition["display-name"] == name)
						found = true
			}
			assertThat(found)
					.as("Item $name was not in element list")
					.isTrue()
		}
	}

	@Then('^the element list does not contains items with display-names$')
	static void verifyElementListNotContain(DataTable dataTable) {
		def productNames = dataTable.asList(String)
		def resultsUri = client.body.self.uri
		for (String name : productNames) {
			Boolean found = false
			client.GET(resultsUri)
			client.findElement {
				item ->
					def definition = item.definition()
					if (definition["display-name"] == name)
						found = true
			}
			assertThat(!found)
					.as("Unexpected item $name was in element list")
					.isTrue()
		}
	}
}
