package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.resultspane.SearchIndexesResultPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.Constants;

public class SearchIndexesDefinition {
	private final SearchIndexesResultPane searchIndexesResultPane;
	private final ConfigurationActionToolbar configurationActionToolbar;

	/**
	 * Constructor
	 */
	public SearchIndexesDefinition() {
		configurationActionToolbar = new ConfigurationActionToolbar(SetUp.getDriver());
		searchIndexesResultPane = new SearchIndexesResultPane(SetUp.getDriver());
	}

	/**
	 * Open Search Indexes
	 */
	@When("^I go to Search Indexes$")
	public void openSearchIndexes() {
		configurationActionToolbar.clickSearchIndexes();
	}

	/**
	 * Rebuilds specified index
	 * @param indexName index name
	 */
	@When("^I rebuild (.+) index$")
	public void rebuildIndexByName(final String indexName) {
		searchIndexesResultPane.rebuildIndexByName(indexName);
	}

	/**
	 * Verifies specified index's current status
	 *
	 * @param indexName index name to be checked
	 * @param status expected status value
	 */
	@And("^Index (.+) should have status: (.+)$")
	public void verifyIndexStatus(final String indexName, final String status) {
		int index = 0;
		while(!status.equals(searchIndexesResultPane.getSearchIndexStatus(indexName)) && index <= Constants.RETRY_COUNTER_5) {
			searchIndexesResultPane.sleep(Constants.SLEEP_FIVE_SECONDS_IN_MILLIS);
			index++;
		}
		assertThat(searchIndexesResultPane.getSearchIndexStatus(indexName))
			.as("Index Status is not as expected")
			.isEqualTo(status);
	}
}
